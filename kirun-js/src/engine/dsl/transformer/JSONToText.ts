import { FunctionDefinition } from '../../model/FunctionDefinition';
import { KIRuntime } from '../../runtime/KIRuntime';
import { KIRunFunctionRepository } from '../../repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../repository/KIRunSchemaRepository';
import { ExpressionHandler } from './ExpressionHandler';
import { SchemaTransformer } from './SchemaTransformer';

interface NestedStructure {
    blockName: string;
    parent: string;
    statements: string[];
}

/**
 * JSON to Text Transformer
 * Converts FunctionDefinition JSON back to DSL text
 * Uses execution graph to reconstruct nested block structure
 */
export class JSONToTextTransformer {
    private indentChar: string = '    '; // 4 spaces
    private functionRepo = new KIRunFunctionRepository();
    private schemaRepo = new KIRunSchemaRepository();

    /**
     * Transform JSON to DSL text
     */
    public async transform(json: any): Promise<string> {
        const lines: string[] = [];

        // Function header
        lines.push(`FUNCTION ${json.name}`);

        // Namespace
        if (json.namespace && json.namespace !== '_') {
            lines.push(`${this.indent(1)}NAMESPACE ${json.namespace}`);
        }

        // Parameters
        if (json.parameters && Object.keys(json.parameters).length > 0) {
            lines.push(`${this.indent(1)}PARAMETERS`);
            for (const [name, param] of Object.entries(json.parameters as any)) {
                const schemaText = this.schemaToText((param as any).schema);
                lines.push(`${this.indent(2)}${name} AS ${schemaText}`);
            }
        }

        // Events
        if (json.events && Object.keys(json.events).length > 0) {
            lines.push(`${this.indent(1)}EVENTS`);
            for (const [name, event] of Object.entries(json.events as any)) {
                lines.push(`${this.indent(2)}${name}`);
                const eventParams = (event as any).parameters;
                if (eventParams && Object.keys(eventParams).length > 0) {
                    for (const [paramName, schema] of Object.entries(eventParams)) {
                        const schemaText = this.schemaToText(schema);
                        lines.push(`${this.indent(3)}${paramName} AS ${schemaText}`);
                    }
                }
            }
        }

        // Logic
        lines.push(`${this.indent(1)}LOGIC`);

        // Build execution graph to get structure
        const nestedStructure = await this.buildNestedStructure(json);

        // Generate statement lines with nesting
        const stmtLines = await this.stepsToText(json.steps, nestedStructure, 2);
        lines.push(...stmtLines);

        return lines.join('\n');
    }

    /**
     * Build nested structure from execution graph
     */
    private async buildNestedStructure(json: any): Promise<Map<string, NestedStructure>> {
        const structure = new Map<string, NestedStructure>();

        try {
            // Create function definition
            const funcDef = FunctionDefinition.from(json);

            // Get execution graph
            const runtime = new KIRuntime(funcDef);
            const graph = await runtime.getExecutionPlan(this.functionRepo, this.schemaRepo);

            // Analyze dependencies to identify nested blocks
            for (const [stepName, step] of Object.entries(json.steps)) {
                const deps = (step as any).dependentStatements || {};

                // Look for dependencies that indicate nesting
                // Pattern: "Steps.parentName.blockName"
                for (const depKey of Object.keys(deps)) {
                    const match = depKey.match(/^Steps\.([^.]+)\.(.+)$/);
                    if (match) {
                        const parentName = match[1];
                        const blockName = match[2];

                        // This step is nested under parentName in blockName block
                        structure.set(stepName, {
                            blockName,
                            parent: parentName,
                            statements: [], // Will be populated later
                        });
                    }
                }
            }
        } catch (error) {
            // If we can't build execution graph, fall back to flat structure
            // Silently fall back to flat structure
        }

        return structure;
    }

    /**
     * Convert steps to DSL text with nesting
     */
    private async stepsToText(
        steps: any,
        nestedStructure: Map<string, NestedStructure>,
        baseIndent: number,
    ): Promise<string[]> {
        const lines: string[] = [];

        // Group statements by parent
        const topLevel: string[] = [];
        const byParent = new Map<string, Map<string, string[]>>();

        for (const stepName of Object.keys(steps)) {
            const nestInfo = nestedStructure.get(stepName);
            if (nestInfo) {
                // This is a nested statement
                if (!byParent.has(nestInfo.parent)) {
                    byParent.set(nestInfo.parent, new Map());
                }
                const parentBlocks = byParent.get(nestInfo.parent)!;
                if (!parentBlocks.has(nestInfo.blockName)) {
                    parentBlocks.set(nestInfo.blockName, []);
                }
                parentBlocks.get(nestInfo.blockName)!.push(stepName);
            } else {
                // This is a top-level statement
                topLevel.push(stepName);
            }
        }

        // Render top-level statements
        for (const stepName of topLevel) {
            const step = steps[stepName];
            const stmtLines = this.stepToText(step, baseIndent);
            lines.push(...stmtLines);

            // Render nested blocks for this statement
            const blocks = byParent.get(stepName);
            if (blocks) {
                for (const [blockName, nestedSteps] of blocks) {
                    lines.push(`${this.indent(baseIndent + 1)}${blockName}`);
                    for (const nestedStepName of nestedSteps) {
                        const nestedStep = steps[nestedStepName];
                        const nestedLines = this.stepToText(nestedStep, baseIndent + 2);
                        lines.push(...nestedLines);
                    }
                }
            }
        }

        return lines;
    }

    /**
     * Convert single step to DSL text
     */
    private stepToText(step: any, indent: number): string[] {
        const lines: string[] = [];
        const ind = this.indent(indent);

        // Build function call
        const funcCall = `${step.namespace}.${step.name}(${this.argsToText(step.parameterMap)})`;
        let line = `${ind}${step.statementName}: ${funcCall}`;

        // Add AFTER clause (filter out block dependencies)
        const afterSteps: string[] = [];
        const deps = step.dependentStatements || {};
        for (const [depKey, value] of Object.entries(deps)) {
            // Skip block dependencies (Steps.x.blockName)
            if (!depKey.match(/^Steps\.[^.]+\..+$/)) {
                afterSteps.push(depKey);
            }
        }

        if (afterSteps.length > 0) {
            line += ` AFTER ${afterSteps.join(', ')}`;
        }

        // Add IF clause
        const ifSteps = Object.keys(step.executeIftrue || {});
        if (ifSteps.length > 0) {
            line += ` IF ${ifSteps.join(', ')}`;
        }

        lines.push(line);

        return lines;
    }

    /**
     * Convert parameter map to argument list string
     */
    private argsToText(parameterMap: any): string {
        const args: string[] = [];

        for (const [paramName, paramRefs] of Object.entries(parameterMap || {})) {
            // Get first parameter reference (there should only be one per parameter)
            const refEntries = Object.entries(paramRefs as any);
            if (refEntries.length > 0) {
                const [_, ref] = refEntries[0];
                const value = this.paramRefToText(ref);
                args.push(`${paramName} = ${value}`);
            }
        }

        return args.join(', ');
    }

    /**
     * Convert parameter reference to text
     */
    private paramRefToText(ref: any): string {
        if (ref.type === 'EXPRESSION') {
            return ref.expression;
        } else {
            return this.valueToText(ref.value);
        }
    }

    /**
     * Convert value to text representation
     */
    private valueToText(value: any): string {
        if (value === null || value === undefined) {
            return 'null';
        }

        if (typeof value === 'string') {
            // Check if it looks like an expression
            if (ExpressionHandler.isExpression({ isExpression: true, value })) {
                return value;
            }
            return `"${value}"`;
        }

        if (typeof value === 'boolean') {
            return value ? 'true' : 'false';
        }

        if (typeof value === 'number') {
            return String(value);
        }

        if (Array.isArray(value)) {
            if (value.length === 0) {
                return '[]';
            }
            const items = value.map((v) => this.valueToText(v));
            return `[${items.join(', ')}]`;
        }

        if (typeof value === 'object') {
            // Check if it's an expression object
            if (ExpressionHandler.isExpression(value)) {
                return value.value;
            }

            // Check if it's a schema object
            if (value.type) {
                // Might be a schema - try to convert to text
                const schemaText = SchemaTransformer.toText(value);
                if (schemaText.startsWith('{')) {
                    // Complex schema - use as-is
                    return schemaText;
                }
                // Simple schema - wrap in parentheses for schema literal
                return `(${schemaText})`;
            }

            // Regular object
            return JSON.stringify(value);
        }

        return String(value);
    }

    /**
     * Convert schema to text
     */
    private schemaToText(schema: any): string {
        return SchemaTransformer.toText(schema);
    }

    /**
     * Generate indentation
     */
    private indent(level: number): string {
        return this.indentChar.repeat(level);
    }
}

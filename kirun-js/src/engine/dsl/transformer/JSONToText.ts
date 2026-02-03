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

                // Collect all block dependencies for this step
                const blockDeps: { parent: string; blockName: string }[] = [];
                const validBlockNames = ['true', 'false', 'iteration'];

                // Look for dependencies that indicate nesting
                // Pattern: "Steps.parentName.blockName"
                for (const depKey of Object.keys(deps)) {
                    const decodedKey = this.decodeDots(depKey);
                    const match = decodedKey.match(/^Steps\.([^.]+)\.(.+)$/);
                    if (match) {
                        const parentName = match[1];
                        const blockName = match[2];

                        // Only treat specific block names as nested blocks
                        // - true/false for If statements
                        // - iteration for Loop statements
                        // - NOT output (that's just a dependency on the statement's output)
                        if (validBlockNames.includes(blockName)) {
                            blockDeps.push({ parent: parentName, blockName });
                        }
                    }
                }

                // Only nest a step if it depends on exactly ONE block from ONE parent
                // If it depends on multiple blocks (like both true and false), it's a top-level step
                // that runs after the if statement, not inside a specific branch
                if (blockDeps.length === 1) {
                    const dep = blockDeps[0];
                    structure.set(stepName, {
                        blockName: dep.blockName,
                        parent: dep.parent,
                        statements: [], // Will be populated later
                    });
                }
                // If blockDeps.length > 1, the step depends on multiple blocks
                // (e.g., both true and false), so it's NOT nested - it's a top-level step
            }

            // Detect and break circular dependencies
            // A circular dependency occurs when A's parent is B and B's parent is A (or longer chains)
            const stepsInCycles = this.detectCircularDependencies(structure);
            for (const stepName of stepsInCycles) {
                structure.delete(stepName);
            }
        } catch (error) {
            // If we can't build execution graph, fall back to flat structure
            // Silently fall back to flat structure
        }

        return structure;
    }

    /**
     * Detect circular dependencies in the nested structure
     * Returns set of step names that are part of cycles
     */
    private detectCircularDependencies(structure: Map<string, NestedStructure>): Set<string> {
        const inCycle = new Set<string>();

        for (const [stepName] of structure) {
            // Follow the parent chain to detect cycles
            const visited = new Set<string>();
            let current: string | undefined = stepName;

            while (current && structure.has(current)) {
                if (visited.has(current)) {
                    // Found a cycle - mark all visited steps as in cycle
                    for (const s of visited) {
                        inCycle.add(s);
                    }
                    break;
                }
                visited.add(current);
                current = structure.get(current)?.parent;
            }
        }

        return inCycle;
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

        // Handle missing steps
        if (!steps) {
            return lines;
        }

        // Group statements by parent
        const topLevel: string[] = [];
        const byParent = new Map<string, Map<string, string[]>>();

        for (const stepName of Object.keys(steps)) {
            const nestInfo = nestedStructure.get(stepName);
            if (nestInfo) {
                // This is a nested statement - but only if parent exists
                if (steps[nestInfo.parent]) {
                    if (!byParent.has(nestInfo.parent)) {
                        byParent.set(nestInfo.parent, new Map());
                    }
                    const parentBlocks = byParent.get(nestInfo.parent)!;
                    if (!parentBlocks.has(nestInfo.blockName)) {
                        parentBlocks.set(nestInfo.blockName, []);
                    }
                    parentBlocks.get(nestInfo.blockName)!.push(stepName);
                } else {
                    // Parent doesn't exist - treat as top-level
                    topLevel.push(stepName);
                }
            } else {
                // This is a top-level statement
                topLevel.push(stepName);
            }
        }

        // Helper function to render a step and its nested blocks recursively
        const renderStepWithNestedBlocks = (stepName: string, step: any, indent: number): void => {
            const stmtLines = this.stepToText(step, indent);
            lines.push(...stmtLines);

            // Render nested blocks for this statement
            const blocks = byParent.get(stepName);
            if (blocks) {
                for (const [blockName, nestedSteps] of blocks) {
                    lines.push(`${this.indent(indent + 1)}${blockName}`);
                    for (const nestedStepName of nestedSteps) {
                        const nestedStep = steps[nestedStepName];
                        // Recursively render the nested step and its own nested blocks
                        renderStepWithNestedBlocks(nestedStepName, nestedStep, indent + 2);
                    }
                }
            }
        };

        // Render top-level statements
        for (const stepName of topLevel) {
            const step = steps[stepName];
            renderStepWithNestedBlocks(stepName, step, baseIndent);
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

        // Add AFTER clause (filter out block dependencies like Steps.x.true/false/iteration)
        const afterSteps: string[] = [];
        const deps = step.dependentStatements || {};
        const validBlockNames = ['true', 'false', 'iteration'];
        for (const [depKey, value] of Object.entries(deps)) {
            const decodedKey = this.decodeDots(depKey);
            const match = decodedKey.match(/^Steps\.([^.]+)\.(.+)$/);
            if (match) {
                const blockName = match[2];
                // Only skip actual block dependencies
                if (!validBlockNames.includes(blockName)) {
                    afterSteps.push(decodedKey);
                }
            } else {
                afterSteps.push(decodedKey);
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
            // Get first parameter reference (sorted by order)
            // Note: Multi-value parameters are a known limitation - only the first value is preserved
            const refEntries = Object.entries(paramRefs as any);
            if (refEntries.length > 0) {
                // Sort by order and take first
                const sortedRefs = refEntries
                    .map(([_, ref]) => ref as any)
                    .sort((a, b) => (a.order || 0) - (b.order || 0));
                const ref = sortedRefs[0];
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
            const expr = ref.expression;
            // Handle empty expressions - wrap in backticks to preserve
            if (expr === '' || expr === undefined || expr === null) {
                return '``';
            }
            // For EXPRESSION types, check if the expression looks like a literal
            // If so, wrap in backticks to distinguish from VALUE
            if (this.expressionNeedsBackticks(expr)) {
                // Escape backslashes first, then backticks
                return '`' + expr.replace(/\\/g, '\\\\').replace(/`/g, '\\`') + '`';
            }
            // Single-quoted strings and other expressions are output as-is
            return expr;
        } else {
            // For VALUE types, use double-quoted strings
            // Double-quoted strings → parsed as VALUE
            return this.valueToTextPreserveType(ref.value);
        }
    }

    /**
     * Check if an expression needs to be wrapped in backticks
     * Expressions that look like literals need backticks to preserve their EXPRESSION type
     */
    private expressionNeedsBackticks(expr: string): boolean {
        if (!expr) return false;
        const trimmed = expr.trim();

        // Expressions containing double quotes need backticks
        // (either a full string or an expression with embedded strings)
        if (expr.includes('"')) {
            return true;
        }

        // Expressions containing single quotes need backticks
        // to avoid ambiguity with DSL's single-quote expression syntax
        if (expr.includes("'")) {
            return true;
        }

        // Boolean keywords need backticks (exact match or at start of expression)
        if (trimmed === 'true' || trimmed === 'false') {
            return true;
        }
        // Expressions starting with boolean keywords (e.g., "true and X", "false or Y")
        if (trimmed.startsWith('true ') || trimmed.startsWith('false ')) {
            return true;
        }

        // null keyword needs backticks
        if (trimmed === 'null') {
            return true;
        }

        // Numbers need backticks (exact numbers or expressions starting with numbers)
        if (/^-?\d+(\.\d+)?$/.test(trimmed)) {
            return true;
        }
        // Numeric expressions with operators (e.g., "3 * 604800")
        if (/^-?\d+(\.\d+)?\s*[+\-*\/%]/.test(trimmed)) {
            return true;
        }

        // Empty array/object literals need backticks
        if (trimmed === '[]' || trimmed === '{}') {
            return true;
        }

        return false;
    }

    /**
     * Convert value to text representation, always quoting strings
     * This is used for VALUE types where we want to preserve the value-ness
     * IMPORTANT: Do NOT try to detect schemas here - this is for raw VALUE types
     * and we need to preserve the exact value (not convert to schema syntax)
     */
    private valueToTextPreserveType(value: any): string {
        if (value === null || value === undefined) {
            return 'null';
        }

        if (typeof value === 'string') {
            // Always quote strings for VALUE types, even if they look like expressions
            // Escape backslashes first, then double quotes, and wrap with quotes
            return `"${value.replace(/\\/g, '\\\\').replace(/"/g, '\\"')}"`;
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
            const items = value.map((v) => this.valueToTextPreserveType(v));
            return `[${items.join(', ')}]`;
        }

        if (typeof value === 'object') {
            // Check if it's an expression object
            if (ExpressionHandler.isExpression(value)) {
                return value.value;
            }

            // For VALUE types, always use JSON - do NOT convert to schema syntax
            // This preserves the exact value structure for round-trip
            return JSON.stringify(value);
        }

        return String(value);
    }


    /**
     * Convert value to text representation
     */
    private valueToText(value: any): string {
        if (value === null || value === undefined) {
            return 'null';
        }

        if (typeof value === 'string') {
            // Check if it looks like an expression (starts with known prefixes)
            // Expression prefixes: Arguments., Steps., Context., Store., Page., etc.
            if (this.looksLikeExpression(value)) {
                return value;
            }
            // Escape backslashes first, then double quotes, and wrap with quotes
            return `"${value.replace(/\\/g, '\\\\').replace(/"/g, '\\"')}"`;
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

    /**
     * Decode MongoDB dot encoding (__d-o-t__ -> .)
     */
    private decodeDots(str: string): string {
        return str.replace(/__d-o-t__/g, '.');
    }

    /**
     * Check if a string value looks like an expression
     * Expressions typically start with known prefixes like Arguments., Steps., Context., etc.
     */
    private looksLikeExpression(value: string): boolean {
        // Common expression prefixes in KIRun
        const expressionPrefixes = [
            'Arguments.',
            'Steps.',
            'Context.',
            'Store.',
            'Page.',
            'Application.',
            'LocalStore.',
            'SessionStore.',
            'Cookies.',
            'URL.',
        ];

        // Check if starts with a known prefix
        for (const prefix of expressionPrefixes) {
            if (value.startsWith(prefix)) {
                return true;
            }
        }

        // Don't treat emails or URLs as expressions
        if (value.includes('@') || value.includes('://')) {
            return false;
        }

        // Don't treat file paths or common strings with dots as expressions
        const nonExpressionPatterns = [
            /\.(com|org|net|io|dev|co|edu|gov)$/i,  // Domain endings
            /\.(json|xml|html|css|js|ts|txt|md|pdf)$/i,  // File extensions
            /^\d+\.\d+/,  // Version numbers like 1.0.0
        ];
        for (const pattern of nonExpressionPatterns) {
            if (pattern.test(value)) {
                return false;
            }
        }

        return false;
    }
}

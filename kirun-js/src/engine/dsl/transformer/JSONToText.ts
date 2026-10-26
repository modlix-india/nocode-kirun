import { DSLLexer } from '../lexer/DSLLexer';
import { DSLTokenType } from '../lexer/DSLToken';
import { ExpressionHandler } from './ExpressionHandler';
import { SchemaTransformer } from './SchemaTransformer';

interface NestedStructure {
    blockName: string;
    parent: string;
}

/**
 * A ParameterReference's `type` as the schema declares it: a plain string.
 *
 * `ParameterReference.SCHEMA` types it as a string with enums EXPRESSION | VALUE,
 * but a large amount of stored data carries it as a single-element ARRAY,
 * `["EXPRESSION"]`. The runtime never minded — `KIRuntime` compares with `==`,
 * and `['EXPRESSION'] == 'EXPRESSION'` is true in JS — so those functions run
 * correctly and reach this transformer intact.
 *
 * Comparing `type === 'EXPRESSION'` here therefore missed the expression branch,
 * fell through to the value branch, and emitted the ref's `value`, which for an
 * expression ref is null. The round trip then silently replaced every expression
 * in the function with `null`. Read the type through this, never directly.
 */
function refType(ref: any): string | undefined {
    const type = ref?.type;
    return Array.isArray(type) ? type[0] : type;
}

/**
 * JSON to Text Transformer
 * Converts FunctionDefinition JSON back to DSL text
 * Extracts implicit dependencies from expressions using same logic as KIRuntime
 */
export class JSONToTextTransformer {
    private indentChar: string = '    '; // 4 spaces

    // A step line longer than this puts its arguments one per line.
    private static readonly LINE_WIDTH = 120;
    // An object or array argument longer than this on one line moves to VALUES.
    private static readonly INLINE_VALUE_WIDTH = 100;
    private static readonly PLAIN_KEY = /^[A-Za-z_]\w*$/;

    // Values moved to the VALUES section by the current transform, in order of use.
    private namedValues: { name: string; value: any; exprKey?: string }[] = [];
    private usedValueNames = new Set<string>();

    /**
     * Transform JSON to DSL text
     */
    public async transform(json: any): Promise<string> {
        this.namedValues = [];
        this.usedValueNames = new Set();
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

        // Build execution graph using KIRuntime to get implicit dependencies
        const nestedStructure = await this.buildNestedStructureFromRuntime(json);

        // Generate statement lines with nesting
        const stmtLines = await this.stepsToText(json.steps, nestedStructure, 2);
        lines.push(...stmtLines);

        // Large objects and arrays, kept out of the step list
        if (this.namedValues.length > 0) {
            lines.push(`${this.indent(1)}VALUES`);
            for (const { name, value, exprKey } of this.namedValues) {
                const head = `${this.indent(2)}@${name} = `;
                lines.push(head + this.formatJson(value, 2, head.length, exprKey));
            }
        }

        return lines.join('\n');
    }

    // Same regex pattern as KIRuntime uses for extracting step dependencies
    // Matches: Steps.stepName.eventName (e.g., Steps.fetch.output, Steps.loop.iteration)
    private static readonly STEP_REGEX_PATTERN: RegExp = /Steps\.([a-zA-Z0-9_-]+)\.([a-zA-Z0-9_-]+)/g;

    /**
     * Build nested structure by extracting dependencies from expressions
     * Uses the same logic as KIRuntime for finding step dependencies
     */
    private async buildNestedStructureFromRuntime(json: any): Promise<Map<string, NestedStructure>> {
        const structure = new Map<string, NestedStructure>();
        const steps = json.steps || {};
        const stepNames = new Set(Object.keys(steps));

        // First pass: Extract all dependencies for each step
        const allEdges = new Map<string, { parent: string; blockName: string }[]>();

        for (const [stepName, step] of Object.entries(steps)) {
            const stepObj = step as any;
            const dependencies = this.extractAllDependencies(stepObj, stepNames);
            allEdges.set(stepName, dependencies);
        }

        // Helper to calculate nesting depth of a step
        const getNestingDepth = (stepName: string, visited: Set<string> = new Set()): number => {
            if (visited.has(stepName)) return 0;
            visited.add(stepName);
            const nestInfo = structure.get(stepName);
            if (!nestInfo) return 0;
            return 1 + getNestingDepth(nestInfo.parent, visited);
        };

        // Sort steps topologically so dependencies are placed before dependents
        // This ensures getNestingDepth() returns accurate values when choosing between multiple parents
        const sortedSteps = this.topologicalSortForNesting(Array.from(allEdges.keys()), allEdges);

        // Second pass: Assign nesting, handling multiple edges by picking deepest parent
        let changed = true;
        while (changed) {
            changed = false;

            // Process steps in topological order
            for (const stepName of sortedSteps) {
                if (structure.has(stepName)) continue;

                const incomingEdges = allEdges.get(stepName);
                if (!incomingEdges || incomingEdges.length === 0) continue;

                if (incomingEdges.length === 1) {
                    const edge = incomingEdges[0];
                    structure.set(stepName, {
                        blockName: edge.blockName,
                        parent: edge.parent,
                    });
                    changed = true;
                } else {
                    // Multiple edges - pick the deepest parent
                    let deepestEdge = incomingEdges[0];
                    let maxDepth = getNestingDepth(deepestEdge.parent);

                    for (let i = 1; i < incomingEdges.length; i++) {
                        const edge = incomingEdges[i];
                        const depth = getNestingDepth(edge.parent);
                        if (depth > maxDepth) {
                            maxDepth = depth;
                            deepestEdge = edge;
                        }
                    }

                    structure.set(stepName, {
                        blockName: deepestEdge.blockName,
                        parent: deepestEdge.parent,
                    });
                    changed = true;
                }
            }
        }

        // Detect and break circular dependencies
        const stepsInCycles = this.detectCircularDependencies(structure);
        for (const stepName of stepsInCycles) {
            structure.delete(stepName);
        }

        return structure;
    }

    /**
     * Extract all step dependencies from a step (same logic as KIRuntime)
     * Extracts from: EXPRESSION parameters, JsonExpression in VALUE, dependentStatements, executeIftrue
     */
    private extractAllDependencies(
        step: any,
        validStepNames: Set<string>,
    ): { parent: string; blockName: string }[] {
        const deps: { parent: string; blockName: string }[] = [];
        const seenDeps = new Set<string>();

        const addDep = (text: string) => {
            // Reset regex lastIndex for global regex
            JSONToTextTransformer.STEP_REGEX_PATTERN.lastIndex = 0;
            let match;
            while ((match = JSONToTextTransformer.STEP_REGEX_PATTERN.exec(text)) !== null) {
                const parentStep = match[1];
                const blockName = match[2];
                const key = `${parentStep}.${blockName}`;

                if (validStepNames.has(parentStep) && !seenDeps.has(key)) {
                    seenDeps.add(key);
                    deps.push({ parent: parentStep, blockName });
                }
            }
        };

        // Extract from parameterMap
        if (step.parameterMap) {
            for (const [, paramRefs] of Object.entries(step.parameterMap)) {
                for (const [, ref] of Object.entries(paramRefs as any)) {
                    const refObj = ref as any;

                    // EXPRESSION type - extract from expression string
                    if (refType(refObj) === 'EXPRESSION' && refObj.expression) {
                        addDep(refObj.expression);
                    }

                    // VALUE type - check for nested expressions
                    if (refType(refObj) === 'VALUE' && refObj.value != null) {
                        // Recursively search for expressions in the value
                        this.extractExpressionsFromValue(refObj.value, addDep);
                    }
                }
            }
        }

        // Extract from dependentStatements (explicit dependencies like AFTER Steps.store.output)
        // These should also contribute to nesting - if step depends on Steps.store.output,
        // it should be nested under store's output block
        if (step.dependentStatements) {
            for (const depKey of Object.keys(step.dependentStatements)) {
                if (step.dependentStatements[depKey] !== true) continue;
                // Decode MongoDB dot encoding: Steps__d-o-t__store__d-o-t__output -> Steps.store.output
                const decodedKey = this.decodeDots(depKey);
                addDep(decodedKey);
            }
        }

        // Extract from executeIftrue
        if (step.executeIftrue) {
            for (const condition of Object.keys(step.executeIftrue)) {
                addDep(condition);
            }
        }

        return deps;
    }

    /**
     * Recursively extract expressions from a VALUE (handles nested objects/arrays)
     */
    private extractExpressionsFromValue(value: any, addDep: (text: string) => void): void {
        if (value == null) return;

        if (typeof value === 'string') {
            // Check if string contains step references
            addDep(value);
        } else if (Array.isArray(value)) {
            for (const item of value) {
                this.extractExpressionsFromValue(item, addDep);
            }
        } else if (typeof value === 'object') {
            // Check for JsonExpression pattern (isExpression: true, value: "...")
            if (value.isExpression === true && typeof value.value === 'string') {
                addDep(value.value);
            } else if (refType(value) === 'EXPRESSION' && typeof value.expression === 'string') {
                // Another expression pattern
                addDep(value.expression);
            } else {
                // Recursively check object properties
                for (const propValue of Object.values(value)) {
                    this.extractExpressionsFromValue(propValue, addDep);
                }
            }
        }
    }

    /**
     * Topological sort for nesting - ensures parent steps are processed before dependent steps
     * This prevents the issue where a step with multiple parents picks the wrong one
     * because the deeper parent hasn't been placed in the structure yet
     */
    private topologicalSortForNesting(
        stepNames: string[],
        allEdges: Map<string, { parent: string; blockName: string }[]>,
    ): string[] {
        const stepSet = new Set(stepNames);
        const dependencies = new Map<string, Set<string>>();

        // Build dependency graph: stepName depends on its parent steps
        for (const stepName of stepNames) {
            dependencies.set(stepName, new Set());
            const edges = allEdges.get(stepName) || [];
            for (const edge of edges) {
                if (stepSet.has(edge.parent)) {
                    dependencies.get(stepName)!.add(edge.parent);
                }
            }
        }

        // Kahn's algorithm for topological sort
        const sorted: string[] = [];
        const inDegree = new Map<string, number>();

        for (const stepName of stepNames) {
            inDegree.set(stepName, dependencies.get(stepName)!.size);
        }

        const queue: string[] = [];
        for (const stepName of stepNames) {
            if (inDegree.get(stepName) === 0) {
                queue.push(stepName);
            }
        }

        while (queue.length > 0) {
            const current = queue.shift()!;
            sorted.push(current);

            // Decrease in-degree for all steps that depend on current
            for (const [stepName, deps] of dependencies) {
                if (deps.has(current)) {
                    const newDegree = inDegree.get(stepName)! - 1;
                    inDegree.set(stepName, newDegree);
                    if (newDegree === 0) {
                        queue.push(stepName);
                    }
                }
            }
        }

        // Add remaining steps (cycles) - maintain original order for these
        for (const stepName of stepNames) {
            if (!sorted.includes(stepName)) {
                sorted.push(stepName);
            }
        }

        return sorted;
    }

    /**
     * Detect circular dependencies in the nested structure
     * Returns set of step names that are part of cycles
     */
    private detectCircularDependencies(structure: Map<string, NestedStructure>): Set<string> {
        const inCycle = new Set<string>();

        for (const [stepName] of structure) {
            const visited = new Set<string>();
            let current: string | undefined = stepName;

            while (current && structure.has(current)) {
                if (visited.has(current)) {
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
     * Extract step references from an expression string
     */
    private extractStepReferences(expr: string): string[] {
        const refs: string[] = [];
        const regex = /Steps\.([a-zA-Z_][a-zA-Z0-9_]*)/g;
        let match;
        while ((match = regex.exec(expr)) !== null) {
            refs.push(match[1]);
        }
        return refs;
    }

    /**
     * Extract all step dependencies from a step's parameterMap
     */
    private extractDependenciesFromParams(parameterMap: any): string[] {
        const deps: string[] = [];

        if (!parameterMap) return deps;

        for (const [, paramRefs] of Object.entries(parameterMap)) {
            for (const [, ref] of Object.entries(paramRefs as any)) {
                const refObj = ref as any;
                if (refType(refObj) === 'EXPRESSION' && refObj.expression) {
                    deps.push(...this.extractStepReferences(refObj.expression));
                }
                if (refType(refObj) === 'VALUE' && refObj.value) {
                    const valueStr = JSON.stringify(refObj.value);
                    deps.push(...this.extractStepReferences(valueStr));
                }
            }
        }

        return deps;
    }

    /**
     * Topologically sort steps based on their dependencies
     */
    private topologicalSort(stepNames: string[], steps: any): string[] {
        const dependencies = new Map<string, Set<string>>();
        const stepSet = new Set(stepNames);

        for (const stepName of stepNames) {
            dependencies.set(stepName, new Set());
            const step = steps[stepName];

            // Get explicit dependencies from dependentStatements
            const explicitDeps = step.dependentStatements || {};
            for (const depKey of Object.keys(explicitDeps)) {
                const decodedKey = this.decodeDots(depKey);
                const match = decodedKey.match(/^Steps\.([^.]+)/);
                if (match) {
                    const depStepName = match[1];
                    if (stepSet.has(depStepName)) {
                        dependencies.get(stepName)!.add(depStepName);
                    }
                }
            }

            // Get implicit dependencies from expressions
            const implicitDeps = this.extractDependenciesFromParams(step.parameterMap);
            for (const depStepName of implicitDeps) {
                if (stepSet.has(depStepName) && depStepName !== stepName) {
                    dependencies.get(stepName)!.add(depStepName);
                }
            }
        }

        // Kahn's algorithm
        const sorted: string[] = [];
        const inDegree = new Map<string, number>();

        for (const stepName of stepNames) {
            inDegree.set(stepName, dependencies.get(stepName)!.size);
        }

        const queue: string[] = [];
        for (const stepName of stepNames) {
            if (inDegree.get(stepName) === 0) {
                queue.push(stepName);
            }
        }

        while (queue.length > 0) {
            const current = queue.shift()!;
            sorted.push(current);

            for (const [stepName, deps] of dependencies) {
                if (deps.has(current)) {
                    const newDegree = inDegree.get(stepName)! - 1;
                    inDegree.set(stepName, newDegree);
                    if (newDegree === 0) {
                        queue.push(stepName);
                    }
                }
            }
        }

        // Add remaining steps (cycles)
        for (const stepName of stepNames) {
            if (!sorted.includes(stepName)) {
                sorted.push(stepName);
            }
        }

        return sorted;
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

        if (!steps) {
            return lines;
        }

        // A step that follows another's `output` is printed in the same block as that
        // step, right after it, so a sequence reads top to bottom instead of moving
        // right one level per step. Only branches (true, false, iteration, error and
        // other events) indent. Indentation carries no meaning for the compiler:
        // dependencies come from AFTER, IF and Steps references.
        const blockOf = (stepName: string): NestedStructure | undefined => {
            let nestInfo = nestedStructure.get(stepName);
            const seen = new Set<string>([stepName]);
            while (nestInfo?.blockName === 'output' && !seen.has(nestInfo.parent)) {
                seen.add(nestInfo.parent);
                nestInfo = nestedStructure.get(nestInfo.parent);
            }
            return nestInfo?.blockName === 'output' ? undefined : nestInfo;
        };

        // Group statements by parent
        const topLevel: string[] = [];
        const byParent = new Map<string, Map<string, string[]>>();

        for (const stepName of Object.keys(steps)) {
            const nestInfo = blockOf(stepName);
            if (nestInfo && steps[nestInfo.parent]) {
                if (!byParent.has(nestInfo.parent)) {
                    byParent.set(nestInfo.parent, new Map());
                }
                const parentBlocks = byParent.get(nestInfo.parent)!;
                if (!parentBlocks.has(nestInfo.blockName)) {
                    parentBlocks.set(nestInfo.blockName, []);
                }
                parentBlocks.get(nestInfo.blockName)!.push(stepName);
            } else {
                topLevel.push(stepName);
            }
        }

        // Sort statements
        const sortedTopLevel = this.topologicalSort(topLevel, steps);

        for (const [, blocks] of byParent) {
            for (const [blockName, nestedSteps] of blocks) {
                const sortedNested = this.topologicalSort(nestedSteps, steps);
                blocks.set(blockName, sortedNested);
            }
        }

        // Block ordering
        const blockOrder = (blockName: string): number => {
            const order: { [key: string]: number } = {
                'error': 0,
                'iteration': 1,
                'true': 2,
                'false': 3,
                'output': 4,
            };
            return order[blockName] ?? 3;
        };

        // Render step and nested blocks recursively
        const renderStepWithNestedBlocks = (stepName: string, step: any, indent: number): void => {
            const stmtLines = this.stepToText(step, stepName, indent);
            lines.push(...stmtLines);

            const blocks = byParent.get(stepName);
            if (blocks) {
                const sortedBlockNames = Array.from(blocks.keys()).sort(
                    (a, b) => blockOrder(a) - blockOrder(b),
                );

                for (const blockName of sortedBlockNames) {
                    const nestedSteps = blocks.get(blockName)!;
                    lines.push(`${this.indent(indent + 1)}${blockName}`);
                    for (const nestedStepName of nestedSteps) {
                        const nestedStep = steps[nestedStepName];
                        renderStepWithNestedBlocks(nestedStepName, nestedStep, indent + 2);
                    }
                }
            }
        };

        for (const stepName of sortedTopLevel) {
            const step = steps[stepName];
            renderStepWithNestedBlocks(stepName, step, baseIndent);
        }

        return lines;
    }

    /**
     * Convert single step to DSL text
     */
    private stepToText(step: any, stepKey: string, indent: number): string[] {
        const lines: string[] = [];
        const ind = this.indent(indent);

        const stepName = step.statementName || stepKey;
        const args = this.argsToText(step.parameterMap, stepName, step.namespace === 'System' && step.name === 'GenerateEvent');
        const head = `${ind}${stepName}: ${step.namespace}.${step.name}(`;
        let line = `${head}${args.join(', ')})`;

        // Add AFTER clause for explicit dependencies
        const afterSteps: string[] = [];
        const deps = step.dependentStatements || {};

        for (const [depKey, depValue] of Object.entries(deps)) {
            if (depValue !== true) continue;
            const decodedKey = this.decodeDots(depKey);
            afterSteps.push(decodedKey);
        }

        let tail = '';
        if (afterSteps.length > 0) {
            tail += ` AFTER ${afterSteps.join(', ')}`;
        }

        // Add IF clause
        const ifSteps = Object.keys(step.executeIftrue || {});
        if (ifSteps.length > 0) {
            tail += ` IF ${ifSteps.join(', ')}`;
        }

        // Add comment
        if (step.comment && step.comment.trim()) {
            const escapedComment = step.comment.replace(/\*\//g, '*\\/');
            tail += ` /* ${escapedComment} */`;
        }

        line += tail;

        // A long call puts one argument per line, closing `)` back at the step's indent
        if (line.length > JSONToTextTransformer.LINE_WIDTH && args.length > 1) {
            const argIndent = this.indent(indent + 1);
            lines.push(head);
            args.forEach((arg, i) => lines.push(`${argIndent}${arg}${i < args.length - 1 ? ',' : ''}`));
            lines.push(`${ind})${tail}`);
            return lines;
        }

        lines.push(line);
        return lines;
    }

    /**
     * Convert parameter map to a list of `name = value` arguments
     */
    private argsToText(parameterMap: any, stepName: string, isGenerateEvent: boolean): string[] {
        const args: string[] = [];

        for (const [paramName, paramRefs] of Object.entries(parameterMap || {})) {
            const refEntries = Object.entries(paramRefs as any);
            if (refEntries.length === 0) continue;

            const sortedRefs = refEntries
                .map(([_, ref]) => ref as any)
                .sort((a, b) => (a.order || 0) - (b.order || 0));

            // GenerateEvent evaluates the `value` of each `results` entry; see compactJson
            const exprKey = isGenerateEvent && paramName === 'results' ? 'value' : undefined;
            sortedRefs.forEach((ref, i) => {
                const valueName = sortedRefs.length > 1 ? `${stepName}.${paramName}.${i + 1}` : `${stepName}.${paramName}`;
                const value = this.paramRefToText(ref, valueName, exprKey);
                args.push(`${paramName} = ${value}`);
            });
        }

        return args;
    }

    /**
     * Convert parameter reference to text. An object or array value too long for
     * one line is moved to the VALUES section under `valueName` and printed as
     * `@valueName`.
     */
    private paramRefToText(ref: any, valueName: string, exprKey?: string): string {
        if (refType(ref) !== 'EXPRESSION') {
            const value = ref.value;
            if (value && typeof value === 'object' && !ExpressionHandler.isExpression(value)) {
                const text = this.compactJson(value, exprKey);
                if (text.length <= JSONToTextTransformer.INLINE_VALUE_WIDTH) return text;
                const name = this.uniqueValueName(valueName);
                this.namedValues.push({ name, value, exprKey });
                return `@${name}`;
            }
        }
        if (refType(ref) === 'EXPRESSION') {
            const expr = ref.expression;
            if (expr === '' || expr === undefined || expr === null) {
                return '``';
            }
            if (this.expressionNeedsBackticks(expr)) {
                return '`' + expr.replace(/\\/g, '\\\\').replace(/`/g, '\\`') + '`';
            }
            return expr;
        } else {
            return this.valueToTextPreserveType(ref.value);
        }
    }

    /**
     * Check if an expression needs to be wrapped in backticks
     */
    private expressionNeedsBackticks(expr: string): boolean {
        if (!expr) return false;
        const trimmed = expr.trim();

        // Whitespace at the end would be lost before a `)` on the next line
        if (/\s$/.test(expr)) return true;
        // `@name` is a named value
        if (/^@\w/.test(trimmed)) return true;
        if (expr.includes('"')) return true;
        if (expr.includes("'")) return true;
        if (trimmed === 'true' || trimmed === 'false') return true;
        if (trimmed.startsWith('true ') || trimmed.startsWith('false ')) return true;
        if (trimmed === 'null' || trimmed === 'undefined') return true;
        if (/^-?\d+(\.\d+)?$/.test(trimmed)) return true;
        if (/^-?\d+(\.\d+)?\s*[+\-*\/%]/.test(trimmed)) return true;
        if (trimmed === '[]' || trimmed === '{}') return true;

        return false;
    }

    /**
     * Convert value to text representation
     */
    private valueToTextPreserveType(value: any): string {
        if (value === undefined) return 'undefined';
        if (value === null) return 'null';

        if (typeof value === 'string') {
            return `"${value.replace(/\\/g, '\\\\').replace(/"/g, '\\"')}"`;
        }

        if (typeof value === 'boolean') {
            return value ? 'true' : 'false';
        }

        if (typeof value === 'number') {
            return String(value);
        }

        if (Array.isArray(value)) {
            if (value.length === 0) return '[]';
            const items = value.map((v) => this.valueToTextPreserveType(v));
            return `[${items.join(', ')}]`;
        }

        if (typeof value === 'object') {
            if (ExpressionHandler.isExpression(value)) {
                return value.value;
            }
            return JSON.stringify(value, null, 4);
        }

        return String(value);
    }

    /** `{isExpression: true, value: "<text>"}` and nothing else. */
    private static isPlainExpression(value: any): boolean {
        return ExpressionHandler.isExpression(value) && Object.keys(value).length === 2;
    }

    /**
     * One-line JSON in DSL form, with plain keys unquoted. Undefined is dropped from
     * objects and becomes null in arrays, as in JSON.
     *
     * In the object's `exprKey` entry, `{isExpression: true, value}` is written as the
     * expression itself, which the parser turns back into that object. Only
     * GenerateEvent evaluates such an object (the `value` of each `results` entry);
     * anywhere else the runtime passes it through as data, so it stays written out in
     * full rather than look like an expression that runs.
     */
    private compactJson(value: any, exprKey?: string): string {
        if (value === undefined || value === null) return 'null';
        if (typeof value !== 'object') return JSON.stringify(value) ?? 'null';
        if (Array.isArray(value)) {
            return `[${value.map((v) => this.compactJson(v)).join(', ')}]`;
        }
        const entries = Object.entries(value).filter(([, v]) => v !== undefined);
        if (entries.length === 0) return '{}';
        return `{${entries.map(([k, v]) => `${this.jsonKey(k)}: ${this.entryText(k, v, exprKey)}`).join(', ')}}`;
    }

    /** `{isExpression: false, value}` and nothing else: a GenerateEvent result given as a literal. */
    private static isPlainLiteral(value: any): boolean {
        return (
            !!value &&
            typeof value === 'object' &&
            value.isExpression === false &&
            'value' in value &&
            Object.keys(value).length === 2
        );
    }

    private entryText(key: string, value: any, exprKey?: string): string {
        if (key === exprKey && JSONToTextTransformer.isPlainExpression(value)) {
            return this.jsonExpressionText(value.value);
        }
        if (key === exprKey && JSONToTextTransformer.isPlainLiteral(value)) {
            return this.compactJson(value.value);
        }
        return this.compactJson(value);
    }

    /**
     * JSON that breaks over lines, one entry per line, only where the one-line form
     * would run past LINE_WIDTH starting at `column`.
     */
    private formatJson(value: any, level: number, column: number, exprKey?: string): string {
        const compact = this.compactJson(value, exprKey);
        if (column + compact.length <= JSONToTextTransformer.LINE_WIDTH || !value || typeof value !== 'object') {
            return compact;
        }

        const inner = this.indent(level + 1);
        const items = Array.isArray(value)
            ? value.map((v) => inner + this.formatJson(v, level + 1, inner.length))
            : Object.entries<any>(value)
                  .filter(([, v]) => v !== undefined)
                  .map(([k, v]) => {
                      const head = `${inner}${this.jsonKey(k)}: `;
                      if (k === exprKey && JSONToTextTransformer.isPlainExpression(v)) {
                          return head + this.jsonExpressionText(v.value);
                      }
                      if (k === exprKey && JSONToTextTransformer.isPlainLiteral(v)) {
                          return head + this.formatJson(v.value, level + 1, head.length);
                      }
                      return head + this.formatJson(v, level + 1, head.length);
                  });
        if (items.length === 0) return compact;
        const [open, close] = Array.isArray(value) ? ['[', ']'] : ['{', '}'];
        return `${open}\n${items.join(',\n')}\n${this.indent(level)}${close}`;
    }

    private jsonKey(key: string): string {
        // `null` lexes as a null literal, which the parser does not take as a key
        return JSONToTextTransformer.PLAIN_KEY.test(key) && key !== 'null' ? key : JSON.stringify(key);
    }

    /**
     * An expression inside a JSON value: bare when the parser reads it back as the
     * same expression, otherwise in backticks.
     */
    private jsonExpressionText(expr: string): string {
        if (this.isBareJsonExpression(expr)) return expr;
        return '`' + expr.replace(/\\/g, '\\\\').replace(/`/g, '\\`') + '`';
    }

    /**
     * Inside JSON the parser reads a string, number, boolean, null, undefined, object
     * or array literal as that literal, and an expression runs to the first `,` or
     * closing bracket at its own depth. So an expression can go bare only if it
     * starts with none of those literals, its brackets balance, it has no top-level
     * comma, and it has no whitespace at either end to lose.
     */
    private isBareJsonExpression(expr: string): boolean {
        if (!expr || expr !== expr.trim()) return false;
        let tokens;
        try {
            tokens = new DSLLexer(expr).tokenize().filter((t) => t.type !== DSLTokenType.EOF);
        } catch {
            return false;
        }
        if (tokens.length === 0) return false;

        const first = tokens[0];
        if (
            first.type === DSLTokenType.STRING ||
            first.type === DSLTokenType.NUMBER ||
            first.type === DSLTokenType.BOOLEAN ||
            first.type === DSLTokenType.NULL ||
            first.type === DSLTokenType.BACKTICK_STRING ||
            first.type === DSLTokenType.LEFT_BRACE ||
            first.type === DSLTokenType.LEFT_BRACKET ||
            (first.type === DSLTokenType.OPERATOR && first.value === '@') ||
            (first.type === DSLTokenType.KEYWORD && ['true', 'false', 'null'].includes(first.value)) ||
            (first.type === DSLTokenType.IDENTIFIER && first.value === 'undefined')
        ) {
            return false;
        }

        let depth = 0;
        for (const token of tokens) {
            switch (token.type) {
                case DSLTokenType.COMMENT:
                    return false;
                case DSLTokenType.LEFT_PAREN:
                case DSLTokenType.LEFT_BRACKET:
                case DSLTokenType.LEFT_BRACE:
                    depth++;
                    break;
                case DSLTokenType.RIGHT_PAREN:
                case DSLTokenType.RIGHT_BRACKET:
                case DSLTokenType.RIGHT_BRACE:
                    if (depth === 0) return false;
                    depth--;
                    break;
                case DSLTokenType.COMMA:
                    if (depth === 0) return false;
                    break;
            }
        }
        return depth === 0;
    }

    /** A VALUES name not used yet in this function: `step.param`, then `step.param_2`, ... */
    private uniqueValueName(base: string): string {
        const clean = base.replace(/[^\w.]/g, '_').replace(/\.(?=\.|$)|^\./g, '_');
        let name = clean;
        for (let n = 2; this.usedValueNames.has(name); n++) name = `${clean}_${n}`;
        this.usedValueNames.add(name);
        return name;
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
     * Decode MongoDB dot encoding
     */
    private decodeDots(str: string): string {
        return str.replace(/__d-o-t__/g, '.');
    }
}

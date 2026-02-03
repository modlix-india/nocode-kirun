import { ExpressionHandler } from './ExpressionHandler';
import { SchemaTransformer } from './SchemaTransformer';

interface NestedStructure {
    blockName: string;
    parent: string;
}

/**
 * JSON to Text Transformer
 * Converts FunctionDefinition JSON back to DSL text
 * Extracts implicit dependencies from expressions using same logic as KIRuntime
 */
export class JSONToTextTransformer {
    private indentChar: string = '    '; // 4 spaces

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

        // Build execution graph using KIRuntime to get implicit dependencies
        const nestedStructure = await this.buildNestedStructureFromRuntime(json);

        // Generate statement lines with nesting
        const stmtLines = await this.stepsToText(json.steps, nestedStructure, 2);
        lines.push(...stmtLines);

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
                    if (refObj.type === 'EXPRESSION' && refObj.expression) {
                        addDep(refObj.expression);
                    }

                    // VALUE type - check for nested expressions
                    if (refObj.type === 'VALUE' && refObj.value != null) {
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
            } else if (value.type === 'EXPRESSION' && typeof value.expression === 'string') {
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
                if (refObj.type === 'EXPRESSION' && refObj.expression) {
                    deps.push(...this.extractStepReferences(refObj.expression));
                }
                if (refObj.type === 'VALUE' && refObj.value) {
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

        // Group statements by parent
        const topLevel: string[] = [];
        const byParent = new Map<string, Map<string, string[]>>();

        for (const stepName of Object.keys(steps)) {
            const nestInfo = nestedStructure.get(stepName);
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
        const funcCall = `${step.namespace}.${step.name}(${this.argsToText(step.parameterMap)})`;
        let line = `${ind}${stepName}: ${funcCall}`;

        // Add AFTER clause for explicit dependencies
        const afterSteps: string[] = [];
        const deps = step.dependentStatements || {};

        for (const [depKey, depValue] of Object.entries(deps)) {
            if (depValue !== true) continue;
            const decodedKey = this.decodeDots(depKey);
            afterSteps.push(decodedKey);
        }

        if (afterSteps.length > 0) {
            line += ` AFTER ${afterSteps.join(', ')}`;
        }

        // Add IF clause
        const ifSteps = Object.keys(step.executeIftrue || {});
        if (ifSteps.length > 0) {
            line += ` IF ${ifSteps.join(', ')}`;
        }

        // Add comment
        if (step.comment && step.comment.trim()) {
            const escapedComment = step.comment.replace(/\*\//g, '*\\/');
            line += ` /* ${escapedComment} */`;
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
            const refEntries = Object.entries(paramRefs as any);
            if (refEntries.length === 0) continue;

            const sortedRefs = refEntries
                .map(([_, ref]) => ref as any)
                .sort((a, b) => (a.order || 0) - (b.order || 0));

            for (const ref of sortedRefs) {
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

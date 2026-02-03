import {
    ArgumentNode,
    ComplexValueNode,
    EventDeclNode,
    ExpressionNode,
    FunctionDefNode,
    ParameterDeclNode,
    SchemaLiteralNode,
    StatementNode,
} from '../parser/ast';
import { SchemaTransformer } from './SchemaTransformer';

/**
 * AST to JSON Transformer
 * Converts parsed AST to FunctionDefinition JSON
 */
export class ASTToJSONTransformer {
    /**
     * Transform AST to FunctionDefinition JSON
     * Only includes non-empty/non-default fields
     */
    public transform(ast: FunctionDefNode): any {
        const json: any = {
            name: ast.name,
        };

        // Only add namespace if not empty
        if (ast.namespace) {
            json.namespace = ast.namespace;
        }

        // Only add parameters if not empty
        const parameters = this.transformParameters(ast.parameters);
        if (Object.keys(parameters).length > 0) {
            json.parameters = parameters;
        }

        // Only add events if not empty
        const events = this.transformEvents(ast.events);
        if (Object.keys(events).length > 0) {
            json.events = events;
        }

        // Always add steps
        json.steps = this.transformSteps(ast.logic);

        return json;
    }

    /**
     * Transform parameters
     */
    private transformParameters(params: ParameterDeclNode[]): any {
        const result: any = {};
        for (const param of params) {
            result[param.name] = {
                parameterName: param.name,
                schema: SchemaTransformer.transform(param.schema.schemaSpec),
                variableArgument: false,
                type: 'EXPRESSION',
            };
        }
        return result;
    }

    /**
     * Transform events
     */
    private transformEvents(events: EventDeclNode[]): any {
        const result: any = {};
        for (const event of events) {
            result[event.name] = {
                name: event.name,
                parameters: this.transformEventParameters(event.parameters),
            };
        }
        return result;
    }

    /**
     * Transform event parameters (these are schemas, not Parameter objects)
     */
    private transformEventParameters(params: ParameterDeclNode[]): any {
        const result: any = {};
        for (const param of params) {
            result[param.name] = SchemaTransformer.transform(param.schema.schemaSpec);
        }
        return result;
    }

    /**
     * Transform statements (top-level and nested)
     */
    private transformSteps(statements: StatementNode[]): any {
        const result: any = {};
        for (const stmt of statements) {
            result[stmt.statementName] = this.transformStatement(stmt);
        }
        return result;
    }

    /**
     * Transform single statement
     * Only includes non-empty/non-default fields
     */
    private transformStatement(stmt: StatementNode): any {
        const json: any = {
            statementName: stmt.statementName,
            namespace: stmt.functionCall.namespace,
            name: stmt.functionCall.name,
        };

        // Only add parameterMap if not empty
        const parameterMap = this.transformParameterMap(stmt.functionCall.argumentsMap);
        if (Object.keys(parameterMap).length > 0) {
            json.parameterMap = parameterMap;
        }

        // Only add dependentStatements if not empty
        const dependentStatements = this.createDependentStatementsMap(stmt);
        if (Object.keys(dependentStatements).length > 0) {
            json.dependentStatements = dependentStatements;
        }

        // Only add executeIftrue if not empty
        const executeIftrue = this.createExecuteIfMap(stmt.executeIfSteps);
        if (Object.keys(executeIftrue).length > 0) {
            json.executeIftrue = executeIftrue;
        }

        // Only add comment if not empty
        if (stmt.comment && stmt.comment.trim()) {
            json.comment = stmt.comment;
        }

        return json;
    }

    /**
     * Create dependentStatements map from AFTER clause and nested blocks
     */
    private createDependentStatementsMap(stmt: StatementNode): any {
        const result: any = {};

        // Add explicit AFTER dependencies
        for (const stepRef of stmt.afterSteps) {
            result[stepRef] = true; // true = depends on this step
        }

        // Add implicit dependencies from nested blocks
        // Nested statements depend on their parent's specific events
        for (const [blockName, nestedStmts] of stmt.nestedBlocks) {
            // Create dependency key based on block name
            // e.g., "Steps.loop.iteration", "Steps.if.true"
            const dependencyKey = `Steps.${stmt.statementName}.${blockName}`;

            // Mark nested statements as depending on this block
            // (These will be added when we transform nested statements)
        }

        return result;
    }

    /**
     * Create executeIftrue map from IF clause
     */
    private createExecuteIfMap(executeIfSteps: string[]): any {
        const result: any = {};
        for (const stepRef of executeIfSteps) {
            result[stepRef] = true;
        }
        return result;
    }

    /**
     * Transform parameter map (function arguments)
     * Skips parameters with empty values
     * Supports multi-value parameters
     */
    private transformParameterMap(argsMap: Map<string, ArgumentNode>): any {
        const result: any = {};

        for (const [paramName, arg] of argsMap) {
            // Skip parameters with empty values
            if (this.isEmptyArgument(arg)) {
                continue;
            }

            result[paramName] = {};

            // Handle multi-value parameters
            if (arg.isMultiValue()) {
                for (let i = 0; i < arg.values.length; i++) {
                    const paramRef = this.transformArgumentValue(arg.values[i], i + 1);
                    result[paramName][paramRef.key] = paramRef;
                }
            } else {
                const paramRef = this.transformArgumentValue(arg.value, 1);
                result[paramName][paramRef.key] = paramRef;
            }
        }

        return result;
    }

    /**
     * Check if an argument is empty (no value provided)
     * Note: Empty expressions and undefined values are valid and should NOT be filtered out
     * Only filter out parser artifacts like stray delimiters
     */
    private isEmptyArgument(arg: ArgumentNode): boolean {
        if (arg.value instanceof ExpressionNode) {
            const expr = arg.value.expressionText.trim();
            // Only filter out parser artifacts (stray delimiters)
            // Empty expressions ('') are valid and should be preserved
            return expr === ',' || expr === ')';
        }
        // Both null and undefined are valid values and should be preserved
        // ComplexValueNode is never "empty" - it always has a value (even if null/undefined)
        return false;
    }

    /**
     * Transform single argument value to ParameterReference
     * @param value The argument value (expression, complex value, or schema literal)
     * @param order The order index for multi-value parameters (1-based)
     */
    private transformArgumentValue(
        value: ExpressionNode | ComplexValueNode | SchemaLiteralNode,
        order: number,
    ): any {
        const key = this.generateUUID();

        if (value instanceof ExpressionNode) {
            return {
                key,
                type: 'EXPRESSION',
                expression: value.expressionText,
                value: undefined,
                order: order,
            };
        } else if (value instanceof ComplexValueNode) {
            return {
                key,
                type: 'VALUE',
                value: value.value,
                expression: undefined,
                order: order,
            };
        } else if (value instanceof SchemaLiteralNode) {
            // Schema literal with optional default value
            const schema = SchemaTransformer.transform(value.schema.schemaSpec);

            if (value.defaultValue) {
                // Has default value - evaluate it
                return {
                    key,
                    type: 'VALUE',
                    value: this.evaluateDefaultValue(value.defaultValue, schema),
                    expression: undefined,
                    order: order,
                };
            } else {
                // No default value - just pass the schema
                return {
                    key,
                    type: 'VALUE',
                    value: schema,
                    expression: undefined,
                    order: order,
                };
            }
        }

        // Fallback
        return {
            key,
            type: 'VALUE',
            value: null,
            expression: undefined,
            order: order,
        };
    }

    /**
     * Evaluate default value for schema literal
     */
    private evaluateDefaultValue(defaultValueExpr: ExpressionNode, schema: any): any {
        const exprText = defaultValueExpr.expressionText.trim();

        // Try to parse as literal value
        if (exprText === '[]') {
            return [];
        }
        if (exprText === '{}') {
            return {};
        }
        if (exprText === 'null') {
            return null;
        }
        if (exprText === 'true') {
            return true;
        }
        if (exprText === 'false') {
            return false;
        }

        // Try to parse as number
        const num = parseFloat(exprText);
        if (!isNaN(num)) {
            return num;
        }

        // Try to parse as string literal
        if (
            (exprText.startsWith('"') && exprText.endsWith('"')) ||
            (exprText.startsWith("'") && exprText.endsWith("'"))
        ) {
            return exprText.slice(1, -1);
        }

        // Otherwise, return as-is (might be an expression)
        return exprText;
    }

    /**
     * Generate UUID for parameter reference keys
     */
    private generateUUID(): string {
        // Simple UUID v4 generation
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            const r = (Math.random() * 16) | 0;
            const v = c === 'x' ? r : (r & 0x3) | 0x8;
            return v.toString(16);
        });
    }

    /**
     * Flatten nested blocks into steps
     * This adds all nested statements to the top-level steps object
     * and sets their dependentStatements appropriately
     */
    public flattenNestedBlocks(ast: FunctionDefNode): void {
        const allStatements: StatementNode[] = [...ast.logic];

        // Process each top-level statement
        for (const stmt of ast.logic) {
            this.collectNestedStatements(stmt, allStatements);
        }

        // Update ast.logic with all statements (flattened)
        ast.logic = allStatements;
    }

    /**
     * Recursively collect nested statements
     * NOTE: Nesting is implicit (derived from expression analysis)
     * We do NOT add block dependencies to dependentStatements
     * Only explicit AFTER clauses become dependentStatements
     */
    private collectNestedStatements(stmt: StatementNode, allStatements: StatementNode[]): void {
        for (const [, nestedStmts] of stmt.nestedBlocks) {
            for (const nestedStmt of nestedStmts) {
                // Do NOT add implicit block dependency - nesting is derived from expressions
                // Only explicit AFTER clauses should become dependentStatements

                // Add to all statements
                allStatements.push(nestedStmt);

                // Recursively process this statement's nested blocks
                this.collectNestedStatements(nestedStmt, allStatements);
            }
        }

        // Clear nested blocks after flattening
        stmt.nestedBlocks.clear();
    }
}

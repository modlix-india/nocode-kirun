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
import { ExpressionHandler } from './ExpressionHandler';
import { SchemaTransformer } from './SchemaTransformer';

/**
 * AST to JSON Transformer
 * Converts parsed AST to FunctionDefinition JSON
 */
export class ASTToJSONTransformer {
    /**
     * Transform AST to FunctionDefinition JSON
     */
    public transform(ast: FunctionDefNode): any {
        return {
            name: ast.name,
            namespace: ast.namespace || '',
            version: 1,
            parameters: this.transformParameters(ast.parameters),
            events: this.transformEvents(ast.events),
            steps: this.transformSteps(ast.logic),
            stepGroups: {},
            parts: [],
        };
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
     */
    private transformStatement(stmt: StatementNode): any {
        const json: any = {
            statementName: stmt.statementName,
            namespace: stmt.functionCall.namespace,
            name: stmt.functionCall.name,
            parameterMap: this.transformParameterMap(stmt.functionCall.argumentsMap),
            dependentStatements: this.createDependentStatementsMap(stmt),
            executeIftrue: this.createExecuteIfMap(stmt.executeIfSteps),
            position: null,
            comment: '',
            description: '',
            override: false,
        };

        return json;
    }

    /**
     * Create dependentStatements map from AFTER clause and nested blocks
     */
    private createDependentStatementsMap(stmt: StatementNode): any {
        const result: any = {};

        // Add explicit AFTER dependencies
        for (const stepRef of stmt.afterSteps) {
            result[stepRef] = false; // false = must complete before this step
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
     */
    private transformParameterMap(argsMap: Map<string, ArgumentNode>): any {
        const result: any = {};

        for (const [paramName, arg] of argsMap) {
            result[paramName] = {};
            const paramRef = this.transformArgument(arg);
            result[paramName][paramRef.key] = paramRef;
        }

        return result;
    }

    /**
     * Transform single argument to ParameterReference
     */
    private transformArgument(arg: ArgumentNode): any {
        const key = this.generateUUID();

        if (arg.value instanceof ExpressionNode) {
            return {
                key,
                type: 'EXPRESSION',
                expression: arg.value.expressionText,
                value: undefined,
                order: 0,
            };
        } else if (arg.value instanceof ComplexValueNode) {
            return {
                key,
                type: 'VALUE',
                value: arg.value.value,
                expression: undefined,
                order: 0,
            };
        } else if (arg.value instanceof SchemaLiteralNode) {
            // Schema literal with optional default value
            const schema = SchemaTransformer.transform(arg.value.schema.schemaSpec);

            if (arg.value.defaultValue) {
                // Has default value - evaluate it
                return {
                    key,
                    type: 'VALUE',
                    value: this.evaluateDefaultValue(arg.value.defaultValue, schema),
                    expression: undefined,
                    order: 0,
                };
            } else {
                // No default value - just pass the schema
                return {
                    key,
                    type: 'VALUE',
                    value: schema,
                    expression: undefined,
                    order: 0,
                };
            }
        }

        // Fallback
        return {
            key,
            type: 'VALUE',
            value: null,
            expression: undefined,
            order: 0,
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
            this.collectNestedStatements(stmt, stmt.statementName, allStatements);
        }

        // Update ast.logic with all statements (flattened)
        ast.logic = allStatements;
    }

    /**
     * Recursively collect nested statements
     */
    private collectNestedStatements(
        stmt: StatementNode,
        parentName: string,
        allStatements: StatementNode[],
    ): void {
        for (const [blockName, nestedStmts] of stmt.nestedBlocks) {
            for (const nestedStmt of nestedStmts) {
                // Add dependency on parent block
                const dependencyKey = `Steps.${parentName}.${blockName}`;
                nestedStmt.afterSteps.push(dependencyKey);

                // Add to all statements
                allStatements.push(nestedStmt);

                // Recursively process this statement's nested blocks
                this.collectNestedStatements(nestedStmt, nestedStmt.statementName, allStatements);
            }
        }

        // Clear nested blocks after flattening
        stmt.nestedBlocks.clear();
    }
}

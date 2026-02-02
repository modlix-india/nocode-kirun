import { DSLToken, DSLTokenType } from '../lexer/DSLToken';
import { isBlockName } from '../lexer/Keywords';
import {
    ArgumentNode,
    ComplexValueNode,
    EventDeclNode,
    ExpressionNode,
    FunctionCallNode,
    FunctionDefNode,
    ParameterDeclNode,
    SchemaLiteralNode,
    SchemaNode,
    StatementNode,
} from './ast';
import { DSLParserError } from './DSLParserError';

/**
 * DSL Parser - Recursive descent parser
 * Converts tokens into AST
 */
export class DSLParser {
    private tokens: DSLToken[];
    private current: number = 0;

    constructor(tokens: DSLToken[]) {
        this.tokens = tokens;
    }

    /**
     * Parse tokens into AST
     */
    public parse(): FunctionDefNode {
        return this.parseFunctionDefinition();
    }

    /**
     * Parse function definition (top-level)
     */
    private parseFunctionDefinition(): FunctionDefNode {
        const startToken = this.expect(DSLTokenType.KEYWORD, 'FUNCTION');
        const name = this.expectIdentifier();

        const namespace = this.parseNamespaceDecl();
        const parameters = this.parseParametersDecl();
        const events = this.parseEventsDecl();

        this.expect(DSLTokenType.KEYWORD, 'LOGIC');
        const logic = this.parseLogicBlock();

        return new FunctionDefNode(name, namespace, parameters, events, logic, startToken.location);
    }

    /**
     * Parse optional namespace declaration
     */
    private parseNamespaceDecl(): string | undefined {
        if (this.match(DSLTokenType.KEYWORD, 'NAMESPACE')) {
            this.advance(); // consume NAMESPACE
            return this.expectDottedIdentifier();
        }
        return undefined;
    }

    /**
     * Parse optional parameters declaration
     */
    private parseParametersDecl(): ParameterDeclNode[] {
        if (!this.match(DSLTokenType.KEYWORD, 'PARAMETERS')) {
            return [];
        }

        this.advance(); // consume PARAMETERS

        const parameters: ParameterDeclNode[] = [];

        // Parse parameters until we hit EVENTS, LOGIC, or EOF
        while (
            !this.match(DSLTokenType.KEYWORD, 'EVENTS') &&
            !this.match(DSLTokenType.KEYWORD, 'LOGIC') &&
            !this.match(DSLTokenType.EOF)
        ) {
            parameters.push(this.parseParameterDecl());
        }

        return parameters;
    }

    /**
     * Parse single parameter declaration
     */
    private parseParameterDecl(): ParameterDeclNode {
        const nameToken = this.peek();
        const name = this.expectIdentifier();
        this.expect(DSLTokenType.KEYWORD, 'AS');
        const schema = this.parseSchemaSpec();

        return new ParameterDeclNode(name, schema, nameToken.location);
    }

    /**
     * Parse optional events declaration
     */
    private parseEventsDecl(): EventDeclNode[] {
        if (!this.match(DSLTokenType.KEYWORD, 'EVENTS')) {
            return [];
        }

        this.advance(); // consume EVENTS

        const events: EventDeclNode[] = [];

        // Parse events until we hit LOGIC or EOF
        while (!this.match(DSLTokenType.KEYWORD, 'LOGIC') && !this.match(DSLTokenType.EOF)) {
            events.push(this.parseEventDecl());
        }

        return events;
    }

    /**
     * Parse single event declaration
     */
    private parseEventDecl(): EventDeclNode {
        const nameToken = this.peek();
        const name = this.expectIdentifier();

        const parameters: ParameterDeclNode[] = [];

        // Parse event parameters until we hit another identifier (next event) or LOGIC
        while (
            !this.match(DSLTokenType.KEYWORD, 'LOGIC') &&
            !this.match(DSLTokenType.EOF) &&
            this.peek().type === DSLTokenType.IDENTIFIER
        ) {
            // Check if this looks like a parameter declaration (identifier followed by AS)
            if (this.peekAhead(1)?.is(DSLTokenType.KEYWORD, 'AS')) {
                parameters.push(this.parseParameterDecl());
            } else {
                // This is the next event name, break
                break;
            }
        }

        return new EventDeclNode(name, parameters, nameToken.location);
    }

    /**
     * Parse logic block (list of statements)
     */
    private parseLogicBlock(): StatementNode[] {
        const statements: StatementNode[] = [];

        while (!this.match(DSLTokenType.EOF)) {
            statements.push(this.parseStatement());
        }

        return statements;
    }

    /**
     * Parse single statement
     */
    private parseStatement(): StatementNode {
        const nameToken = this.peek();

        // Check if this is an anonymous statement (starts with :)
        let statementName: string;
        if (this.match(DSLTokenType.COLON)) {
            // Anonymous statement - generate a name
            statementName = `_anonymous_${this.current}`;
        } else {
            statementName = this.expectIdentifier();
            this.expect(DSLTokenType.COLON);
        }

        const functionCall = this.parseFunctionCall();

        // Parse optional AFTER clause
        const afterSteps: string[] = [];
        if (this.match(DSLTokenType.KEYWORD, 'AFTER')) {
            this.advance(); // consume AFTER
            do {
                afterSteps.push(this.expectStepReference());
                if (this.match(DSLTokenType.COMMA)) {
                    this.advance(); // consume comma
                } else {
                    break;
                }
            } while (true);
        }

        // Parse optional IF clause (executeIftrue)
        const executeIfSteps: string[] = [];
        if (this.match(DSLTokenType.KEYWORD, 'IF')) {
            this.advance(); // consume IF
            do {
                executeIfSteps.push(this.expectStepReference());
                if (this.match(DSLTokenType.COMMA)) {
                    this.advance(); // consume comma
                } else {
                    break;
                }
            } while (true);
        }

        // Parse nested blocks
        const nestedBlocks = this.parseNestedBlocks();

        return new StatementNode(
            statementName,
            functionCall,
            afterSteps,
            executeIfSteps,
            nestedBlocks,
            nameToken.location,
        );
    }

    /**
     * Parse nested blocks (iteration, true, false, output, error)
     */
    private parseNestedBlocks(): Map<string, StatementNode[]> {
        const blocks = new Map<string, StatementNode[]>();

        while (!this.match(DSLTokenType.EOF)) {
            const token = this.peek();

            // Check if this is a block name
            if (token.type === DSLTokenType.IDENTIFIER && isBlockName(token.value)) {
                const blockName = this.advance().value;
                const statements: StatementNode[] = [];

                // Parse statements in this block until we hit another block name or identifier with colon
                while (!this.match(DSLTokenType.EOF)) {
                    const next = this.peek();

                    // Check if next token is a block name or top-level statement
                    if (next.type === DSLTokenType.IDENTIFIER) {
                        const nextNext = this.peekAhead(1);
                        if (isBlockName(next.value)) {
                            // Another block
                            break;
                        }
                        if (!nextNext || nextNext.type !== DSLTokenType.COLON) {
                            // Not a statement, might be end of block
                            break;
                        }
                    }

                    if (next.type === DSLTokenType.COLON) {
                        // Anonymous statement in block
                        statements.push(this.parseStatement());
                    } else if (next.type === DSLTokenType.IDENTIFIER) {
                        // Named statement in block
                        statements.push(this.parseStatement());
                    } else {
                        break;
                    }
                }

                blocks.set(blockName, statements);
            } else {
                // Not a block name, we're done
                break;
            }
        }

        return blocks;
    }

    /**
     * Parse function call
     */
    private parseFunctionCall(): FunctionCallNode {
        const startToken = this.peek();
        const fullName = this.expectDottedIdentifier();

        // Split namespace and name
        const parts = fullName.split('.');
        const name = parts.pop()!;
        const namespace = parts.join('.');

        this.expect(DSLTokenType.LEFT_PAREN);
        const argumentsMap = this.parseArgumentList();
        this.expect(DSLTokenType.RIGHT_PAREN);

        return new FunctionCallNode(namespace, name, argumentsMap, startToken.location);
    }

    /**
     * Parse argument list
     */
    private parseArgumentList(): Map<string, ArgumentNode> {
        const argumentsMap = new Map<string, ArgumentNode>();

        if (this.match(DSLTokenType.RIGHT_PAREN)) {
            // Empty argument list
            return argumentsMap;
        }

        do {
            const argToken = this.peek();
            const paramName = this.expectIdentifier();
            this.expect(DSLTokenType.EQUALS);
            const value = this.parseArgumentValue();

            argumentsMap.set(paramName, new ArgumentNode(paramName, value, argToken.location));

            if (this.match(DSLTokenType.COMMA)) {
                this.advance(); // consume comma
            } else {
                break;
            }
        } while (!this.match(DSLTokenType.RIGHT_PAREN) && !this.match(DSLTokenType.EOF));

        return argumentsMap;
    }

    /**
     * Parse argument value (expression, complex value, or schema literal)
     */
    private parseArgumentValue(): ExpressionNode | ComplexValueNode | SchemaLiteralNode {
        const token = this.peek();

        // Check for schema literal: (SchemaSpec) WITH DEFAULT VALUE expr
        if (this.match(DSLTokenType.LEFT_PAREN)) {
            // Could be schema literal or just a parenthesized expression
            // Try to parse as schema literal first
            const savedPos = this.current;
            try {
                return this.parseSchemaLiteral();
            } catch {
                // Not a schema literal, reset and parse as expression
                this.current = savedPos;
            }
        }

        // Check for complex value (object or array)
        if (this.match(DSLTokenType.LEFT_BRACE)) {
            return this.parseComplexValue();
        }

        if (this.match(DSLTokenType.LEFT_BRACKET)) {
            return this.parseComplexValue();
        }

        // Otherwise, parse as expression
        return this.parseExpression();
    }

    /**
     * Parse expression (everything until comma, paren, or newline)
     */
    private parseExpression(): ExpressionNode {
        const startToken = this.peek();
        let expressionText = '';

        // Collect tokens until we hit a delimiter
        let depth = 0;
        while (!this.match(DSLTokenType.EOF)) {
            const token = this.peek();

            // Track parentheses/bracket depth
            if (
                token.type === DSLTokenType.LEFT_PAREN ||
                token.type === DSLTokenType.LEFT_BRACKET ||
                token.type === DSLTokenType.LEFT_BRACE
            ) {
                depth++;
            } else if (
                token.type === DSLTokenType.RIGHT_PAREN ||
                token.type === DSLTokenType.RIGHT_BRACKET ||
                token.type === DSLTokenType.RIGHT_BRACE
            ) {
                if (depth === 0) {
                    // End of expression
                    break;
                }
                depth--;
            } else if (token.type === DSLTokenType.COMMA && depth === 0) {
                // End of expression
                break;
            }

            expressionText += token.value;
            if (token.type !== DSLTokenType.DOT) {
                expressionText += ' '; // Add space between tokens
            }
            this.advance();
        }

        return new ExpressionNode(expressionText.trim(), startToken.location);
    }

    /**
     * Parse schema specification
     */
    private parseSchemaSpec(): SchemaNode {
        const startToken = this.peek();

        // Check for complex JSON schema
        if (this.match(DSLTokenType.LEFT_BRACE)) {
            const value = this.parseComplexValue();
            return new SchemaNode(value.value as object, startToken.location);
        }

        // Parse simple schema syntax
        const schemaText = this.parseSimpleSchema();
        return new SchemaNode(schemaText, startToken.location);
    }

    /**
     * Parse simple schema syntax (INTEGER, ARRAY OF INTEGER, etc.)
     */
    private parseSimpleSchema(): string {
        let schemaText = '';

        // Handle ARRAY OF recursively
        if (this.match(DSLTokenType.KEYWORD, 'ARRAY')) {
            this.advance(); // consume ARRAY
            this.expect(DSLTokenType.KEYWORD, 'OF');
            const innerSchema = this.parseSimpleSchema();
            return `ARRAY OF ${innerSchema}`;
        }

        // Handle OBJECT
        if (this.match(DSLTokenType.KEYWORD, 'OBJECT')) {
            return this.advance().value;
        }

        // Handle primitive types
        const token = this.peek();
        if (token.type === DSLTokenType.KEYWORD) {
            return this.advance().value;
        }

        throw new DSLParserError(
            'Expected schema type',
            token.location,
            ['INTEGER', 'LONG', 'FLOAT', 'DOUBLE', 'STRING', 'BOOLEAN', 'NULL', 'ANY', 'ARRAY', 'OBJECT'],
            token,
        );
    }

    /**
     * Parse schema literal: (SchemaSpec) WITH DEFAULT VALUE expr
     */
    private parseSchemaLiteral(): SchemaLiteralNode {
        const startToken = this.peek();
        this.expect(DSLTokenType.LEFT_PAREN);
        const schema = this.parseSchemaSpec();
        this.expect(DSLTokenType.RIGHT_PAREN);

        let defaultValue: ExpressionNode | undefined;

        if (this.match(DSLTokenType.KEYWORD, 'WITH')) {
            this.advance(); // consume WITH
            this.expect(DSLTokenType.KEYWORD, 'DEFAULT');
            this.expect(DSLTokenType.KEYWORD, 'VALUE');
            defaultValue = this.parseExpression();
        }

        return new SchemaLiteralNode(schema, defaultValue, startToken.location);
    }

    /**
     * Parse complex value (JSON object or array)
     */
    private parseComplexValue(): ComplexValueNode {
        const startToken = this.peek();

        if (this.match(DSLTokenType.LEFT_BRACE)) {
            return new ComplexValueNode(this.parseObject(), startToken.location);
        }

        if (this.match(DSLTokenType.LEFT_BRACKET)) {
            return new ComplexValueNode(this.parseArray(), startToken.location);
        }

        throw new DSLParserError('Expected { or [', startToken.location, ['{', '['], startToken);
    }

    /**
     * Parse JSON object
     */
    private parseObject(): object {
        this.expect(DSLTokenType.LEFT_BRACE);
        const obj: any = {};

        if (this.match(DSLTokenType.RIGHT_BRACE)) {
            this.advance();
            return obj;
        }

        do {
            // Parse key (can be identifier or string)
            let key: string;
            if (this.match(DSLTokenType.STRING)) {
                const strToken = this.advance();
                key = strToken.value.slice(1, -1); // Remove quotes
            } else {
                key = this.expectIdentifier();
            }

            this.expect(DSLTokenType.COLON);

            // Parse value
            obj[key] = this.parseJsonValue();

            if (this.match(DSLTokenType.COMMA)) {
                this.advance();
            } else {
                break;
            }
        } while (!this.match(DSLTokenType.RIGHT_BRACE) && !this.match(DSLTokenType.EOF));

        this.expect(DSLTokenType.RIGHT_BRACE);
        return obj;
    }

    /**
     * Parse JSON array
     */
    private parseArray(): any[] {
        this.expect(DSLTokenType.LEFT_BRACKET);
        const arr: any[] = [];

        if (this.match(DSLTokenType.RIGHT_BRACKET)) {
            this.advance();
            return arr;
        }

        do {
            arr.push(this.parseJsonValue());

            if (this.match(DSLTokenType.COMMA)) {
                this.advance();
            } else {
                break;
            }
        } while (!this.match(DSLTokenType.RIGHT_BRACKET) && !this.match(DSLTokenType.EOF));

        this.expect(DSLTokenType.RIGHT_BRACKET);
        return arr;
    }

    /**
     * Parse JSON value (string, number, boolean, null, object, array, or expression)
     */
    private parseJsonValue(): any {
        const token = this.peek();

        if (token.type === DSLTokenType.STRING) {
            const strToken = this.advance();
            return strToken.value.slice(1, -1); // Remove quotes
        }

        if (token.type === DSLTokenType.NUMBER) {
            const numToken = this.advance();
            return parseFloat(numToken.value);
        }

        if (token.type === DSLTokenType.BOOLEAN) {
            const boolToken = this.advance();
            return boolToken.value === 'true';
        }

        if (token.type === DSLTokenType.NULL) {
            this.advance();
            return null;
        }

        if (token.type === DSLTokenType.LEFT_BRACE) {
            return this.parseObject();
        }

        if (token.type === DSLTokenType.LEFT_BRACKET) {
            return this.parseArray();
        }

        // Try to parse as expression
        const expr = this.parseExpression();
        return { isExpression: true, value: expr.expressionText };
    }

    // ===== Helper Methods =====

    /**
     * Peek at current token
     */
    private peek(): DSLToken {
        return this.tokens[this.current];
    }

    /**
     * Peek ahead n tokens
     */
    private peekAhead(n: number): DSLToken | undefined {
        return this.tokens[this.current + n];
    }

    /**
     * Advance to next token and return current
     */
    private advance(): DSLToken {
        const token = this.tokens[this.current];
        this.current++;
        return token;
    }

    /**
     * Check if current token matches type and optional value
     */
    private match(type: DSLTokenType, value?: string): boolean {
        const token = this.peek();
        if (!token) return false;
        if (token.type !== type) return false;
        if (value !== undefined && token.value !== value) return false;
        return true;
    }

    /**
     * Expect a specific token type and value, throw error if not found
     */
    private expect(type: DSLTokenType, value?: string): DSLToken {
        const token = this.peek();

        if (!this.match(type, value)) {
            const expected = value ? `${type} (${value})` : type;
            throw new DSLParserError(`Expected ${expected}`, token.location, [expected], token);
        }

        return this.advance();
    }

    /**
     * Expect an identifier token
     */
    private expectIdentifier(): string {
        const token = this.expect(DSLTokenType.IDENTIFIER);
        return token.value;
    }

    /**
     * Expect a dotted identifier (e.g., System.Math.Add)
     */
    private expectDottedIdentifier(): string {
        let name = this.expectIdentifier();

        while (this.match(DSLTokenType.DOT)) {
            this.advance(); // consume dot
            name += '.' + this.expectIdentifier();
        }

        return name;
    }

    /**
     * Expect a step reference (e.g., Steps.create.output)
     */
    private expectStepReference(): string {
        return this.expectDottedIdentifier();
    }
}

import { DSLToken, DSLTokenType } from '../lexer/DSLToken';
// Block names are dynamic based on function signatures, not from a fixed set
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
    private originalInput: string;

    constructor(tokens: DSLToken[], originalInput: string = '') {
        this.tokens = tokens;
        this.originalInput = originalInput;
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
     * @param minBlockColumn - minimum column for blocks to belong to this statement.
     *                         Blocks at smaller column positions belong to an ancestor.
     */
    private parseStatement(minBlockColumn: number = 0): StatementNode {
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

        // Parse optional AFTER clause (statement dependencies)
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

        // Parse optional trailing comment
        let comment = '';
        if (this.match(DSLTokenType.COMMENT)) {
            const commentToken = this.advance();
            // Extract comment text, removing /* and */ markers
            comment = commentToken.value
                .replace(/^\/\*\s*/, '')
                .replace(/\s*\*\/$/, '')
                .trim();
        }

        // Parse nested blocks, but only those indented more than minBlockColumn
        const nestedBlocks = this.parseNestedBlocks(minBlockColumn);

        return new StatementNode(
            statementName,
            functionCall,
            afterSteps,
            executeIfSteps,
            nestedBlocks,
            nameToken.location,
            comment,
        );
    }

    /**
     * Parse nested blocks (iteration, true, false, output, error)
     * @param minBlockColumn - minimum column for blocks to belong to this statement.
     *                         Blocks at smaller column positions belong to an ancestor.
     */
    private parseNestedBlocks(minBlockColumn: number = 0): Map<string, StatementNode[]> {
        const blocks = new Map<string, StatementNode[]>();

        while (!this.match(DSLTokenType.EOF)) {
            const token = this.peek();

            // Check if this is a block name
            // Block names can be IDENTIFIER (iteration, output, error), BOOLEAN (true, false),
            // or KEYWORD (true, false are also keywords in the lexer)
            // BUT if followed by colon, it's a statement name, not a block name
            const isBlockToken = token.type === DSLTokenType.IDENTIFIER ||
                                 token.type === DSLTokenType.BOOLEAN ||
                                 token.type === DSLTokenType.KEYWORD;
            const nextToken = this.peekAhead(1);
            const isFollowedByColon = nextToken && nextToken.type === DSLTokenType.COLON;

            // Check if block is at or below our minimum column threshold
            // Blocks at smaller columns belong to an ancestor statement
            const blockColumn = token.location.column;
            if (blockColumn <= minBlockColumn) {
                // This block belongs to an ancestor, stop parsing
                break;
            }

            // Accept ANY identifier (not followed by colon) as a potential block name
            // Block names are dynamic based on function signatures, not a fixed set
            if (isBlockToken && !isFollowedByColon) {
                const blockName = this.advance().value;
                const statements: StatementNode[] = [];

                // Record this block's column as the threshold for nested statements
                // Nested statements' blocks must be indented MORE than this block
                const thisBlockColumn = blockColumn;

                // Parse statements in this block until we hit another block at same level
                // or a statement at smaller indentation
                while (!this.match(DSLTokenType.EOF)) {
                    const next = this.peek();

                    // Check if next token is a block name or statement
                    const isNextBlockToken = next.type === DSLTokenType.IDENTIFIER ||
                                             next.type === DSLTokenType.BOOLEAN ||
                                             next.type === DSLTokenType.KEYWORD;
                    if (isNextBlockToken) {
                        const nextNext = this.peekAhead(1);
                        const nextIsFollowedByColon = nextNext && nextNext.type === DSLTokenType.COLON;

                        if (!nextIsFollowedByColon) {
                            // Not followed by colon = potential block name
                            // Check if this block is at the same or lesser indentation
                            // If so, it's a sibling block, stop parsing this block
                            if (next.location.column <= thisBlockColumn) {
                                break;
                            }
                            // Otherwise it's a nested block, let the statement handle it
                        } else {
                            // Followed by colon = statement
                            // Check its indentation
                            if (next.location.column <= thisBlockColumn) {
                                break;
                            }
                            // Statement is more indented, continue parsing
                        }
                    }

                    if (next.type === DSLTokenType.COLON) {
                        // Anonymous statement in block
                        // Pass the block column as min threshold for nested blocks
                        statements.push(this.parseStatement(thisBlockColumn));
                    } else if (next.type === DSLTokenType.IDENTIFIER ||
                               next.type === DSLTokenType.BOOLEAN ||
                               next.type === DSLTokenType.KEYWORD) {
                        // Named statement in block (can use any identifier-like token as name)
                        // Pass the block column as min threshold for nested blocks
                        statements.push(this.parseStatement(thisBlockColumn));
                    } else {
                        break;
                    }
                }

                blocks.set(blockName, statements);
            } else {
                // Followed by colon = statement name, not block name
                // We're done parsing blocks
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
     * Supports multi-value parameters by repeating the parameter name:
     *   param = val1, param = val2, param = val3
     * Order is preserved based on occurrence in the argument list.
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

            // Check if this parameter already exists (multi-value)
            const existing = argumentsMap.get(paramName);
            if (existing) {
                // Add to existing ArgumentNode's values array
                existing.values.push(value);
            } else {
                // Create new ArgumentNode
                argumentsMap.set(paramName, new ArgumentNode(paramName, value, argToken.location));
            }

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

        // Check for backtick string - always treated as EXPRESSION
        // Backticks are used to wrap expressions that might look like literals
        // e.g., `false` is expression "false", `"hello"` is expression "hello"
        if (this.match(DSLTokenType.BACKTICK_STRING)) {
            const btToken = this.advance();
            // The content between backticks is the expression
            return new ExpressionNode(btToken.value, btToken.location);
        }

        // Check for string literal
        // Single-quoted strings are treated as EXPRESSION (KIRun expression parser evaluates them)
        // Double-quoted strings are treated as VALUE (literal string value)
        if (this.match(DSLTokenType.STRING)) {
            const strToken = this.peek();
            const quoteChar = strToken.value[0];

            if (quoteChar === "'") {
                // Single-quoted string could be part of a larger expression (e.g., 'a' + 'b')
                // Check if followed by an operator - if so, parse as full expression
                const nextToken = this.peekAhead(1);
                if (nextToken && nextToken.type === DSLTokenType.OPERATOR) {
                    // Fall through to parseExpression to handle full expression
                    return this.parseExpression();
                }
                // Standalone single-quoted string → EXPRESSION
                this.advance();
                return new ExpressionNode(strToken.value, strToken.location);
            } else {
                // Double-quoted string → VALUE (remove quotes and unescape JSON escape sequences)
                this.advance();
                const strValue = this.unescapeJsonString(strToken.value.slice(1, -1));
                return new ComplexValueNode(strValue, strToken.location);
            }
        }

        // Check for number literal - treat as VALUE unless followed by operator
        if (this.match(DSLTokenType.NUMBER)) {
            // Check if followed by an operator or = (making it an expression like "0 = undefined" or "3 * 2")
            const nextToken = this.peekAhead(1);
            if (nextToken && (nextToken.type === DSLTokenType.OPERATOR || nextToken.type === DSLTokenType.EQUALS)) {
                // Fall through to parseExpression to handle full expression
                return this.parseExpression();
            }
            // Standalone number → VALUE
            const numToken = this.advance();
            return new ComplexValueNode(parseFloat(numToken.value), numToken.location);
        }

        // Check for boolean literal - treat as VALUE
        if (this.match(DSLTokenType.BOOLEAN) ||
            (this.match(DSLTokenType.KEYWORD) && (token.value === 'true' || token.value === 'false'))) {
            const boolToken = this.advance();
            return new ComplexValueNode(boolToken.value === 'true', boolToken.location);
        }

        // Check for null literal - treat as VALUE
        if (this.match(DSLTokenType.NULL) ||
            (this.match(DSLTokenType.KEYWORD) && token.value === 'null')) {
            const nullToken = this.advance();
            return new ComplexValueNode(null, nullToken.location);
        }

        // Check for undefined literal - treat as VALUE with undefined
        if (this.match(DSLTokenType.IDENTIFIER) && token.value === 'undefined') {
            const undefinedToken = this.advance();
            return new ComplexValueNode(undefined, undefinedToken.location);
        }

        // Check for complex value (object or array)
        // But NOT if it's {{ which is a KIRun expression reference
        if (this.match(DSLTokenType.LEFT_BRACE)) {
            // Check if next token is also LEFT_BRACE (making it {{ expression)
            const nextToken = this.peekAhead(1);
            if (nextToken && nextToken.type !== DSLTokenType.LEFT_BRACE) {
                // Single { means JSON object
                return this.parseComplexValue();
            }
            // {{ means expression, fall through to parseExpression
        }

        if (this.match(DSLTokenType.LEFT_BRACKET)) {
            return this.parseComplexValue();
        }

        // Otherwise, parse as expression
        return this.parseExpression();
    }

    /**
     * Parse expression (everything until comma, paren, or newline)
     * If originalInput is available, extract exact text to preserve whitespace
     */
    private parseExpression(): ExpressionNode {
        const startToken = this.peek();
        const startPos = startToken.location.startPos;

        // Check if we're already at a delimiter (empty expression)
        if (
            startToken.type === DSLTokenType.COMMA ||
            startToken.type === DSLTokenType.RIGHT_PAREN ||
            startToken.type === DSLTokenType.RIGHT_BRACKET ||
            startToken.type === DSLTokenType.RIGHT_BRACE
        ) {
            // Empty expression
            return new ExpressionNode('', startToken.location);
        }

        // Collect tokens until we hit a delimiter
        let depth = 0;
        let lastToken = startToken;
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

            lastToken = token;
            this.advance();
        }

        // If we have the original input, extract exact text preserving whitespace
        // Use the delimiter's start position to include any trailing whitespace
        if (this.originalInput && startPos >= 0) {
            const delimiterToken = this.peek();
            const endPos = delimiterToken.location.startPos;
            if (endPos > startPos) {
                const exactText = this.originalInput.substring(startPos, endPos);
                // Only trim leading whitespace, preserve trailing whitespace
                return new ExpressionNode(exactText.trimStart(), startToken.location);
            }
        }
        // Fallback using last token position
        if (this.originalInput && startPos >= 0 && lastToken.location.endPos > startPos) {
            const exactText = this.originalInput.substring(startPos, lastToken.location.endPos);
            return new ExpressionNode(exactText.trimStart(), startToken.location);
        }

        // Fallback: reconstruct from tokens (shouldn't normally happen)
        // This path is only used if originalInput is not provided
        return this.reconstructExpressionFromTokens(startToken, lastToken);
    }

    /**
     * Reconstruct expression text from tokens (fallback when originalInput unavailable)
     */
    private reconstructExpressionFromTokens(startToken: DSLToken, endToken: DSLToken): ExpressionNode {
        // Re-parse the tokens to reconstruct the expression
        // This is a fallback and may not preserve exact whitespace
        const savedPos = this.current;

        // Find the start token position
        let startIdx = 0;
        for (let i = 0; i < this.tokens.length; i++) {
            if (this.tokens[i].location.startPos === startToken.location.startPos) {
                startIdx = i;
                break;
            }
        }

        let expressionText = '';
        let depth = 0;
        for (let i = startIdx; i < this.tokens.length; i++) {
            const token = this.tokens[i];
            if (token.location.startPos > endToken.location.startPos) break;

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
                depth--;
            }

            expressionText += token.value;

            // Add space between tokens if needed
            const nextToken = this.tokens[i + 1];
            if (nextToken && nextToken.location.startPos <= endToken.location.startPos) {
                if (this.needsSpaceBetween(token, nextToken, depth)) {
                    expressionText += ' ';
                }
            }
        }

        // Preserve trailing whitespace (may be significant in expressions)
        return new ExpressionNode(expressionText.trimStart(), startToken.location);
    }

    /**
     * Determine if a space is needed between two tokens in an expression
     * @param depth - current brace nesting depth (0 = top level)
     */
    private needsSpaceBetween(current: DSLToken, next: DSLToken, depth: number): boolean {
        // No space between consecutive braces (for {{ and }})
        if (current.type === DSLTokenType.LEFT_BRACE && next.type === DSLTokenType.LEFT_BRACE) {
            return false;
        }
        if (current.type === DSLTokenType.RIGHT_BRACE && next.type === DSLTokenType.RIGHT_BRACE) {
            return false;
        }

        // No space around dots (for property access like Context.a)
        if (current.type === DSLTokenType.DOT || next.type === DSLTokenType.DOT) {
            return false;
        }

        // No space after [ or before ]
        if (current.type === DSLTokenType.LEFT_BRACKET || next.type === DSLTokenType.RIGHT_BRACKET) {
            return false;
        }

        // No space after ( or before )
        if (current.type === DSLTokenType.LEFT_PAREN || next.type === DSLTokenType.RIGHT_PAREN) {
            return false;
        }

        // No space after { or before }
        if (current.type === DSLTokenType.LEFT_BRACE || next.type === DSLTokenType.RIGHT_BRACE) {
            return false;
        }

        // No space around ?? nullish coalescing operator
        if ((current.type === DSLTokenType.OPERATOR && current.value === '??') ||
            (next.type === DSLTokenType.OPERATOR && next.value === '??')) {
            return false;
        }

        // No space around - when between identifiers (for hyphenated names like content-type)
        if (current.type === DSLTokenType.OPERATOR && current.value === '-') {
            return false;
        }
        if (next.type === DSLTokenType.OPERATOR && next.value === '-') {
            return false;
        }

        // Inside nested braces (depth > 0), don't add spaces around arithmetic operators
        // This preserves expressions like {{1+x}} without spaces
        if (depth > 0) {
            const arithOps = ['+', '*', '/', '%'];
            if (current.type === DSLTokenType.OPERATOR && arithOps.includes(current.value)) {
                return false;
            }
            if (next.type === DSLTokenType.OPERATOR && arithOps.includes(next.value)) {
                return false;
            }
        }

        // Space between identifiers/keywords/booleans (to separate words like "false and")
        const isCurrentWord = current.type === DSLTokenType.IDENTIFIER ||
                              current.type === DSLTokenType.KEYWORD ||
                              current.type === DSLTokenType.BOOLEAN ||
                              current.type === DSLTokenType.NUMBER;
        const isNextWord = next.type === DSLTokenType.IDENTIFIER ||
                          next.type === DSLTokenType.KEYWORD ||
                          next.type === DSLTokenType.BOOLEAN ||
                          next.type === DSLTokenType.NUMBER;

        if (isCurrentWord && isNextWord) {
            return true;
        }

        // Space around = operator for comparisons
        if (current.type === DSLTokenType.EQUALS || next.type === DSLTokenType.EQUALS) {
            return true;
        }

        // Space around comparison and arithmetic operators at top level
        if (current.type === DSLTokenType.OPERATOR || next.type === DSLTokenType.OPERATOR) {
            return true;
        }

        // No space by default
        return false;
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
            // Remove quotes and unescape JSON escape sequences
            return this.unescapeJsonString(strToken.value.slice(1, -1));
        }

        if (token.type === DSLTokenType.NUMBER) {
            const numToken = this.advance();
            return parseFloat(numToken.value);
        }

        // Boolean values can be BOOLEAN token or KEYWORD token (true/false are also keywords)
        if (token.type === DSLTokenType.BOOLEAN ||
            (token.type === DSLTokenType.KEYWORD && (token.value === 'true' || token.value === 'false'))) {
            const boolToken = this.advance();
            return boolToken.value === 'true';
        }

        // Null can be NULL token or KEYWORD token
        if (token.type === DSLTokenType.NULL ||
            (token.type === DSLTokenType.KEYWORD && token.value === 'null')) {
            this.advance();
            return null;
        }

        // Undefined as identifier
        if (token.type === DSLTokenType.IDENTIFIER && token.value === 'undefined') {
            this.advance();
            return undefined;
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
     * Also accepts BOOLEAN tokens (true/false) as identifiers since they're used
     * as nested block names in if/loop statements (e.g., if.true, if.false)
     */
    private expectIdentifier(): string {
        const token = this.peek();
        if (token.type === DSLTokenType.IDENTIFIER) {
            return this.advance().value;
        }
        if (token.type === DSLTokenType.BOOLEAN) {
            // Accept true/false as identifiers for nested block names
            return this.advance().value;
        }
        if (token.type === DSLTokenType.KEYWORD) {
            // Accept keywords as identifiers (e.g., nested block names like "iteration")
            return this.advance().value;
        }
        // Fall back to expect for proper error message
        const expected = this.expect(DSLTokenType.IDENTIFIER);
        return expected.value;
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

    /**
     * Unescape JSON string escape sequences
     * Handles: \n, \r, \t, \\, \", \/, \b, \f, \uXXXX
     */
    private unescapeJsonString(str: string): string {
        let result = '';
        let i = 0;
        while (i < str.length) {
            if (str[i] === '\\' && i + 1 < str.length) {
                const next = str[i + 1];
                switch (next) {
                    case 'n':
                        result += '\n';
                        i += 2;
                        break;
                    case 'r':
                        result += '\r';
                        i += 2;
                        break;
                    case 't':
                        result += '\t';
                        i += 2;
                        break;
                    case 'b':
                        result += '\b';
                        i += 2;
                        break;
                    case 'f':
                        result += '\f';
                        i += 2;
                        break;
                    case '\\':
                        result += '\\';
                        i += 2;
                        break;
                    case '"':
                        result += '"';
                        i += 2;
                        break;
                    case '/':
                        result += '/';
                        i += 2;
                        break;
                    case 'u':
                        // Unicode escape \uXXXX
                        if (i + 5 < str.length) {
                            const hex = str.substring(i + 2, i + 6);
                            if (/^[0-9a-fA-F]{4}$/.test(hex)) {
                                result += String.fromCharCode(parseInt(hex, 16));
                                i += 6;
                                break;
                            }
                        }
                        // Invalid unicode escape, keep as-is
                        result += str[i];
                        i++;
                        break;
                    default:
                        // Unknown escape, keep backslash and character
                        result += str[i];
                        i++;
                }
            } else {
                result += str[i];
                i++;
            }
        }
        return result;
    }
}

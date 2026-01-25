import { ExpressionEvaluationException } from './exception/ExpressionEvaluationException';
import { ExpressionToken } from './ExpressionToken';
import { ExpressionTokenValue } from './ExpressionTokenValue';
import { Expression } from './Expression';
import { Operation } from './Operation';
import { ExpressionLexer, Token, TokenType } from './ExpressionLexer';

export class ExpressionParser {
    private lexer: ExpressionLexer;
    private currentToken: Token | null = null;
    private previousTokenValue: Token | null = null;

    constructor(expression: string) {
        this.lexer = new ExpressionLexer(expression);
        this.currentToken = this.lexer.nextToken();
    }

    public parse(): Expression {
        if (!this.currentToken) {
            throw new ExpressionEvaluationException('', 'Empty expression');
        }

        const expr = this.parseExpression();
        
        // Ensure we consumed all tokens
        if (this.currentToken && this.currentToken.type !== TokenType.EOF) {
            throw new ExpressionEvaluationException(
                this.lexer.getPosition().toString(),
                `Unexpected token: ${this.currentToken.value} at position ${this.currentToken.startPos}`,
            );
        }

        return expr;
    }

    private parseExpression(): Expression {
        return this.parseTernary();
    }

    // Ternary: condition ? trueExpr : falseExpr (precedence 12)
    // Note: Ternary requires 3 tokens, so we use a special constructor pattern
    private parseTernary(): Expression {
        let expr = this.parseLogicalOr();

        if (this.matchToken(TokenType.QUESTION)) {
            const trueExpr = this.parseTernary();
            this.expectToken(TokenType.COLON);
            const falseExpr = this.parseTernary();
            // Create ternary expression with all 3 tokens: condition, trueExpr, falseExpr
            return Expression.createTernary(expr, trueExpr, falseExpr);
        }

        return expr;
    }

    // Logical OR: expr or expr (precedence 11)
    private parseLogicalOr(): Expression {
        let expr = this.parseLogicalAnd();

        while (this.matchOperator('or')) {
            const right = this.parseLogicalAnd();
            expr = new Expression('', expr, right, Operation.OR);
        }

        return expr;
    }

    // Logical AND: expr and expr (precedence 10)
    private parseLogicalAnd(): Expression {
        let expr = this.parseLogicalNot();

        while (this.matchOperator('and')) {
            const right = this.parseLogicalNot();
            expr = new Expression('', expr, right, Operation.AND);
        }

        return expr;
    }

    // Logical NOT: not expr (precedence 10, but unary)
    private parseLogicalNot(): Expression {
        if (this.matchOperator('not')) {
            const expr = this.parseLogicalNot(); // Right-associative for unary
            return new Expression('', expr, undefined, Operation.UNARY_LOGICAL_NOT);
        }

        return this.parseComparison();
    }

    // Comparison: <, <=, >, >=, =, != (precedence 5-6)
    private parseComparison(): Expression {
        let expr = this.parseBitwiseOr();

        while (true) {
            let op: Operation | null = null;

            if (this.matchOperator('<')) {
                op = Operation.LESS_THAN;
            } else if (this.matchOperator('<=')) {
                op = Operation.LESS_THAN_EQUAL;
            } else if (this.matchOperator('>')) {
                op = Operation.GREATER_THAN;
            } else if (this.matchOperator('>=')) {
                op = Operation.GREATER_THAN_EQUAL;
            } else if (this.matchOperator('=')) {
                op = Operation.EQUAL;
            } else if (this.matchOperator('!=')) {
                op = Operation.NOT_EQUAL;
            } else {
                break;
            }

            const right = this.parseBitwiseOr();
            expr = new Expression('', expr, right, op);
        }

        return expr;
    }

    // Bitwise OR: | (precedence 9)
    private parseBitwiseOr(): Expression {
        let expr = this.parseBitwiseXor();

        while (this.matchOperator('|')) {
            const right = this.parseBitwiseXor();
            expr = new Expression('', expr, right, Operation.BITWISE_OR);
        }

        return expr;
    }

    // Bitwise XOR: ^ (precedence 8)
    private parseBitwiseXor(): Expression {
        let expr = this.parseBitwiseAnd();

        while (this.matchOperator('^')) {
            const right = this.parseBitwiseAnd();
            expr = new Expression('', expr, right, Operation.BITWISE_XOR);
        }

        return expr;
    }

    // Bitwise AND: & (precedence 7)
    private parseBitwiseAnd(): Expression {
        let expr = this.parseShift();

        while (this.matchOperator('&')) {
            const right = this.parseShift();
            expr = new Expression('', expr, right, Operation.BITWISE_AND);
        }

        return expr;
    }

    // Shift: <<, >>, >>> (precedence 4)
    private parseShift(): Expression {
        let expr = this.parseAdditive();

        while (true) {
            let op: Operation | null = null;

            if (this.matchOperator('<<')) {
                op = Operation.BITWISE_LEFT_SHIFT;
            } else if (this.matchOperator('>>')) {
                op = Operation.BITWISE_RIGHT_SHIFT;
            } else if (this.matchOperator('>>>')) {
                op = Operation.BITWISE_UNSIGNED_RIGHT_SHIFT;
            } else {
                break;
            }

            const right = this.parseAdditive();
            expr = new Expression('', expr, right, op);
        }

        return expr;
    }

    // Additive: +, - (precedence 3)
    private parseAdditive(): Expression {
        let expr = this.parseMultiplicative();

        while (true) {
            let op: Operation | null = null;

            if (this.matchOperator('+')) {
                op = Operation.ADDITION;
            } else if (this.matchOperator('-')) {
                op = Operation.SUBTRACTION;
            } else {
                break;
            }

            const right = this.parseMultiplicative();
            expr = new Expression('', expr, right, op);
        }

        return expr;
    }

    // Multiplicative: *, /, //, % (precedence 2)
    // Note: Right-associative to match old parser behavior (12*13*14/7 = 12*(13*(14/7)))
    private parseMultiplicative(): Expression {
        let expr = this.parseUnary();

        // Check for multiplicative operators
        if (this.matchOperator('*')) {
            const right = this.parseMultiplicative(); // Right-associative
            return new Expression('', expr, right, Operation.MULTIPLICATION);
        } else if (this.matchOperator('/')) {
            const right = this.parseMultiplicative(); // Right-associative
            return new Expression('', expr, right, Operation.DIVISION);
        } else if (this.matchOperator('//')) {
            const right = this.parseMultiplicative(); // Right-associative
            return new Expression('', expr, right, Operation.INTEGER_DIVISION);
        } else if (this.matchOperator('%')) {
            const right = this.parseMultiplicative(); // Right-associative
            return new Expression('', expr, right, Operation.MOD);
        }

        return expr;
    }

    // Unary: +, -, ~, not (precedence 1)
    private parseUnary(): Expression {
        if (this.matchOperator('+')) {
            const expr = this.parseUnary();
            return new Expression('', expr, undefined, Operation.UNARY_PLUS);
        }

        if (this.matchOperator('-')) {
            const expr = this.parseUnary();
            return new Expression('', expr, undefined, Operation.UNARY_MINUS);
        }

        if (this.matchOperator('~')) {
            const expr = this.parseUnary();
            return new Expression('', expr, undefined, Operation.UNARY_BITWISE_COMPLEMENT);
        }

        return this.parsePostfix();
    }

    // Postfix: member access, array access (precedence 1)
    // Note: When we have Context.a[...], we need to parse it as Context.(a[...])
    // not (Context.a)[...]. This means array access on an identifier should be
    // grouped with that identifier before applying the object operator.
    private parsePostfix(): Expression {
        let expr = this.parsePrimary();

        while (true) {
            // Object member access: .identifier
            // This must parse the entire right-hand side (including array access) before applying
            if (this.matchToken(TokenType.DOT)) {
                // Parse identifier and all its postfix operations (array access, more dots, etc.)
                // This will consume all tokens up to the next non-postfix operator
                const right = this.parsePostfixRightSide();
                expr = new Expression('', expr, right, Operation.OBJECT_OPERATOR);
                // Don't continue - parsePostfixRightSide() should have consumed everything
                // If there are more postfix operations, they should be part of the right-hand side
                continue;
            }
            // Array access: [expression] - for array access directly on the current expression
            // Only if we didn't just process a dot (dot handling is above)
            else if (this.matchToken(TokenType.LEFT_BRACKET)) {
                const indexExpr = this.parseBracketContent();
                this.expectToken(TokenType.RIGHT_BRACKET);
                expr = new Expression('', expr, indexExpr, Operation.ARRAY_OPERATOR);
            }
            // Range operator: ..
            else if (this.matchOperator('..')) {
                const right = this.parsePrimary();
                expr = new Expression('', expr, right, Operation.ARRAY_RANGE_INDEX_OPERATOR);
            } else {
                break;
            }
        }

        return expr;
    }

    // Parse the right side of a dot operator (identifier that may have static array access)
    // The lexer now only includes STATIC bracket content in identifiers (numeric or quoted strings)
    // Dynamic bracket content ([Page.id], [expr]) is tokenized separately as LEFT_BRACKET
    // 
    // This method consumes ALL subsequent property accesses (dots) and array accesses,
    // creating a grouped right-hand side for the OBJECT_OPERATOR.
    // 
    // Examples:
    // - "Context.obj[\"key\"]" -> Context . (obj["key"])
    // - "Context.a.b.c" -> Context . (a . (b . c))
    // - "Context.obj[\"key\"].value" -> Context . (obj["key"] . value)
    private parsePostfixRightSide(): Expression {
        // Expect an identifier (which may include STATIC bracket notation like obj["key"] or a[9])
        if (!this.currentToken || this.currentToken.type !== TokenType.IDENTIFIER) {
            throw new ExpressionEvaluationException(
                this.lexer.getPosition().toString(),
                'Expected identifier after dot',
            );
        }

        // The identifier token value might contain static bracket notation like "obj[\"key\"]" or "a[9]"
        // Or it might be a plain identifier if bracket content is dynamic
        const identifierValue = this.currentToken.value;
        this.advance();
        
        // Check if the identifier contains static bracket notation
        const bracketIndex = identifierValue.indexOf('[');
        let expr: Expression;
        
        if (bracketIndex === -1) {
            // No bracket notation in the identifier - it's a simple identifier
            // Use createLeaf to avoid re-parsing
            expr = Expression.createLeaf(identifierValue);
        } else {
            // Static bracket notation is included in the identifier
            // Parse it to extract base identifier and static bracket expressions
            expr = this.parseStaticBracketIdentifier(identifierValue);
        }
        
        // Check for dynamic array access following the identifier (LEFT_BRACKET token)
        while (this.matchToken(TokenType.LEFT_BRACKET)) {
            const indexExpr = this.parseBracketContent();
            this.expectToken(TokenType.RIGHT_BRACKET);
            expr = new Expression('', expr, indexExpr, Operation.ARRAY_OPERATOR);
        }
        
        // Also consume any subsequent DOTs to group all property accesses on the right side
        // This creates the structure: Context . (a . (b . c)) instead of ((Context . a) . b) . c
        while (this.matchToken(TokenType.DOT)) {
            const right = this.parsePostfixRightSide();  // Recursive call
            expr = new Expression('', expr, right, Operation.OBJECT_OPERATOR);
        }
        
        return expr;
    }
    
    /**
     * Parse an identifier that contains static bracket notation.
     * E.g., "obj[\"key\"]" or "a[9]" or "a[9][\"key\"]"
     */
    private parseStaticBracketIdentifier(identifierValue: string): Expression {
        const bracketIndex = identifierValue.indexOf('[');
        
        // Extract base identifier - use createLeaf to avoid re-parsing
        const baseIdentifier = identifierValue.substring(0, bracketIndex);
        let expr = Expression.createLeaf(baseIdentifier);
        
        // Parse all static bracket expressions
        let remaining = identifierValue.substring(bracketIndex);
        let bracketStart = 0;
        
        while (bracketStart < remaining.length && remaining[bracketStart] === '[') {
            // Find the matching closing bracket
            let bracketCount = 1;
            let endIndex = bracketStart + 1;
            let inString = false;
            let stringChar = '';
            
            while (endIndex < remaining.length && bracketCount > 0) {
                const c = remaining[endIndex];
                
                if (inString) {
                    if (c === stringChar && (endIndex === 0 || remaining[endIndex - 1] !== '\\')) {
                        inString = false;
                    }
                } else {
                    if (c === '"' || c === "'") {
                        inString = true;
                        stringChar = c;
                    } else if (c === '[') {
                        bracketCount++;
                    } else if (c === ']') {
                        bracketCount--;
                    }
                }
                endIndex++;
            }
            
            // Extract bracket content (without the brackets)
            const bracketContent = remaining.substring(bracketStart + 1, endIndex - 1);
            
            // Create expression for this bracket content
            let indexExpr: Expression;
            if ((bracketContent.startsWith('"') && bracketContent.endsWith('"')) ||
                (bracketContent.startsWith("'") && bracketContent.endsWith("'"))) {
                // It's a string literal - preserve quotes
                const quoteChar = bracketContent[0];
                const strValue = bracketContent.substring(1, bracketContent.length - 1);
                indexExpr = new Expression('', new ExpressionTokenValue(quoteChar + strValue + quoteChar, strValue), undefined, undefined);
            } else {
                // It's a number or range (static content from lexer)
                // Check for range operator
                const rangeIndex = bracketContent.indexOf('..');
                if (rangeIndex !== -1) {
                    // Range expression like "0..5"
                    const startExpr = rangeIndex === 0 ? Expression.createLeaf('0') : Expression.createLeaf(bracketContent.substring(0, rangeIndex));
                    const endExpr = rangeIndex === bracketContent.length - 2 ? Expression.createLeaf('') : Expression.createLeaf(bracketContent.substring(rangeIndex + 2));
                    indexExpr = new Expression('', startExpr, endExpr, Operation.ARRAY_RANGE_INDEX_OPERATOR);
                } else {
                    // Simple number - use createLeaf to avoid re-parsing
                    indexExpr = Expression.createLeaf(bracketContent);
                }
            }
            
            // Create array access expression
            expr = new Expression('', expr, indexExpr, Operation.ARRAY_OPERATOR);
            
            bracketStart = endIndex;
        }
        
        return expr;
    }

    // Parse identifier path - the lexer now handles paths with STATIC bracket notation only
    // Dynamic bracket content is tokenized separately as LEFT_BRACKET
    // 
    // Examples:
    // - "Context.obj[\"key\"]" (static) -> single IDENTIFIER token
    // - "Context.a[9]" (static) -> single IDENTIFIER token  
    // - "Context.a[Page.id]" (dynamic) -> IDENTIFIER "Context.a" + LEFT_BRACKET + expression
    private parseIdentifierPath(): Expression {
        if (!this.currentToken || this.currentToken.type !== TokenType.IDENTIFIER) {
            throw new ExpressionEvaluationException(
                this.lexer.getPosition().toString(),
                'Expected identifier',
            );
        }

        // The identifier token contains the path with static brackets (e.g., "Context.obj[\"key\"]")
        // or a plain path if bracket content is dynamic (e.g., "Context.a")
        const path = this.currentToken.value;
        this.advance();

        // For paths with static bracket notation, return as single identifier for TokenValueExtractor
        // The evaluator will use TokenValueExtractor to resolve the entire path efficiently
        // Use createLeaf to avoid re-parsing
        return Expression.createLeaf(path);
    }

    // Parse bracket content - can be expression, string literal, or identifier
    private parseBracketContent(): Expression {
        // If it's a string literal (quoted), preserve it
        if (this.currentToken && this.currentToken.type === TokenType.STRING) {
            const token = this.currentToken;
            this.advance();
            const strValue = token.value.substring(1, token.value.length - 1);
            return new Expression('', new ExpressionTokenValue(token.value, strValue), undefined, undefined);
        }

        // Otherwise parse as expression
        return this.parseExpression();
    }

    // Primary: literals, identifiers, parentheses, groups
    private parsePrimary(): Expression {
        // Number literal - wrap in Expression with ExpressionToken as token
        if (this.matchToken(TokenType.NUMBER)) {
            const token = this.previousToken()!;
            // Create an Expression containing a single ExpressionToken (no operations)
            // This matches the old parser's structure
            return new Expression('', new ExpressionToken(token.value), undefined, undefined);
        }

        // String literal
        if (this.matchToken(TokenType.STRING)) {
            const token = this.previousToken()!;
            // Remove quotes for the value, but keep original expression for bracket notation
            const strValue = token.value.substring(1, token.value.length - 1);
            // Create ExpressionTokenValue with original quoted string as expression
            return new Expression('', new ExpressionTokenValue(token.value, strValue), undefined, undefined);
        }

        // Identifier (may contain dots for paths like "Context.obj")
        if (this.currentToken && this.currentToken.type === TokenType.IDENTIFIER) {
            return this.parseIdentifierPath();
        }

        // Parenthesized expression
        if (this.matchToken(TokenType.LEFT_PAREN)) {
            const expr = this.parseExpression();
            this.expectToken(TokenType.RIGHT_PAREN);
            return expr;
        }

        // Nullish coalescing: ??
        if (this.matchOperator('??')) {
            const right = this.parsePrimary();
            // This should be handled at a higher level, but for now we'll treat it here
            return new Expression('', this.parsePrimary(), right, Operation.NULLISH_COALESCING_OPERATOR);
        }

        throw new ExpressionEvaluationException(
            this.lexer.getPosition().toString(),
            `Unexpected token: ${this.currentToken?.value || 'EOF'} at position ${this.currentToken?.startPos || this.lexer.getPosition()}`,
        );
    }

    // Helper methods
    private matchToken(type: TokenType): boolean {
        if (this.currentToken && this.currentToken.type === type) {
            this.advance();
            return true;
        }
        return false;
    }

    private matchOperator(op: string): boolean {
        if (
            this.currentToken &&
            this.currentToken.type === TokenType.OPERATOR &&
            this.currentToken.value === op
        ) {
            this.advance();
            return true;
        }
        return false;
    }

    private expectToken(type: TokenType): Token {
        if (!this.currentToken || this.currentToken.type !== type) {
            throw new ExpressionEvaluationException(
                this.lexer.getPosition().toString(),
                `Expected ${type}, got ${this.currentToken?.type || 'EOF'}`,
            );
        }
        const token = this.currentToken;
        this.advance();
        return token;
    }

    private advance(): void {
        this.previousTokenValue = this.currentToken;
        this.currentToken = this.lexer.nextToken();
    }

    private previousToken(): Token | null {
        return this.previousTokenValue;
    }
}

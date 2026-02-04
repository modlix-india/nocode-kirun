package com.fincity.nocode.kirun.engine.runtime.expression;

import com.fincity.nocode.kirun.engine.runtime.expression.exception.ExpressionEvaluationException;
import com.google.gson.JsonPrimitive;

public class ExpressionParser {
    private ExpressionLexer lexer;
    private ExpressionLexer.Token currentToken = null;
    private ExpressionLexer.Token previousTokenValue = null;
    private String originalExpression;
    private int parseDepth = 0;

    public ExpressionParser(String expression) {
        this.originalExpression = expression;
        this.lexer = new ExpressionLexer(expression);
        this.currentToken = this.lexer.nextToken();
    }

    /**
     * Creates a detailed parser error with context information for debugging.
     * Logs comprehensive error details including position, context, and token information.
     */
    private ExpressionEvaluationException createParserError(String message) {
        int position = lexer.getPosition();
        int contextStart = Math.max(0, position - 20);
        int contextEnd = Math.min(originalExpression.length(), position + 20);
        String context = originalExpression.substring(contextStart, contextEnd);

        // Create visual pointer to error location
        String pointer = " ".repeat(position - contextStart) + "^";

        // Build detailed error message
        StringBuilder errorDetails = new StringBuilder("\nParser Error: ").append(message)
            .append("\nExpression: ").append(originalExpression)
            .append("\nPosition: ").append(position)
            .append("\nContext: ...").append(context).append("...")
            .append("\n         ").append(pointer);

        if (currentToken != null) {
            errorDetails.append("\nCurrent token: ").append(currentToken.type)
                       .append("(\"").append(currentToken.value).append("\")");
        } else {
            errorDetails.append("\nCurrent token: EOF");
        }

        if (previousTokenValue != null) {
            errorDetails.append("\nPrevious token: ").append(previousTokenValue.type)
                       .append("(\"").append(previousTokenValue.value).append("\")");
        } else {
            errorDetails.append("\nPrevious token: null");
        }

        errorDetails.append("\nParse depth: ").append(parseDepth).append("\n");

        System.err.println(errorDetails.toString());

        return new ExpressionEvaluationException(
            originalExpression,
            message + " at position " + position
        );
    }

    public Expression parse() {
        if (currentToken == null || currentToken.type == ExpressionLexer.TokenType.EOF) {
            throw new ExpressionEvaluationException("", "Empty expression");
        }

        Expression expr = parseExpression();
        
        // Ensure we consumed all tokens
        if (currentToken != null && currentToken.type != ExpressionLexer.TokenType.EOF) {
            throw new ExpressionEvaluationException(
                String.valueOf(lexer.getPosition()),
                String.format("Unexpected token: %s at position %d", currentToken.value, currentToken.startPos)
            );
        }

        return expr;
    }

    private Expression parseExpression() {
        return parseTernary();
    }

    // Ternary: condition ? trueExpr : falseExpr (precedence 12)
    private Expression parseTernary() {
        Expression expr = parseLogicalOr();

        if (matchToken(ExpressionLexer.TokenType.QUESTION)) {
            Expression trueExpr = parseTernary();
            expectToken(ExpressionLexer.TokenType.COLON);
            Expression falseExpr = parseTernary();
            return Expression.createTernary(expr, trueExpr, falseExpr);
        }

        return expr;
    }

    // Logical OR: expr or expr (precedence 11)
    private Expression parseLogicalOr() {
        Expression expr = parseNullishCoalescing();

        while (matchOperator("or")) {
            Expression right = parseNullishCoalescing();
            expr = new Expression("", expr, right, Operation.OR, true);
        }

        return expr;
    }

    // Nullish coalescing: expr ?? expr (precedence between OR and AND)
    private Expression parseNullishCoalescing() {
        Expression expr = parseLogicalAnd();

        while (matchOperator("??")) {
            Expression right = parseLogicalAnd();
            expr = new Expression("", expr, right, Operation.NULLISH_COALESCING_OPERATOR, true);
        }

        return expr;
    }

    // Logical AND: expr and expr (precedence 10)
    private Expression parseLogicalAnd() {
        Expression expr = parseLogicalNot();

        while (matchOperator("and")) {
            Expression right = parseLogicalNot();
            expr = new Expression("", expr, right, Operation.AND, true);
        }

        return expr;
    }

    // Logical NOT: not expr (precedence 10, but unary)
    private Expression parseLogicalNot() {
        if (matchOperator("not")) {
            Expression expr = parseLogicalNot();
            return new Expression("", expr, null, Operation.UNARY_LOGICAL_NOT, true);
        }

        return parseComparison();
    }

    // Comparison: <, <=, >, >=, =, != (precedence 5-6)
    private Expression parseComparison() {
        Expression expr = parseBitwiseOr();

        while (true) {
            Operation op = null;

            if (matchOperator("<")) {
                op = Operation.LESS_THAN;
            } else if (matchOperator("<=")) {
                op = Operation.LESS_THAN_EQUAL;
            } else if (matchOperator(">")) {
                op = Operation.GREATER_THAN;
            } else if (matchOperator(">=")) {
                op = Operation.GREATER_THAN_EQUAL;
            } else if (matchOperator("=")) {
                op = Operation.EQUAL;
            } else if (matchOperator("!=")) {
                op = Operation.NOT_EQUAL;
            } else {
                break;
            }

            Expression right = parseBitwiseOr();
            expr = new Expression("", expr, right, op, true);
        }

        return expr;
    }

    // Bitwise OR: | (precedence 9)
    private Expression parseBitwiseOr() {
        Expression expr = parseBitwiseXor();

        while (matchOperator("|")) {
            Expression right = parseBitwiseXor();
            expr = new Expression("", expr, right, Operation.BITWISE_OR, true);
        }

        return expr;
    }

    // Bitwise XOR: ^ (precedence 8)
    private Expression parseBitwiseXor() {
        Expression expr = parseBitwiseAnd();

        while (matchOperator("^")) {
            Expression right = parseBitwiseAnd();
            expr = new Expression("", expr, right, Operation.BITWISE_XOR, true);
        }

        return expr;
    }

    // Bitwise AND: & (precedence 7)
    private Expression parseBitwiseAnd() {
        Expression expr = parseShift();

        while (matchOperator("&")) {
            Expression right = parseShift();
            expr = new Expression("", expr, right, Operation.BITWISE_AND, true);
        }

        return expr;
    }

    // Shift: <<, >>, >>> (precedence 4)
    private Expression parseShift() {
        Expression expr = parseAdditive();

        while (true) {
            Operation op = null;

            if (matchOperator("<<")) {
                op = Operation.BITWISE_LEFT_SHIFT;
            } else if (matchOperator(">>")) {
                op = Operation.BITWISE_RIGHT_SHIFT;
            } else if (matchOperator(">>>")) {
                op = Operation.BITWISE_UNSIGNED_RIGHT_SHIFT;
            } else {
                break;
            }

            Expression right = parseAdditive();
            expr = new Expression("", expr, right, op, true);
        }

        return expr;
    }

    // Additive: +, - (precedence 3)
    private Expression parseAdditive() {
        Expression expr = parseMultiplicative();

        while (true) {
            Operation op = null;

            if (matchOperator("+")) {
                op = Operation.ADDITION;
            } else if (matchOperator("-")) {
                op = Operation.SUBTRACTION;
            } else {
                break;
            }

            Expression right = parseMultiplicative();
            expr = new Expression("", expr, right, op, true);
        }

        return expr;
    }

    // Multiplicative: *, /, //, % (precedence 2)
    // Note: Right-associative to match old parser behavior
    private Expression parseMultiplicative() {
        Expression expr = parseUnary();

        if (matchOperator("*")) {
            Expression right = parseMultiplicative();
            return new Expression("", expr, right, Operation.MULTIPLICATION, true);
        } else if (matchOperator("/")) {
            Expression right = parseMultiplicative();
            return new Expression("", expr, right, Operation.DIVISION, true);
        } else if (matchOperator("//")) {
            Expression right = parseMultiplicative();
            // Note: Java doesn't have INTEGER_DIVISION operation, use DIVISION for now
            // The evaluator should handle // differently if needed
            return new Expression("", expr, right, Operation.DIVISION, true);
        } else if (matchOperator("%")) {
            Expression right = parseMultiplicative();
            return new Expression("", expr, right, Operation.MOD, true);
        }

        return expr;
    }

    // Unary: +, -, ~, not (precedence 1)
    private Expression parseUnary() {
        if (matchOperator("+")) {
            Expression expr = parseUnary();
            return new Expression("", expr, null, Operation.UNARY_PLUS, true);
        }

        if (matchOperator("-")) {
            Expression expr = parseUnary();
            return new Expression("", expr, null, Operation.UNARY_MINUS, true);
        }

        if (matchOperator("~")) {
            Expression expr = parseUnary();
            return new Expression("", expr, null, Operation.UNARY_BITWISE_COMPLEMENT, true);
        }

        return parsePostfix();
    }

    // Postfix: member access, array access (precedence 1)
    private Expression parsePostfix() {
        Expression expr = parsePrimary();

        while (true) {
            if (matchToken(ExpressionLexer.TokenType.DOT)) {
                Expression right = parsePostfixRightSide();
                expr = new Expression("", expr, right, Operation.OBJECT_OPERATOR, true);
                continue;
            } else if (matchToken(ExpressionLexer.TokenType.LEFT_BRACKET)) {
                Expression indexExpr = parseBracketContent();
                expectToken(ExpressionLexer.TokenType.RIGHT_BRACKET);
                expr = new Expression("", expr, indexExpr, Operation.ARRAY_OPERATOR, true);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expression parsePostfixRightSide() {
        // Accept both IDENTIFIER and NUMBER tokens after DOT (for numeric property keys)
        if (currentToken == null ||
            (currentToken.type != ExpressionLexer.TokenType.IDENTIFIER &&
             currentToken.type != ExpressionLexer.TokenType.NUMBER)) {
            throw createParserError(
                "Expected identifier or number after dot, but found " +
                (currentToken != null ? currentToken.type.toString() : "EOF")
            );
        }

        String identifierValue = currentToken.value;
        advance();

        // Concatenate NUMBER + IDENTIFIER for ObjectId-like values (e.g., 507f1f77bcf86cd799439011)
        // This handles cases where lexer splits "507f1f77..." into NUMBER("507") + IDENTIFIER("f1f77...")
        if (currentToken != null &&
            currentToken.type == ExpressionLexer.TokenType.IDENTIFIER &&
            currentToken.value.length() > 0 &&
            (Character.isLetter(currentToken.value.charAt(0)) || currentToken.value.charAt(0) == '_')) {
            identifierValue += currentToken.value;
            advance();
        }
        
        int bracketIndex = identifierValue.indexOf('[');
        Expression expr;
        
        if (bracketIndex == -1) {
            expr = Expression.createLeaf(identifierValue);
        } else {
            expr = parseStaticBracketIdentifier(identifierValue);
        }
        
        while (matchToken(ExpressionLexer.TokenType.LEFT_BRACKET)) {
            Expression indexExpr = parseBracketContent();
            expectToken(ExpressionLexer.TokenType.RIGHT_BRACKET);
            expr = new Expression("", expr, indexExpr, Operation.ARRAY_OPERATOR, true);
        }
        
        while (matchToken(ExpressionLexer.TokenType.DOT)) {
            Expression right = parsePostfixRightSide();
            expr = new Expression("", expr, right, Operation.OBJECT_OPERATOR, true);
        }
        
        return expr;
    }
    
    private Expression parseStaticBracketIdentifier(String identifierValue) {
        int bracketIndex = identifierValue.indexOf('[');
        
        String baseIdentifier = identifierValue.substring(0, bracketIndex);
        Expression expr = Expression.createLeaf(baseIdentifier);
        
        String remaining = identifierValue.substring(bracketIndex);
        int bracketStart = 0;
        
        while (bracketStart < remaining.length() && remaining.charAt(bracketStart) == '[') {
            int bracketCount = 1;
            int endIndex = bracketStart + 1;
            boolean inString = false;
            char stringChar = 0;
            
            while (endIndex < remaining.length() && bracketCount > 0) {
                char c = remaining.charAt(endIndex);
                
                if (inString) {
                    if (c == stringChar && (endIndex == 0 || remaining.charAt(endIndex - 1) != '\\')) {
                        inString = false;
                    }
                } else {
                    if (c == '"' || c == '\'') {
                        inString = true;
                        stringChar = c;
                    } else if (c == '[') {
                        bracketCount++;
                    } else if (c == ']') {
                        bracketCount--;
                    }
                }
                endIndex++;
            }
            
            String bracketContent = remaining.substring(bracketStart + 1, endIndex - 1);
            
            Expression indexExpr;
            if ((bracketContent.startsWith("\"") && bracketContent.endsWith("\"")) ||
                (bracketContent.startsWith("'") && bracketContent.endsWith("'"))) {
                char quoteChar = bracketContent.charAt(0);
                String strValue = bracketContent.substring(1, bracketContent.length() - 1);
                indexExpr = new Expression("", new ExpressionTokenValue(quoteChar + strValue + quoteChar, new JsonPrimitive(strValue)), null, null, true);
            } else {
                int rangeIndex = bracketContent.indexOf("..");
                if (rangeIndex != -1) {
                    Expression startExpr = rangeIndex == 0 ? Expression.createLeaf("0") : Expression.createLeaf(bracketContent.substring(0, rangeIndex));
                    Expression endExpr = rangeIndex == bracketContent.length() - 2 ? Expression.createLeaf("") : Expression.createLeaf(bracketContent.substring(rangeIndex + 2));
                    indexExpr = new Expression("", startExpr, endExpr, Operation.ARRAY_RANGE_INDEX_OPERATOR, true);
                } else {
                    indexExpr = Expression.createLeaf(bracketContent);
                }
            }
            
            expr = new Expression("", expr, indexExpr, Operation.ARRAY_OPERATOR, true);
            
            bracketStart = endIndex;
        }
        
        return expr;
    }

    private Expression parseIdentifierPath() {
        if (currentToken == null || currentToken.type != ExpressionLexer.TokenType.IDENTIFIER) {
            throw new ExpressionEvaluationException(
                String.valueOf(lexer.getPosition()),
                "Expected identifier"
            );
        }

        String path = currentToken.value;
        advance();

        return Expression.createLeaf(path);
    }

    private Expression parseBracketContent() {
        if (currentToken != null && currentToken.type == ExpressionLexer.TokenType.STRING) {
            ExpressionLexer.Token token = currentToken;
            advance();
            String strValue = token.value.substring(1, token.value.length() - 1);
            return new Expression("", new ExpressionTokenValue(token.value, new JsonPrimitive(strValue)), null, null, true);
        }

        // Parse the full expression which may include a range operator
        Expression expr = parseExpression();

        // Check if there's a range operator .. after the expression
        if (matchOperator("..")) {
            Expression right = parseExpression();
            return new Expression("", expr, right, Operation.ARRAY_RANGE_INDEX_OPERATOR, true);
        }

        return expr;
    }

    private Expression parsePrimary() {
        if (matchToken(ExpressionLexer.TokenType.NUMBER)) {
            ExpressionLexer.Token token = previousToken();
            return new Expression("", new ExpressionToken(token.value), null, null, true);
        }

        if (matchToken(ExpressionLexer.TokenType.STRING)) {
            ExpressionLexer.Token token = previousToken();
            String strValue = token.value.substring(1, token.value.length() - 1);
            return new Expression("", new ExpressionTokenValue(token.value, new JsonPrimitive(strValue)), null, null, true);
        }

        if (currentToken != null && currentToken.type == ExpressionLexer.TokenType.IDENTIFIER) {
            return parseIdentifierPath();
        }

        if (matchToken(ExpressionLexer.TokenType.LEFT_PAREN)) {
            Expression expr = parseExpression();
            expectToken(ExpressionLexer.TokenType.RIGHT_PAREN);
            return expr;
        }

        throw new ExpressionEvaluationException(
            String.valueOf(lexer.getPosition()),
            String.format("Unexpected token: %s at position %d", 
                currentToken != null ? currentToken.value : "EOF", 
                currentToken != null ? currentToken.startPos : lexer.getPosition())
        );
    }

    private boolean matchToken(ExpressionLexer.TokenType type) {
        if (currentToken != null && currentToken.type == type) {
            advance();
            return true;
        }
        return false;
    }

    private boolean matchOperator(String op) {
        if (currentToken != null &&
            currentToken.type == ExpressionLexer.TokenType.OPERATOR &&
            currentToken.value.equals(op)) {
            advance();
            return true;
        }
        return false;
    }

    private ExpressionLexer.Token expectToken(ExpressionLexer.TokenType type) {
        if (currentToken == null || currentToken.type != type) {
            throw new ExpressionEvaluationException(
                String.valueOf(lexer.getPosition()),
                String.format("Expected %s, got %s", type, currentToken != null ? currentToken.type : "EOF")
            );
        }
        ExpressionLexer.Token token = currentToken;
        advance();
        return token;
    }

    private void advance() {
        previousTokenValue = currentToken;
        currentToken = lexer.nextToken();
    }

    private ExpressionLexer.Token previousToken() {
        return previousTokenValue;
    }
}

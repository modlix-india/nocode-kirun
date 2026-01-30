package com.fincity.nocode.kirun.engine.runtime.expression;

import com.fincity.nocode.kirun.engine.runtime.expression.exception.ExpressionEvaluationException;

public class ExpressionLexer {
    private int pos = 0;
    private final String input;
    private final int length;

    public ExpressionLexer(String expression) {
        this.input = expression;
        this.length = expression.length();
    }

    public int getPosition() {
        return pos;
    }

    public Token nextToken() {
        if (pos >= length) {
            return new Token(TokenType.EOF, "", pos, pos);
        }

        // Skip whitespace
        while (pos < length && isWhitespace(input.charAt(pos))) {
            pos++;
        }

        if (pos >= length) {
            return new Token(TokenType.EOF, "", pos, pos);
        }

        int startPos = pos;
        char char_ = input.charAt(pos);

        // Handle string literals
        if (char_ == '"' || char_ == '\'') {
            return readStringLiteral(char_, startPos);
        }

        // Handle numbers
        if (isDigit(char_)) {
            return readNumber(startPos);
        }

        // Handle operators and special characters
        switch (char_) {
            case '(':
                pos++;
                return new Token(TokenType.LEFT_PAREN, "(", startPos, pos);
            case ')':
                pos++;
                return new Token(TokenType.RIGHT_PAREN, ")", startPos, pos);
            case '[':
                pos++;
                return new Token(TokenType.LEFT_BRACKET, "[", startPos, pos);
            case ']':
                pos++;
                return new Token(TokenType.RIGHT_BRACKET, "]", startPos, pos);
            case '.':
                // Check for '..' range operator
                if (pos + 1 < length && input.charAt(pos + 1) == '.') {
                    pos += 2;
                    return new Token(TokenType.OPERATOR, "..", startPos, pos);
                }
                pos++;
                return new Token(TokenType.DOT, ".", startPos, pos);
            case '?':
                // Check for ?? (nullish coalescing) before single ? (ternary)
                if (pos + 1 < length && input.charAt(pos + 1) == '?') {
                    pos += 2;
                    return new Token(TokenType.OPERATOR, "??", startPos, pos);
                }
                pos++;
                return new Token(TokenType.QUESTION, "?", startPos, pos);
            case ':':
                pos++;
                return new Token(TokenType.COLON, ":", startPos, pos);
        }

        // Handle multi-character operators
        Token operator = readOperator(startPos);
        if (operator != null) {
            return operator;
        }

        // Handle identifiers and keywords
        return readIdentifier(startPos);
    }

    private Token readStringLiteral(char quoteChar, int startPos) {
        pos++; // Skip opening quote
        StringBuilder value = new StringBuilder();
        boolean escaped = false;

        while (pos < length) {
            char char_ = input.charAt(pos);
            
            if (escaped) {
                value.append(char_);
                escaped = false;
                pos++;
                continue;
            }

            if (char_ == '\\') {
                escaped = true;
                value.append(char_);
                pos++;
                continue;
            }

            if (char_ == quoteChar) {
                pos++; // Skip closing quote
                int endPos = pos;
                // Return the full string including quotes for bracket notation detection
                return new Token(TokenType.STRING, quoteChar + value.toString() + quoteChar, startPos, endPos);
            }

            value.append(char_);
            pos++;
        }

        throw new ExpressionEvaluationException(
            input,
            "Missing string ending marker " + quoteChar
        );
    }

    private Token readNumber(int startPos) {
        StringBuilder value = new StringBuilder();
        boolean hasDecimal = false;

        while (pos < length) {
            char char_ = input.charAt(pos);
            
            if (isDigit(char_)) {
                value.append(char_);
                pos++;
            } else if (char_ == '.' && !hasDecimal && pos + 1 < length && isDigit(input.charAt(pos + 1))) {
                value.append(char_);
                hasDecimal = true;
                pos++;
            } else {
                break;
            }
        }

        return new Token(TokenType.NUMBER, value.toString(), startPos, pos);
    }

    private Token readOperator(int startPos) {
        // Try longest operators first
        String[] operators = {
            ">>>", "<<", ">>", "<=", ">=", "!=", "==", "//",
            "+", "-", "*", "/", "%", "=", "<", ">", "&", "|", "^", "~"
        };

        for (String op : operators) {
            if (pos + op.length() <= length) {
                String candidate = input.substring(pos, pos + op.length());
                if (candidate.equals(op)) {
                    pos += op.length();
                    return new Token(TokenType.OPERATOR, op, startPos, pos);
                }
            }
        }

        return null;
    }

    private Token readIdentifier(int startPos) {
        StringBuilder value = new StringBuilder();

        while (pos < length) {
            char char_ = input.charAt(pos);
            
            // Handle bracket notation: only include STATIC bracket content in identifier
            if (char_ == '[') {
                if (isStaticBracketContent()) {
                    value.append(readStaticBracketContent());
                    continue;
                } else {
                    break;
                }
            }
            
            // Stop at non-identifier characters
            if (!isIdentifierChar(char_)) {
                break;
            }
            
            value.append(char_);
            pos++;
        }

        if (value.length() == 0) {
            throw new ExpressionEvaluationException(
                input,
                "Unexpected character: " + (pos < length ? input.charAt(pos) : "EOF")
            );
        }

        String valueStr = value.toString();
        // Check for keywords that are operators
        String[] keywordOperators = {"and", "or", "not"};
        for (String keyword : keywordOperators) {
            if (valueStr.equalsIgnoreCase(keyword)) {
                return new Token(TokenType.OPERATOR, valueStr.toLowerCase(), startPos, pos);
            }
        }

        return new Token(TokenType.IDENTIFIER, valueStr, startPos, pos);
    }

    private boolean isStaticBracketContent() {
        int peekPos = pos + 1; // Skip the '['
        
        // Skip whitespace
        while (peekPos < length && isWhitespace(input.charAt(peekPos))) {
            peekPos++;
        }
        
        if (peekPos >= length) return false;
        
        char firstChar = input.charAt(peekPos);
        
        // Check for quoted string
        if (firstChar == '"' || firstChar == '\'') {
            return true;
        }
        
        // Check for numeric literal (including negative numbers)
        if (isDigit(firstChar) || (firstChar == '-' && peekPos + 1 < length && isDigit(input.charAt(peekPos + 1)))) {
            int numEnd = peekPos;
            if (input.charAt(numEnd) == '-') numEnd++;
            while (numEnd < length && (isDigit(input.charAt(numEnd)) || input.charAt(numEnd) == '.')) {
                // Check for range operator (..)
                if (input.charAt(numEnd) == '.' && numEnd + 1 < length && input.charAt(numEnd + 1) == '.') {
                    return true;
                }
                numEnd++;
            }
            // Skip whitespace
            while (numEnd < length && isWhitespace(input.charAt(numEnd))) {
                numEnd++;
            }
            // Check if followed by ] or ..
            if (numEnd < length && (input.charAt(numEnd) == ']' || 
                (input.charAt(numEnd) == '.' && numEnd + 1 < length && input.charAt(numEnd + 1) == '.'))) {
                return true;
            }
            return false;
        }
        
        return false;
    }

    private String readStaticBracketContent() {
        StringBuilder content = new StringBuilder();
        content.append('[');
        pos++; // Skip the '['
        
        int bracketCount = 1;
        while (pos < length && bracketCount > 0) {
            char c = input.charAt(pos);
            content.append(c);
            pos++;
            
            if (c == '[') {
                bracketCount++;
            } else if (c == ']') {
                bracketCount--;
            } else if (c == '"' || c == '\'') {
                // Handle string literals inside brackets
                char quoteChar = c;
                while (pos < length) {
                    char nextChar = input.charAt(pos);
                    content.append(nextChar);
                    pos++;
                    if (nextChar == quoteChar && pos > 1 && input.charAt(pos - 2) != '\\') {
                        break;
                    }
                }
            }
        }
        
        return content.toString();
    }

    private boolean isDigit(char char_) {
        return char_ >= '0' && char_ <= '9';
    }

    private boolean isWhitespace(char char_) {
        return char_ == ' ' || char_ == '\t' || char_ == '\n' || char_ == '\r';
    }

    private boolean isIdentifierChar(char char_) {
        return (char_ >= 'a' && char_ <= 'z') ||
               (char_ >= 'A' && char_ <= 'Z') ||
               (char_ >= '0' && char_ <= '9') ||
               char_ == '_';
    }

    public enum TokenType {
        IDENTIFIER,
        NUMBER,
        STRING,
        OPERATOR,
        LEFT_PAREN,
        RIGHT_PAREN,
        LEFT_BRACKET,
        RIGHT_BRACKET,
        DOT,
        QUESTION,
        COLON,
        WHITESPACE,
        EOF
    }

    public static class Token {
        public final TokenType type;
        public final String value;
        public final int startPos;
        public final int endPos;

        public Token(TokenType type, String value, int startPos, int endPos) {
            this.type = type;
            this.value = value;
            this.startPos = startPos;
            this.endPos = endPos;
        }

        @Override
        public String toString() {
            return String.format("Token(%s, \"%s\", %d-%d)", type, value, startPos, endPos);
        }
    }
}

import { ExpressionEvaluationException } from './exception/ExpressionEvaluationException';

export enum TokenType {
    IDENTIFIER = 'IDENTIFIER',
    NUMBER = 'NUMBER',
    STRING = 'STRING',
    OPERATOR = 'OPERATOR',
    LEFT_PAREN = 'LEFT_PAREN',
    RIGHT_PAREN = 'RIGHT_PAREN',
    LEFT_BRACKET = 'LEFT_BRACKET',
    RIGHT_BRACKET = 'RIGHT_BRACKET',
    DOT = 'DOT',
    QUESTION = 'QUESTION',
    COLON = 'COLON',
    WHITESPACE = 'WHITESPACE',
    EOF = 'EOF',
}

export class Token {
    constructor(
        public type: TokenType,
        public value: string,
        public startPos: number,
        public endPos: number,
    ) {}

    public toString(): string {
        return `Token(${this.type}, "${this.value}", ${this.startPos}-${this.endPos})`;
    }
}

export class ExpressionLexer {
    private pos: number = 0;
    private readonly input: string;
    private readonly length: number;

    constructor(expression: string) {
        this.input = expression;
        this.length = expression.length;
    }

    public getPosition(): number {
        return this.pos;
    }

    public peek(): Token | null {
        const savedPos = this.pos;
        const token = this.nextToken();
        this.pos = savedPos;
        return token;
    }

    public nextToken(): Token | null {
        if (this.pos >= this.length) {
            return new Token(TokenType.EOF, '', this.pos, this.pos);
        }

        // Skip whitespace
        while (this.pos < this.length && this.isWhitespace(this.input[this.pos])) {
            this.pos++;
        }

        if (this.pos >= this.length) {
            return new Token(TokenType.EOF, '', this.pos, this.pos);
        }

        const startPos = this.pos;
        const char = this.input[this.pos];

        // Handle string literals
        if (char === '"' || char === "'") {
            return this.readStringLiteral(char, startPos);
        }

        // Handle numbers
        // Note: Only treat '-' as negative sign at start or after operator/open paren, not after identifiers/numbers
        if (this.isDigit(char)) {
            return this.readNumber(startPos);
        }

        // Handle operators and special characters
        switch (char) {
            case '(':
                this.pos++;
                return new Token(TokenType.LEFT_PAREN, '(', startPos, this.pos);
            case ')':
                this.pos++;
                return new Token(TokenType.RIGHT_PAREN, ')', startPos, this.pos);
            case '[':
                this.pos++;
                return new Token(TokenType.LEFT_BRACKET, '[', startPos, this.pos);
            case ']':
                this.pos++;
                return new Token(TokenType.RIGHT_BRACKET, ']', startPos, this.pos);
            case '.':
                // Check for '..' range operator
                if (this.pos + 1 < this.length && this.input[this.pos + 1] === '.') {
                    this.pos += 2;
                    return new Token(TokenType.OPERATOR, '..', startPos, this.pos);
                }
                this.pos++;
                return new Token(TokenType.DOT, '.', startPos, this.pos);
            case '?':
                this.pos++;
                return new Token(TokenType.QUESTION, '?', startPos, this.pos);
            case ':':
                this.pos++;
                return new Token(TokenType.COLON, ':', startPos, this.pos);
        }

        // Handle multi-character operators
        const operator = this.readOperator(startPos);
        if (operator) {
            return operator;
        }

        // Handle identifiers and keywords
        return this.readIdentifier(startPos);
    }

    private readStringLiteral(quoteChar: string, startPos: number): Token {
        this.pos++; // Skip opening quote
        let value = '';
        let escaped = false;

        while (this.pos < this.length) {
            const char = this.input[this.pos];
            
            if (escaped) {
                value += char;
                escaped = false;
                this.pos++;
                continue;
            }

            if (char === '\\') {
                escaped = true;
                value += char;
                this.pos++;
                continue;
            }

            if (char === quoteChar) {
                this.pos++; // Skip closing quote
                const endPos = this.pos;
                // Return the full string including quotes for bracket notation detection
                return new Token(TokenType.STRING, quoteChar + value + quoteChar, startPos, endPos);
            }

            value += char;
            this.pos++;
        }

        throw new ExpressionEvaluationException(
            this.input,
            `Missing string ending marker ${quoteChar}`,
        );
    }

    private readNumber(startPos: number): Token {
        let value = '';
        let hasDecimal = false;

        // Note: Negative sign is handled by the parser as unary minus, not by the lexer
        // This avoids the ambiguity between "a-1" (subtraction) and "-1" (negative number)

        while (this.pos < this.length) {
            const char = this.input[this.pos];
            
            if (this.isDigit(char)) {
                value += char;
                this.pos++;
            } else if (char === '.' && !hasDecimal && this.pos + 1 < this.length && this.isDigit(this.input[this.pos + 1])) {
                value += char;
                hasDecimal = true;
                this.pos++;
            } else {
                break;
            }
        }

        return new Token(TokenType.NUMBER, value, startPos, this.pos);
    }

    private readOperator(startPos: number): Token | null {
        // Try longest operators first
        const operators = [
            '>>>', '<<', '>>', '<=', '>=', '!=', '==', '//',
            '+', '-', '*', '/', '%', '=', '<', '>', '&', '|', '^', '~',
        ];

        for (const op of operators) {
            if (this.pos + op.length <= this.length) {
                const candidate = this.input.substring(this.pos, this.pos + op.length);
                if (candidate === op) {
                    this.pos += op.length;
                    return new Token(TokenType.OPERATOR, op, startPos, this.pos);
                }
            }
        }

        return null;
    }

    private readIdentifier(startPos: number): Token {
        let value = '';

        while (this.pos < this.length) {
            const char = this.input[this.pos];
            
            // Handle bracket notation: only include STATIC bracket content in identifier
            // Static: [9], ["key"], ['key'] - numeric literals or quoted strings
            // Dynamic: [Page.id], [expr + 1] - identifiers or expressions
            if (char === '[') {
                // Check if bracket content is static (numeric or quoted string)
                if (this.isStaticBracketContent()) {
                    // Include static bracket content in the identifier
                    value += this.readStaticBracketContent();
                    // Continue reading more of the identifier
                    continue;
                } else {
                    // Dynamic bracket content - stop identifier here
                    // The bracket will be tokenized separately as LEFT_BRACKET
                    break;
                }
            }
            
            // Stop at non-identifier characters
            // Dots are NOT included - they are handled as DOT tokens for OBJECT_OPERATOR
            if (!this.isIdentifierChar(char)) {
                break;
            }
            
            value += char;
            this.pos++;
        }

        if (value.length === 0) {
            throw new ExpressionEvaluationException(
                this.input,
                `Unexpected character: ${this.input[this.pos]}`,
            );
        }

        // Check for keywords that are operators (standalone, not part of identifier)
        const keywordOperators = ['and', 'or', 'not'];
        if (keywordOperators.includes(value.toLowerCase())) {
            return new Token(TokenType.OPERATOR, value.toLowerCase(), startPos, this.pos);
        }

        return new Token(TokenType.IDENTIFIER, value, startPos, this.pos);
    }

    /**
     * Check if the bracket content starting at current position is static.
     * Static content: numeric literals ([9], [-1]) or quoted strings (["key"], ['key'])
     * Dynamic content: identifiers ([Page.id]) or expressions ([expr + 1])
     */
    private isStaticBracketContent(): boolean {
        // Peek ahead to check what's inside the bracket
        let peekPos = this.pos + 1; // Skip the '['
        
        // Skip whitespace
        while (peekPos < this.length && this.isWhitespace(this.input[peekPos])) {
            peekPos++;
        }
        
        if (peekPos >= this.length) return false;
        
        const firstChar = this.input[peekPos];
        
        // Check for quoted string
        if (firstChar === '"' || firstChar === "'") {
            return true;
        }
        
        // Check for numeric literal (including negative numbers)
        if (this.isDigit(firstChar) || (firstChar === '-' && peekPos + 1 < this.length && this.isDigit(this.input[peekPos + 1]))) {
            // Verify it's a pure number followed by ] or ..
            let numEnd = peekPos;
            if (this.input[numEnd] === '-') numEnd++;
            while (numEnd < this.length && (this.isDigit(this.input[numEnd]) || this.input[numEnd] === '.')) {
                // Check for range operator (..)
                if (this.input[numEnd] === '.' && numEnd + 1 < this.length && this.input[numEnd + 1] === '.') {
                    // This is a range operator - still static
                    return true;
                }
                numEnd++;
            }
            // Skip whitespace
            while (numEnd < this.length && this.isWhitespace(this.input[numEnd])) {
                numEnd++;
            }
            // Check if followed by ] or ..
            if (numEnd < this.length && (this.input[numEnd] === ']' || 
                (this.input[numEnd] === '.' && numEnd + 1 < this.length && this.input[numEnd + 1] === '.'))) {
                return true;
            }
            // If followed by anything else (like + or identifier), it's dynamic
            return false;
        }
        
        // Anything else (identifier, operator) is dynamic
        return false;
    }

    /**
     * Read static bracket content and return it as a string (including brackets).
     * Assumes isStaticBracketContent() returned true.
     */
    private readStaticBracketContent(): string {
        let content = '[';
        this.pos++; // Skip the '['
        
        let bracketCount = 1;
        while (this.pos < this.length && bracketCount > 0) {
            const c = this.input[this.pos];
            content += c;
            this.pos++;
            
            if (c === '[') {
                bracketCount++;
            } else if (c === ']') {
                bracketCount--;
            } else if (c === '"' || c === "'") {
                // Handle string literals inside brackets - read until closing quote
                const quoteChar = c;
                while (this.pos < this.length) {
                    const nextChar = this.input[this.pos];
                    content += nextChar;
                    this.pos++;
                    if (nextChar === quoteChar && this.pos > 1 && this.input[this.pos - 2] !== '\\') {
                        break;
                    }
                }
            }
        }
        
        return content;
    }
    
    private isOperatorChar(char: string): boolean {
        // Characters that indicate operators (not part of identifiers)
        return ['+', '-', '*', '/', '%', '=', '!', '<', '>', '&', '|', '?', ':', '(', ')', ';', ','].includes(char);
    }

    private isDigit(char: string): boolean {
        return char >= '0' && char <= '9';
    }

    private isWhitespace(char: string): boolean {
        return char === ' ' || char === '\t' || char === '\n' || char === '\r';
    }

    private isIdentifierChar(char: string): boolean {
        // Identifiers should NOT contain dots - dots are handled by the parser as OBJECT_OPERATOR
        // This preserves the OBJECT_OPERATOR tree structure for evaluation
        return (
            (char >= 'a' && char <= 'z') ||
            (char >= 'A' && char <= 'Z') ||
            (char >= '0' && char <= '9') ||
            char === '_'
        );
    }
}

import { DSLToken, DSLTokenType, SourceLocation } from './DSLToken';
import { isKeyword } from './Keywords';
import { LexerError } from './LexerError';

/**
 * DSL Lexer - Tokenizes DSL text into tokens
 *
 * Features:
 * - Keyword recognition
 * - String literals with escape sequences
 * - Number literals (integers and floats)
 * - Comment stripping (single-line and block comments)
 * - Position tracking for error messages
 */
export class DSLLexer {
    private input: string;
    private pos: number = 0;
    private line: number = 1;
    private column: number = 1;
    private tokens: DSLToken[] = [];

    constructor(input: string) {
        this.input = input;
    }

    /**
     * Tokenize the input string
     */
    public tokenize(): DSLToken[] {
        while (this.pos < this.input.length) {
            this.skipWhitespace();

            if (this.pos >= this.input.length) {
                break;
            }

            // Try to read a token
            const token = this.readToken();
            if (token) {
                // Skip comments and whitespace tokens
                if (token.type !== DSLTokenType.COMMENT && token.type !== DSLTokenType.WHITESPACE) {
                    this.tokens.push(token);
                }
            }
        }

        // Add EOF token
        this.tokens.push(
            new DSLToken(
                DSLTokenType.EOF,
                '',
                new SourceLocation(this.line, this.column, this.pos, this.pos),
            ),
        );

        return this.tokens;
    }

    /**
     * Skip whitespace (spaces, tabs, newlines)
     */
    private skipWhitespace(): void {
        while (this.pos < this.input.length) {
            const ch = this.input[this.pos];
            if (ch === ' ' || ch === '\t' || ch === '\r') {
                this.advance();
            } else if (ch === '\n') {
                this.advance();
                this.line++;
                this.column = 1;
            } else {
                break;
            }
        }
    }

    /**
     * Read a single token
     */
    private readToken(): DSLToken | null {
        const startPos = this.pos;
        const startLine = this.line;
        const startColumn = this.column;

        const ch = this.peek();

        // Comments
        if (ch === '/' && this.peekAhead(1) === '/') {
            return this.readLineComment();
        }
        if (ch === '/' && this.peekAhead(1) === '*') {
            return this.readBlockComment();
        }

        // String literals
        if (ch === '"' || ch === "'") {
            return this.readStringLiteral(ch);
        }

        // Numbers
        if (this.isDigit(ch) || (ch === '-' && this.isDigit(this.peekAhead(1)))) {
            return this.readNumber();
        }

        // Identifiers and keywords
        if (this.isIdentifierStart(ch)) {
            return this.readIdentifier();
        }

        // Single-character tokens
        const singleChar = this.readSingleCharToken();
        if (singleChar) {
            return singleChar;
        }

        // Unknown character
        throw new LexerError(
            `Unexpected character '${ch}'`,
            new SourceLocation(startLine, startColumn, startPos, this.pos),
            this.input,
        );
    }

    /**
     * Read single-character tokens
     */
    private readSingleCharToken(): DSLToken | null {
        const startPos = this.pos;
        const startLine = this.line;
        const startColumn = this.column;
        const ch = this.advance();

        let type: DSLTokenType | null = null;

        switch (ch) {
            case ':':
                type = DSLTokenType.COLON;
                break;
            case ',':
                type = DSLTokenType.COMMA;
                break;
            case '.':
                type = DSLTokenType.DOT;
                break;
            case '=':
                type = DSLTokenType.EQUALS;
                break;
            case '(':
                type = DSLTokenType.LEFT_PAREN;
                break;
            case ')':
                type = DSLTokenType.RIGHT_PAREN;
                break;
            case '{':
                type = DSLTokenType.LEFT_BRACE;
                break;
            case '}':
                type = DSLTokenType.RIGHT_BRACE;
                break;
            case '[':
                type = DSLTokenType.LEFT_BRACKET;
                break;
            case ']':
                type = DSLTokenType.RIGHT_BRACKET;
                break;
            default:
                return null;
        }

        return new DSLToken(
            type,
            ch,
            new SourceLocation(startLine, startColumn, startPos, this.pos),
        );
    }

    /**
     * Read identifier or keyword
     */
    private readIdentifier(): DSLToken {
        const startPos = this.pos;
        const startLine = this.line;
        const startColumn = this.column;
        let value = '';

        while (this.pos < this.input.length) {
            const ch = this.peek();
            if (this.isIdentifierPart(ch)) {
                value += this.advance();
            } else {
                break;
            }
        }

        const location = new SourceLocation(startLine, startColumn, startPos, this.pos);

        // Check if it's a keyword
        if (isKeyword(value)) {
            return new DSLToken(DSLTokenType.KEYWORD, value, location);
        }

        // Check if it's a boolean literal
        if (value === 'true' || value === 'false') {
            return new DSLToken(DSLTokenType.BOOLEAN, value, location);
        }

        // Check if it's null
        if (value === 'null') {
            return new DSLToken(DSLTokenType.NULL, value, location);
        }

        // Otherwise, it's an identifier
        return new DSLToken(DSLTokenType.IDENTIFIER, value, location);
    }

    /**
     * Read number literal (integer or float)
     */
    private readNumber(): DSLToken {
        const startPos = this.pos;
        const startLine = this.line;
        const startColumn = this.column;
        let value = '';

        // Handle negative numbers
        if (this.peek() === '-') {
            value += this.advance();
        }

        // Read integer part
        while (this.pos < this.input.length && this.isDigit(this.peek())) {
            value += this.advance();
        }

        // Check for decimal point
        if (this.peek() === '.' && this.isDigit(this.peekAhead(1))) {
            value += this.advance(); // consume '.'
            while (this.pos < this.input.length && this.isDigit(this.peek())) {
                value += this.advance();
            }
        }

        return new DSLToken(
            DSLTokenType.NUMBER,
            value,
            new SourceLocation(startLine, startColumn, startPos, this.pos),
        );
    }

    /**
     * Read string literal with escape sequences
     */
    private readStringLiteral(quoteChar: string): DSLToken {
        const startPos = this.pos;
        const startLine = this.line;
        const startColumn = this.column;
        let value = '';

        // Consume opening quote
        this.advance();
        value += quoteChar;

        while (this.pos < this.input.length) {
            const ch = this.peek();

            // End of string
            if (ch === quoteChar) {
                value += this.advance();
                break;
            }

            // Escape sequences
            if (ch === '\\') {
                value += this.advance(); // backslash
                if (this.pos < this.input.length) {
                    value += this.advance(); // escaped character
                }
                continue;
            }

            // Regular character
            value += this.advance();
        }

        // Check if string was closed
        if (!value.endsWith(quoteChar)) {
            throw new LexerError(
                `Unterminated string literal`,
                new SourceLocation(startLine, startColumn, startPos, this.pos),
                this.input,
            );
        }

        return new DSLToken(
            DSLTokenType.STRING,
            value,
            new SourceLocation(startLine, startColumn, startPos, this.pos),
        );
    }

    /**
     * Read single-line comment // ...
     */
    private readLineComment(): DSLToken {
        const startPos = this.pos;
        const startLine = this.line;
        const startColumn = this.column;
        let value = '';

        // Consume //
        value += this.advance();
        value += this.advance();

        // Read until end of line
        while (this.pos < this.input.length && this.peek() !== '\n') {
            value += this.advance();
        }

        return new DSLToken(
            DSLTokenType.COMMENT,
            value,
            new SourceLocation(startLine, startColumn, startPos, this.pos),
        );
    }

    /**
     * Read block comment /* ... *\/
     */
    private readBlockComment(): DSLToken {
        const startPos = this.pos;
        const startLine = this.line;
        const startColumn = this.column;
        let value = '';

        // Consume /*
        value += this.advance();
        value += this.advance();

        // Read until */
        while (this.pos < this.input.length) {
            if (this.peek() === '*' && this.peekAhead(1) === '/') {
                value += this.advance(); // *
                value += this.advance(); // /
                break;
            }

            const ch = this.advance();
            value += ch;

            // Track line numbers
            if (ch === '\n') {
                this.line++;
                this.column = 1;
            }
        }

        // Check if comment was closed
        if (!value.endsWith('*/')) {
            throw new LexerError(
                `Unterminated block comment`,
                new SourceLocation(startLine, startColumn, startPos, this.pos),
                this.input,
            );
        }

        return new DSLToken(
            DSLTokenType.COMMENT,
            value,
            new SourceLocation(startLine, startColumn, startPos, this.pos),
        );
    }

    /**
     * Helper: peek at current character
     */
    private peek(): string {
        return this.input[this.pos] || '';
    }

    /**
     * Helper: peek ahead n characters
     */
    private peekAhead(n: number): string {
        return this.input[this.pos + n] || '';
    }

    /**
     * Helper: advance position and return current character
     */
    private advance(): string {
        const ch = this.input[this.pos];
        this.pos++;
        this.column++;
        return ch;
    }

    /**
     * Helper: check if character is a digit
     */
    private isDigit(ch: string): boolean {
        return ch >= '0' && ch <= '9';
    }

    /**
     * Helper: check if character can start an identifier
     */
    private isIdentifierStart(ch: string): boolean {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch === '_';
    }

    /**
     * Helper: check if character can be part of an identifier
     */
    private isIdentifierPart(ch: string): boolean {
        return this.isIdentifierStart(ch) || this.isDigit(ch);
    }
}

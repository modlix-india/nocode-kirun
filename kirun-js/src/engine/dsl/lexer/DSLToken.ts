/**
 * Token types for DSL lexer
 */
export enum DSLTokenType {
    // Keywords
    KEYWORD = 'KEYWORD',

    // Identifiers and literals
    IDENTIFIER = 'IDENTIFIER',
    NUMBER = 'NUMBER',
    STRING = 'STRING',
    BACKTICK_STRING = 'BACKTICK_STRING', // `...` for expressions
    BOOLEAN = 'BOOLEAN',
    NULL = 'NULL',

    // Operators and delimiters
    COLON = 'COLON',           // :
    COMMA = 'COMMA',           // ,
    DOT = 'DOT',               // .
    EQUALS = 'EQUALS',         // =
    OPERATOR = 'OPERATOR',     // + - * / % < > ! ? & | @ ^ ~

    // Brackets and parentheses
    LEFT_PAREN = 'LEFT_PAREN',      // (
    RIGHT_PAREN = 'RIGHT_PAREN',    // )
    LEFT_BRACE = 'LEFT_BRACE',      // {
    RIGHT_BRACE = 'RIGHT_BRACE',    // }
    LEFT_BRACKET = 'LEFT_BRACKET',  // [
    RIGHT_BRACKET = 'RIGHT_BRACKET', // ]

    // Special
    NEWLINE = 'NEWLINE',
    COMMENT = 'COMMENT',
    WHITESPACE = 'WHITESPACE',
    EOF = 'EOF',
}

/**
 * Position information for tokens
 */
export class SourceLocation {
    constructor(
        public line: number,
        public column: number,
        public startPos: number,
        public endPos: number,
    ) {}

    public toString(): string {
        return `Line ${this.line}, Column ${this.column} (pos ${this.startPos}-${this.endPos})`;
    }
}

/**
 * Token class with value and position tracking
 */
export class DSLToken {
    constructor(
        public type: DSLTokenType,
        public value: string,
        public location: SourceLocation,
    ) {}

    public toString(): string {
        return `${this.type}(${JSON.stringify(this.value)}) at ${this.location}`;
    }

    public is(type: DSLTokenType, value?: string): boolean {
        if (value !== undefined) {
            return this.type === type && this.value === value;
        }
        return this.type === type;
    }
}

import { DSLToken, SourceLocation } from '../lexer/DSLToken';

/**
 * Parser error with position information and expected tokens
 */
export class DSLParserError extends Error {
    constructor(
        message: string,
        public location?: SourceLocation,
        public expectedTokens?: string[],
        public actualToken?: DSLToken,
    ) {
        super(message);
        this.name = 'DSLParserError';

        // Format error message with location if available
        if (location) {
            this.message = `${message} at ${location}`;
        }

        if (expectedTokens && expectedTokens.length > 0) {
            this.message += `\nExpected: ${expectedTokens.join(', ')}`;
        }

        if (actualToken) {
            this.message += `\nActual: ${actualToken.type} (${actualToken.value})`;
        }
    }
}

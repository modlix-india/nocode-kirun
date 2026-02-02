import { SourceLocation } from './DSLToken';

/**
 * Lexer error with position information
 */
export class LexerError extends Error {
    constructor(
        message: string,
        public location?: SourceLocation,
        public context?: string,
    ) {
        super(message);
        this.name = 'LexerError';

        // Format error message with location if available
        if (location) {
            this.message = `${message} at ${location}`;
            if (context) {
                this.message += `\n\n${this.formatContext(context, location)}`;
            }
        }
    }

    private formatContext(input: string, location: SourceLocation): string {
        const lines = input.split('\n');
        const lineIndex = location.line - 1;

        if (lineIndex < 0 || lineIndex >= lines.length) {
            return '';
        }

        const line = lines[lineIndex];
        const pointer = ' '.repeat(location.column - 1) + '^';

        return `${line}\n${pointer}`;
    }
}

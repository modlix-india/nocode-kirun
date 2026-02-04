import { DSLLexer } from './lexer/DSLLexer';
import { DSLParser } from './parser/DSLParser';
import { ASTToJSONTransformer } from './transformer/ASTToJSON';
import { JSONToTextTransformer } from './transformer/JSONToText';

export interface ValidationResult {
    valid: boolean;
    errors: FormattedError[];
}

export interface FormattedError {
    message: string;
    line?: number;
    column?: number;
    snippet?: string;
}

/**
 * DSL Compiler - High-level API
 * Main entry point for DSL compilation and decompilation
 */
export class DSLCompiler {
    /**
     * Compile DSL text to FunctionDefinition JSON
     *
     * @param text DSL source text
     * @returns FunctionDefinition JSON
     */
    public static compile(text: string): any {
        // 1. Lex
        const lexer = new DSLLexer(text);
        const tokens = lexer.tokenize();

        // 2. Parse (pass original input for exact expression extraction)
        const parser = new DSLParser(tokens, text);
        const ast = parser.parse();

        // 3. Transform
        const transformer = new ASTToJSONTransformer();

        // Flatten nested blocks before transformation
        transformer.flattenNestedBlocks(ast);

        const json = transformer.transform(ast);

        return json;
    }

    /**
     * Decompile FunctionDefinition JSON to DSL text
     *
     * @param json FunctionDefinition JSON
     * @returns DSL source text
     */
    public static async decompile(json: any): Promise<string> {
        const transformer = new JSONToTextTransformer();
        return await transformer.transform(json);
    }

    /**
     * Validate DSL syntax without full compilation
     *
     * @param text DSL source text
     * @returns Validation result with errors if any
     */
    public static validate(text: string): ValidationResult {
        try {
            this.compile(text);
            return { valid: true, errors: [] };
        } catch (error: any) {
            return {
                valid: false,
                errors: [this.formatError(error)],
            };
        }
    }

    /**
     * Format DSL text (parse and regenerate with consistent formatting)
     *
     * @param text DSL source text
     * @returns Formatted DSL text
     */
    public static async format(text: string): Promise<string> {
        const json = this.compile(text);
        return await this.decompile(json);
    }

    /**
     * Format error for user-friendly display
     */
    private static formatError(error: any): FormattedError {
        const formatted: FormattedError = {
            message: error.message || 'Unknown error',
        };

        if (error.location) {
            formatted.line = error.location.line;
            formatted.column = error.location.column;
        }

        return formatted;
    }
}

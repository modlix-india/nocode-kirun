import { Expression } from '../../runtime/expression/Expression';

/**
 * Expression Handler
 * Utilities for working with KIRun expressions
 */
export class ExpressionHandler {
    /**
     * Parse expression text using KIRun Expression parser
     */
    public static parse(expressionText: string): Expression {
        return new Expression(expressionText);
    }

    /**
     * Validate expression syntax
     */
    public static validate(expressionText: string): boolean {
        try {
            new Expression(expressionText);
            return true;
        } catch {
            return false;
        }
    }

    /**
     * Check if a value is an expression (has isExpression flag)
     */
    public static isExpression(value: any): boolean {
        return (
            value &&
            typeof value === 'object' &&
            value.isExpression === true &&
            typeof value.value === 'string'
        );
    }

    /**
     * Extract expression text from value object
     */
    public static extractExpressionText(value: any): string | null {
        if (this.isExpression(value)) {
            return value.value;
        }
        return null;
    }
}

import { Expression } from '../../../runtime/expression/Expression';
import { SourceLocation } from '../../lexer/DSLToken';
import { ASTNode } from './ASTNode';

/**
 * Expression node - wraps KIRun Expression
 */
export class ExpressionNode extends ASTNode {
    public parsedExpression?: Expression;

    constructor(
        public expressionText: string,
        location: SourceLocation,
    ) {
        super('Expression', location);
    }

    /**
     * Parse the expression using KIRun Expression parser
     */
    public parse(): void {
        if (!this.parsedExpression) {
            this.parsedExpression = new Expression(this.expressionText);
        }
    }

    public toJSON(): any {
        return {
            type: this.type,
            expressionText: this.expressionText,
        };
    }
}

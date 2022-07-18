export class ExpressionToken {
    expression: string;

    public constructor(expression: string) {
        this.expression = expression;
    }

    public getExpression(): string {
        return this.expression;
    }

    public toString(): string {
        return this.expression;
    }
}

export class JsonExpression {
    private expression: string;

    public constructor(expression: string) {
        this.expression = expression;
    }

    public getExpression(): string {
        return this.expression;
    }
}

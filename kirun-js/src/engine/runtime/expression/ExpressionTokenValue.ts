import { StringFormatter } from '../../util/string/StringFormatter';
import { ExpressionToken } from './ExpressionToken';

export class ExpressionTokenValue extends ExpressionToken {
    private element: any;

    public constructor(expression: string, element: any) {
        super(expression);
        this.element = element;
    }

    public getTokenValue(): any {
        return this.element;
    }

    public getElement(): any {
        return this.element;
    }

    public toString(): string {
        return StringFormatter.format('$: $', this.expression, this.element);
    }
}

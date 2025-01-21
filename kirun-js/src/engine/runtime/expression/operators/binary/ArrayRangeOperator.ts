import { BinaryOperator } from './BinaryOperator';

export class ArrayRangeOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        return `${t ?? ''}..${u ?? ''}`;
    }
}

import { isNullValue } from '../../../../util/NullCheck';
import { BinaryOperator } from './BinaryOperator';

export class ArithmeticAdditionOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        if (isNullValue(t)) return u;
        else if (isNullValue(u)) return t;
        return t + u;
    }
}

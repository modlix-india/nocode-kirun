import { isNullValue } from '../../../../util/NullCheck';
import { BinaryOperator } from './BinaryOperator';

export class LogicalNullishCoalescingOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        return isNullValue(t) ? u : t;
    }
}

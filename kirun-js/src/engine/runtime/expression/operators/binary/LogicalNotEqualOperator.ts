import { deepEqual } from '../../../../util/deepEqual';
import { BinaryOperator } from './BinaryOperator';

export class LogicalNotEqualOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        return !deepEqual(t, u);
    }
}

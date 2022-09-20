import { deepEqual } from '../../../../util/deepEqual';
import { BinaryOperator } from './BinaryOperator';

export class LogicalEqualOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        return deepEqual(t, u);
    }
}

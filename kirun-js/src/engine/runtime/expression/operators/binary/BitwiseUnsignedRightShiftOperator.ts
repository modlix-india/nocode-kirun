import { Operation } from '../../Operation';
import { BinaryOperator } from './BinaryOperator';

export class BitwiseUnsignedRightShiftOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        this.nullCheck(t, u, Operation.BITWISE_UNSIGNED_RIGHT_SHIFT);
        return t >>> u;
    }
}

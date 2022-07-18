import { Operation } from '../../Operation';
import { BinaryOperator } from './BinaryOperator';

export class BitwiseRightShiftOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        this.nullCheck(t, u, Operation.BITWISE_RIGHT_SHIFT);
        return t >> u;
    }
}

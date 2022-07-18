import { Operation } from '../../Operation';
import { BinaryOperator } from './BinaryOperator';

export class BitwiseLeftShiftOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        this.nullCheck(t, u, Operation.BITWISE_LEFT_SHIFT);
        return t << u;
    }
}

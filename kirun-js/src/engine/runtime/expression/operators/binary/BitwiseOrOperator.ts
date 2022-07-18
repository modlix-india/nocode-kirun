import { Operation } from '../../Operation';
import { BinaryOperator } from './BinaryOperator';

export class BitwiseOrOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        this.nullCheck(t, u, Operation.BITWISE_OR);
        return t | u;
    }
}

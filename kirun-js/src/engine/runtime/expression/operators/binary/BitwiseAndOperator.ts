import { Operation } from '../../Operation';
import { BinaryOperator } from './BinaryOperator';

export class BitwiseAndOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        this.nullCheck(t, u, Operation.BITWISE_AND);
        return t & u;
    }
}

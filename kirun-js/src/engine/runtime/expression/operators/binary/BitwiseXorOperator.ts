import { Operation } from '../../Operation';
import { BinaryOperator } from './BinaryOperator';

export class BitwiseXorOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        this.nullCheck(t, u, Operation.BITWISE_XOR);
        return t ^ u;
    }
}

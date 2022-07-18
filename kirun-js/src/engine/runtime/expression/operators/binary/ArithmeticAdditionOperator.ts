import { Operation } from '../../Operation';
import { BinaryOperator } from './BinaryOperator';

export class ArithmeticAdditionOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        this.nullCheck(t, u, Operation.ADDITION);
        return t + u;
    }
}

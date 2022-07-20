import { Operation } from '../../Operation';
import { BinaryOperator } from './BinaryOperator';

export class ArithmeticIntegerDivisionOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        this.nullCheck(t, u, Operation.DIVISION);
        return Math.floor(t / u);
    }
}

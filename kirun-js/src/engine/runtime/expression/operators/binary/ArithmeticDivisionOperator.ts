import { Operation } from '../../Operation';
import { BinaryOperator } from './BinaryOperator';

export class ArithmeticDivisionOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        this.nullCheck(t, u, Operation.DIVISION);
        return t / u;
    }
}

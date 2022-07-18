import { Operation } from '../../Operation';
import { BinaryOperator } from './BinaryOperator';

export class ArithmeticModulusOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        this.nullCheck(t, u, Operation.MOD);
        return t % u;
    }
}

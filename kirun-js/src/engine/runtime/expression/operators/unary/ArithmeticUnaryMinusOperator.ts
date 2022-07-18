import { PrimitiveUtil } from '../../../../util/primitive/PrimitiveUtil';
import { Operation } from '../../Operation';
import { UnaryOperator } from './UnaryOperator';

export class ArithmeticUnaryMinusOperator extends UnaryOperator {
    public apply(t: any): any {
        this.nullCheck(t, Operation.UNARY_MINUS);

        PrimitiveUtil.findPrimitiveNumberType(t);

        return -t;
    }
}

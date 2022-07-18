import { PrimitiveUtil } from '../../../../util/primitive/PrimitiveUtil';
import { Operation } from '../../Operation';
import { UnaryOperator } from './UnaryOperator';

export class ArithmeticUnaryPlusOperator extends UnaryOperator {
    public apply(t: any): any {
        this.nullCheck(t, Operation.UNARY_PLUS);

        PrimitiveUtil.findPrimitiveNumberType(t);

        return t;
    }
}

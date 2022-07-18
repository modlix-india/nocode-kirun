import { ExecutionException } from '../../../../exception/ExecutionException';
import { SchemaType } from '../../../../json/schema/type/SchemaType';
import { PrimitiveUtil } from '../../../../util/primitive/PrimitiveUtil';
import { StringFormatter } from '../../../../util/string/StringFormatter';
import { Tuple2 } from '../../../../util/Tuples';
import { Operation } from '../../Operation';
import { UnaryOperator } from './UnaryOperator';

export class LogicalNotOperator extends UnaryOperator {
    public apply(t: any): any {
        this.nullCheck(t, Operation.UNARY_LOGICAL_NOT);

        let tType: Tuple2<SchemaType, any> = PrimitiveUtil.findPrimitiveNumberType(t);

        if (tType.getT1() != SchemaType.BOOLEAN)
            throw new ExecutionException(
                StringFormatter.format('Unable to apply bitwise operator on $', t),
            );

        return !t;
    }
}

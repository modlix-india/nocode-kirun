import { ExecutionException } from '../../../../exception/ExecutionException';
import { SchemaType } from '../../../../json/schema/type/SchemaType';
import { PrimitiveUtil } from '../../../../util/primitive/PrimitiveUtil';
import { StringFormatter } from '../../../../util/string/StringFormatter';
import { Tuple2 } from '../../../../util/Tuples';
import { BinaryOperator } from './BinaryOperator';

export class LogicalOrOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        const tType: Tuple2<SchemaType, any> = PrimitiveUtil.findPrimitiveNullAsBoolean(t);
        const uType: Tuple2<SchemaType, any> = PrimitiveUtil.findPrimitiveNullAsBoolean(u);

        if (tType.getT1() != SchemaType.BOOLEAN)
            throw new ExecutionException(
                StringFormatter.format('Boolean value expected but found $', tType.getT2()),
            );

        if (uType.getT1() != SchemaType.BOOLEAN)
            throw new ExecutionException(
                StringFormatter.format('Boolean value expected but found $', uType.getT2()),
            );

        return tType.getT2() || uType.getT2();
    }
}

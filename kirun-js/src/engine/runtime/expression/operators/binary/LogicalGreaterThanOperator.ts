import { ExecutionException } from '../../../../exception/ExecutionException';
import { SchemaType } from '../../../../json/schema/type/SchemaType';
import { isNullValue } from '../../../../util/NullCheck';
import { PrimitiveUtil } from '../../../../util/primitive/PrimitiveUtil';
import { StringFormatter } from '../../../../util/string/StringFormatter';
import { Tuple2 } from '../../../../util/Tuples';
import { BinaryOperator } from './BinaryOperator';

export class LogicalGreaterThanOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        // When data is not ready (null/undefined), return undefined instead of throwing
        if (isNullValue(t) || isNullValue(u)) return undefined;

        const tType: Tuple2<SchemaType, any> = PrimitiveUtil.findPrimitiveNullAsBoolean(t);
        const uType: Tuple2<SchemaType, any> = PrimitiveUtil.findPrimitiveNullAsBoolean(u);

        if (tType.getT1() == SchemaType.BOOLEAN || uType.getT1() == SchemaType.BOOLEAN)
            throw new ExecutionException(
                StringFormatter.format(
                    'Cannot compare > with the values $ and $',
                    tType.getT2(),
                    uType.getT2(),
                ),
            );

        return tType.getT2() > uType.getT2();
    }
}

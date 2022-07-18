import { SchemaType } from '../../../../json/schema/type/SchemaType';
import { PrimitiveUtil } from '../../../../util/primitive/PrimitiveUtil';
import { Tuple2 } from '../../../../util/Tuples';
import { BinaryOperator } from './BinaryOperator';

export class LogicalNotEqualOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        const tType: Tuple2<SchemaType, any> = PrimitiveUtil.findPrimitiveNullAsBoolean(t);
        const uType: Tuple2<SchemaType, any> = PrimitiveUtil.findPrimitiveNullAsBoolean(u);

        return tType.getT2() != uType.getT2();
    }
}

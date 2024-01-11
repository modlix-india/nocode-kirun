import { ExecutionException } from '../../../../exception/ExecutionException';
import { SchemaType } from '../../../../json/schema/type/SchemaType';
import { PrimitiveUtil } from '../../../../util/primitive/PrimitiveUtil';
import { StringFormatter } from '../../../../util/string/StringFormatter';
import { Tuple2 } from '../../../../util/Tuples';
import { BinaryOperator } from './BinaryOperator';

export class LogicalAndOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        return !!t && t !== '' && !!u && u !== '';
    }
}

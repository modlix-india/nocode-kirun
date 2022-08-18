import { ExecutionException } from '../../exception/ExecutionException';
import { KIRuntimeException } from '../../exception/KIRuntimeException';
import { SchemaType } from '../../json/schema/type/SchemaType';
import { isNullValue } from '../NullCheck';
import { StringFormatter } from '../string/StringFormatter';
import { Tuple2 } from '../Tuples';

export class PrimitiveUtil {
    public static findPrimitiveNullAsBoolean(element: any): Tuple2<SchemaType, any> {
        if (!element) return new Tuple2(SchemaType.BOOLEAN, false);

        let typof: string = typeof element;
        if (typof === 'object')
            throw new ExecutionException(
                StringFormatter.format('$ is not a primitive type', element),
            );

        let value: any = element;

        if (typof === 'boolean') return new Tuple2(SchemaType.BOOLEAN, value);

        if (typof === 'string') return new Tuple2(SchemaType.STRING, value);

        return PrimitiveUtil.findPrimitiveNumberType(value);
    }

    public static findPrimitive(element: any): Tuple2<SchemaType, any> {
        if (isNullValue(element)) return new Tuple2(SchemaType.NULL, undefined);

        let typof: string = typeof element;

        if (typof === 'object')
            throw new ExecutionException(
                StringFormatter.format('$ is not a primitive type', element),
            );

        let value: any = element;

        if (typof === 'boolean') return new Tuple2(SchemaType.BOOLEAN, value);

        if (typof === 'string') return new Tuple2(SchemaType.STRING, value);

        return PrimitiveUtil.findPrimitiveNumberType(value);
    }

    public static findPrimitiveNumberType(element: any): Tuple2<SchemaType, any> {
        if (isNullValue(element) || Array.isArray(element) || typeof element == 'object')
            throw new ExecutionException(
                StringFormatter.format('Unable to convert $ to a number.', element),
            );

        let value: any = element;

        // I think we should not forcibly convert a string to number, it was needed in java as we were using GSON.
        // if (typeof value === "string") {
        //     if (!/^[\d]{0,}[.]{0,1}[\d]{1,}$/.test(value)) //Don't do this, I need to enhance to accommadate the exponent form.
        //     throw new ExecutionException(StringFormatter.format("Unable to convert $ to a number.", value));
        //     let fv = parseFloat(value);
        //     let iv = parseInt(value);

        //     if (iv === fv)
        //         return new Tuple2(SchemaType.LONG, iv);
        //     else
        //     return new Tuple2(SchemaType.DOUBLE, fv);
        // }

        try {
            let num: number = value as number;
            if (Number.isInteger(num)) return new Tuple2(SchemaType.LONG, num);
            else return new Tuple2(SchemaType.DOUBLE, num);
        } catch (err) {
            throw new ExecutionException(
                StringFormatter.format('Unable to convert $ to a number.', value),
                err,
            );
        }
    }

    public static compareFunction(oa: any, ob: any): number {
        if (isNullValue(oa) || isNullValue(ob)) {
            if (isNullValue(oa) && isNullValue(ob)) return 0;
            return isNullValue(oa) ? -1 : 1;
        }

        if (oa == ob) return 0;

        if (typeof oa == 'string' || typeof ob == 'string') {
            return oa + '' < ob + '' ? -1 : 1;
        }

        if (typeof oa == 'number' || typeof ob == 'number') {
            return oa - ob;
        }
    }

    private static baseNumberType(num: number): SchemaType {
        if (Number.isInteger(num)) return SchemaType.LONG;
        else return SchemaType.DOUBLE;
    }

    public static toPrimitiveType(e: any): number {
        return e as number;
    }

    private constructor() {}
}

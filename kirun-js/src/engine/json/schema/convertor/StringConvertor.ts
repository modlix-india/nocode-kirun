import { Schema } from '../Schema';
import { ConversionMode } from './enums/ConversionMode';
import { isNullValue } from '../../../util/NullCheck';
import { ConvertorUtil } from '../../../util/json/ConvertorUtil';

export class StringConvertor {
    public static convert(
        parents: Schema[],
        schema: Schema,
        mode: ConversionMode,
        element: any,
    ): string | null {
        if (isNullValue(element)) {
            return ConvertorUtil.handleUnConvertibleValueWithDefault(
                parents,
                mode,
                element,
                this.getDefault(schema),
                'Expected a string but found null',
            );
        }

        const value =
            element ?? (typeof element === 'object' ? JSON.stringify(element) : String(element));

        return this.getConvertedString(value, mode);
    }

    private static getConvertedString(value: string, mode: ConversionMode): string {
        if (mode === ConversionMode.STRICT) {
            return value.toString();
        }
        return value.trim();
    }

    private static getDefault(schema: Schema): any {
        return schema.getDefaultValue() ?? null;
    }

    private constructor() {}
}

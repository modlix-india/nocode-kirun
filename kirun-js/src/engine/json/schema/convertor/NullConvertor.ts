import { Schema } from '../Schema';
import { ConversionMode } from './enums/ConversionMode';
import { isNullValue } from '../../../util/NullCheck';
import { ConvertorUtil } from '../../../util/json/ConvertorUtil';

export class NullConvertor {
    public static convert(
        parents: Schema[],
        schema: Schema,
        mode: ConversionMode,
        element: any,
    ): null {
        if (isNullValue(element)) {
            return element;
        }

        if ('string' === typeof element && element.toLowerCase() === 'null') {
            return null;
        }

        return ConvertorUtil.handleUnConvertibleValueWithDefault(
            parents,
            mode,
            element,
            null,
            'Unable to convert to null',
        );
    }

    private constructor() {}
}

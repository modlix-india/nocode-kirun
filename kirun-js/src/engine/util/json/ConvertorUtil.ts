import { Schema } from '../../json/schema/Schema';
import { ConversionMode } from '../../json/schema/convertor/enums/ConversionMode';
import { SchemaConversionException } from '../../json/schema/convertor/exception/SchemaConversionException';
import { SchemaValidator } from '../../json/schema/validator/SchemaValidator';

export class ConvertorUtil {
    public static handleUnConvertibleValue(
        parents: Schema[],
        mode: ConversionMode | null,
        element: any,
        errorMessage: string,
    ): any | null {
        return this.handleUnConvertibleValueWithDefault(parents, mode, element, null, errorMessage);
    }

    public static handleUnConvertibleValueWithDefault(
        parents: Schema[],
        mode: ConversionMode | null,
        element: any,
        defaultValue: any,
        errorMessage: string,
    ): any | null {
        if (mode === null) {
            mode = ConversionMode.STRICT;
        }

        switch (mode) {
            case ConversionMode.STRICT:
                throw new SchemaConversionException(
                    SchemaValidator.path(parents),
                    element,
                    errorMessage,
                    mode,
                );
            case ConversionMode.LENIENT:
                return null;
            case ConversionMode.USE_DEFAULT:
                return defaultValue;
            case ConversionMode.SKIP:
                return element;
            default:
                throw new SchemaConversionException(
                    SchemaValidator.path(parents),
                    element,
                    'Invalid conversion mode',
                );
        }
    }

    private constructor() {}
}

import { Schema } from '../../json/schema/Schema';
import { ConversionMode } from '../../json/schema/convertor/enums/ConversionMode';
import { SchemaValidationException } from '../../json/schema/validator/exception/SchemaValidationException';
import { SchemaValidator } from '../../json/schema/validator/SchemaValidator';

export class ValidatorUtil {
    public static handleValidationError(
        parents: Schema[],
        mode: ConversionMode,
        element: any,
        defaultValue: any,
        errorMessage: string,
    ): any | null {
        mode = mode ?? ConversionMode.STRICT;

        switch (mode) {
            case ConversionMode.STRICT:
                throw new SchemaValidationException(SchemaValidator.path(parents), errorMessage);
            case ConversionMode.LENIENT:
                return null;
            case ConversionMode.USE_DEFAULT:
                return defaultValue;
            case ConversionMode.SKIP:
                return element;
        }
    }

    private constructor() {}
}

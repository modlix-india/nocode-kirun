import { isNullValue } from '../../../util/NullCheck';
import { Schema } from '../Schema';
import { SchemaValidationException } from './exception/SchemaValidationException';
import { SchemaValidator } from './SchemaValidator';

export class BooleanValidator {
    public static validate(parents: Schema[], schema: Schema, element: any): any {
        if (isNullValue(element))
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'Expected a boolean but found null',
            );

        if (typeof element !== 'boolean')
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                element.toString() + ' is not a boolean',
            );

        return element;
    }

    private constructor() {}
}

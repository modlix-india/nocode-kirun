import { Schema } from '../Schema';
import { SchemaValidationException } from './exception/SchemaValidationException';
import { SchemaValidator } from './SchemaValidator';

export class NullValidator {
    public static validate(parents: Schema[], schema: Schema, element: any): any {
        if (element != null && element)
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'Expected a null but found ' + element,
            );

        return element;
    }

    private NullValidator() {}
}

import { isNullValue } from '../../../util/NullCheck';
import { Schema } from '../Schema';
import { StringFormat } from '../string/StringFormat';
import { SchemaValidationException } from './exception/SchemaValidationException';
import { SchemaValidator } from './SchemaValidator';

export class StringValidator {
    private static readonly TIME: RegExp =
        /^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?([+-][01][0-9]:[0-5][0-9])?$/;

    private static readonly DATE: RegExp =
        /^[0-9]{4,4}-([0][0-9]|[1][0-2])-(0[1-9]|[1-2][1-9]|3[01])$/;

    private static readonly DATETIME: RegExp =
        /^[0-9]{4,4}-([0][0-9]|[1][0-2])-(0[1-9]|[1-2][1-9]|3[01])T([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?([+-][01][0-9]:[0-5][0-9])?$/;

    private static readonly EMAIL: RegExp =
        /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/;

    public static validate(parents: Schema[], schema: Schema, element: any): any {
        if (isNullValue(element))
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'Expected a string but found ' + element,
            );

        if (typeof element !== 'string')
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                element.toString() + ' is not String',
            );

        if (schema.getFormat() == StringFormat.TIME) {
            StringValidator.patternMatcher(
                parents,
                schema,
                element,
                StringValidator.TIME,
                'time pattern',
            );
        } else if (schema.getFormat() == StringFormat.DATE) {
            StringValidator.patternMatcher(
                parents,
                schema,
                element,
                StringValidator.DATE,
                'date pattern',
            );
        } else if (schema.getFormat() == StringFormat.DATETIME) {
            StringValidator.patternMatcher(
                parents,
                schema,
                element,
                StringValidator.DATETIME,
                'date time pattern',
            );
        } else if (schema.getFormat() == StringFormat.EMAIL) {
            StringValidator.patternMatcher(
                parents,
                schema,
                element,
                StringValidator.EMAIL,
                'email pattern',
            );
        } else if (schema.getPattern()) {
            StringValidator.patternMatcher(
                parents,
                schema,
                element,
                new RegExp(schema.getPattern()!),
                'pattern ' + schema.getPattern(),
            );
        }

        let length: number = element.length;
        if (schema.getMinLength() && length < schema.getMinLength()!) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                schema.getDetails()?.getValidationMessage('minLength') ??
                'Expected a minimum of ' + schema.getMinLength() + ' characters',
            );
        } else if (schema.getMaxLength() && length > schema.getMaxLength()!) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                schema.getDetails()?.getValidationMessage('maxLength') ??
                'Expected a maximum of ' + schema.getMaxLength() + ' characters',
            );
        }

        return element;
    }

    private static patternMatcher(
        parents: Schema[],
        schema: Schema,
        element: any,
        pattern: RegExp,
        message: string,
    ) {
        let matched: boolean = pattern.test(element);
        if (!matched) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                schema.getDetails()?.getValidationMessage('pattern') ??
                element.toString() + ' is not matched with the ' + message,
            );
        }
    }

    private constructor() {}
}

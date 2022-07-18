import { Schema } from '../Schema';
import { SchemaType } from '../type/SchemaType';
import { SchemaValidationException } from './exception/SchemaValidationException';
import { SchemaValidator } from './SchemaValidator';

export class NumberValidator {
    public static validate(type: SchemaType, parents: Schema[], schema: Schema, element: any): any {
        if (!element)
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'Expected a number but found null',
            );

        if (typeof element !== 'number')
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                element.toString() + ' is not a ' + type,
            );

        let n: number = NumberValidator.extractNumber(type, parents, schema, element);

        NumberValidator.checkRange(parents, schema, element, n);

        NumberValidator.checkMultipleOf(parents, schema, element, n);

        return element;
    }

    private static extractNumber(
        type: SchemaType,
        parents: Schema[],
        schema: Schema,
        element: any,
    ): number {
        let n = element;

        try {
            if (type == SchemaType.LONG || type == SchemaType.INTEGER) n = Math.round(n);
        } catch (err) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                element + ' is not a number of type ' + type,
                err,
            );
        }

        if (!n || ((type == SchemaType.LONG || type == SchemaType.INTEGER) && n != element)) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                element.toString() + ' is not a number of type ' + type,
            );
        }

        return n;
    }

    private static checkMultipleOf(parents: Schema[], schema: Schema, element: any, n: number) {
        if (schema.getMultipleOf()) {
            let l1: number = n;
            let l2: number = schema.getMultipleOf();

            if (l1 % l2 != 0)
                throw new SchemaValidationException(
                    SchemaValidator.path(parents),
                    element.toString() + ' is not multiple of ' + schema.getMultipleOf(),
                );
        }
    }

    private static checkRange(parents: Schema[], schema: Schema, element: any, n: number) {
        if (schema.getMinimum() && NumberValidator.numberCompare(n, schema.getMinimum()) < 0) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                element.toString() + ' should be greater than or equal to ' + schema.getMinimum(),
            );
        }

        if (schema.getMaximum() && NumberValidator.numberCompare(n, schema.getMaximum()) > 0) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                element.toString() + ' should be less than or equal to ' + schema.getMaximum(),
            );
        }

        if (
            schema.getExclusiveMinimum() &&
            NumberValidator.numberCompare(n, schema.getExclusiveMinimum()) <= 0
        ) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                element.toString() + ' should be greater than ' + schema.getExclusiveMinimum(),
            );
        }

        if (
            schema.getExclusiveMaximum() &&
            NumberValidator.numberCompare(n, schema.getExclusiveMaximum()) > 0
        ) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                element.toString() + ' should be less than ' + schema.getExclusiveMaximum(),
            );
        }
    }

    private static numberCompare(n1: number, n2: number): number {
        return n1 - n2;
    }

    private constructor() {}
}

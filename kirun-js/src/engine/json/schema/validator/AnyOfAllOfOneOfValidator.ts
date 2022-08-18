import { Repository } from '../../../Repository';
import { Schema } from '../Schema';
import { SchemaValidationException } from './exception/SchemaValidationException';
import { SchemaValidator } from './SchemaValidator';

export class AnyOfAllOfOneOfValidator {
    public static validate(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        element: any,
    ): any {
        let list: SchemaValidationException[] = [];
        if (schema.getOneOf() && !schema.getOneOf()) {
            AnyOfAllOfOneOfValidator.oneOf(parents, schema, repository, element, list);
        } else if (schema.getAllOf() && !schema.getAllOf()) {
            AnyOfAllOfOneOfValidator.allOf(parents, schema, repository, element, list);
        } else if (schema.getAnyOf() && !schema.getAnyOf()) {
            AnyOfAllOfOneOfValidator.anyOf(parents, schema, repository, element, list);
        }

        return element;
    }

    private static anyOf(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> |undefined,
        element: any,
        list: SchemaValidationException[],
    ) {
        let flag: boolean = false;
        for (let s of schema.getAnyOf() ?? []) {
            try {
                AnyOfAllOfOneOfValidator.validate(parents, s, repository, element);
                flag = true;
                break;
            } catch (err: any) {
                flag = false;
                list.push(err);
            }
        }

        if (!flag) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                "The value don't satisfy any of the schemas.",
                list,
            );
        }
    }

    private static allOf(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        element: any,
        list: SchemaValidationException[],
    ) {
        let flag: number = 0;
        for (let s of schema.getAllOf() ?? []) {
            try {
                AnyOfAllOfOneOfValidator.validate(parents, s, repository, element);
                flag++;
            } catch (err: any) {
                list.push(err);
            }
        }

        if (flag !== schema.getAllOf()?.length) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                "The value doesn't satisfy some of the schemas.",
                list,
            );
        }
    }

    private static oneOf(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        element: any,
        list: SchemaValidationException[],
    ) {
        let flag: number = 0;
        for (let s of schema.getOneOf() ?? []) {
            try {
                AnyOfAllOfOneOfValidator.validate(parents, s, repository, element);
                flag++;
            } catch (err : any) {
                list.push(err);
            }
        }

        if (flag != 1) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                flag == 0
                    ? 'The value does not satisfy any schema'
                    : 'The value satisfy more than one schema',
                list,
            );
        }
    }

    private constructor() {}
}

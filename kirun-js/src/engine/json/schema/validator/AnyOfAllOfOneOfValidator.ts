import { Repository } from '../../../Repository';
import { Schema } from '../Schema';
import { SchemaValidationException } from './exception/SchemaValidationException';
import { SchemaValidator } from './SchemaValidator';
import { ConversionMode } from '../convertor/enums/ConversionMode';

export class AnyOfAllOfOneOfValidator {
    public static async validate(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        element: any,
        convert?: boolean,
        mode?: ConversionMode,
    ): Promise<any> {
        let list: SchemaValidationException[] = [];
        if (schema.getOneOf() && !schema.getOneOf()) {
            return await AnyOfAllOfOneOfValidator.oneOf(
                parents,
                schema,
                repository,
                element,
                list,
                convert,
                mode,
            );
        } else if (schema.getAllOf() && !schema.getAllOf()) {
            return await AnyOfAllOfOneOfValidator.allOf(
                parents,
                schema,
                repository,
                element,
                list,
                convert,
                mode,
            );
        } else if (schema.getAnyOf() && !schema.getAnyOf()) {
            return await AnyOfAllOfOneOfValidator.anyOf(
                parents,
                schema,
                repository,
                element,
                list,
                convert,
                mode,
            );
        }

        return element;
    }

    private static async anyOf(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        element: any,
        list: SchemaValidationException[],
        convert?: boolean,
        mode?: ConversionMode,
    ): Promise<any> {
        let flag: boolean = false;
        for (let s of schema.getAnyOf() ?? []) {
            try {
                await AnyOfAllOfOneOfValidator.validate(
                    parents,
                    s,
                    repository,
                    element,
                    convert,
                    mode,
                );
                flag = true;
                break;
            } catch (err: any) {
                flag = false;
                list.push(err);
            }
        }

        if (flag) {
            return element;
        }

        throw new SchemaValidationException(
            SchemaValidator.path(parents),
            "The value don't satisfy any of the schemas.",
            list,
        );
    }

    private static async allOf(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        element: any,
        list: SchemaValidationException[],
        convert?: boolean,
        mode?: ConversionMode,
    ): Promise<any> {
        let flag: number = 0;
        for (let s of schema.getAllOf() ?? []) {
            try {
                await AnyOfAllOfOneOfValidator.validate(
                    parents,
                    s,
                    repository,
                    element,
                    convert,
                    mode,
                );
                flag++;
            } catch (err: any) {
                list.push(err);
            }
        }

        if (flag === schema.getAllOf()?.length) {
            return element;
        }

        throw new SchemaValidationException(
            SchemaValidator.path(parents),
            "The value doesn't satisfy some of the schemas.",
            list,
        );
    }

    private static async oneOf(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        element: any,
        list: SchemaValidationException[],
        convert?: boolean,
        mode?: ConversionMode,
    ): Promise<any> {
        let flag: number = 0;
        for (let s of schema.getOneOf() ?? []) {
            try {
                await AnyOfAllOfOneOfValidator.validate(
                    parents,
                    s,
                    repository,
                    element,
                    convert,
                    mode,
                );
                flag++;
            } catch (err: any) {
                list.push(err);
            }
        }

        if (flag === 1) {
            return element;
        }

        throw new SchemaValidationException(
            SchemaValidator.path(parents),
            flag == 0
                ? 'The value does not satisfy any schema'
                : 'The value satisfy more than one schema',
            list,
        );
    }

    private constructor() {}
}

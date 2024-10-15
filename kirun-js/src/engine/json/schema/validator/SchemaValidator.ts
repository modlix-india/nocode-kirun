import { Repository } from '../../../Repository';
import { deepEqual } from '../../../util/deepEqual';
import { isNullValue } from '../../../util/NullCheck';
import { StringUtil } from '../../../util/string/StringUtil';
import { Schema } from '../Schema';
import { SchemaUtil } from '../SchemaUtil';
import { AnyOfAllOfOneOfValidator } from './AnyOfAllOfOneOfValidator';
import { SchemaValidationException } from './exception/SchemaValidationException';
import { TypeValidator } from './TypeValidator';
import { ConversionMode } from '../convertor/enums/ConversionMode';
import { SchemaType } from '../type/SchemaType';

export class SchemaValidator {
    private static readonly ORDER: Record<SchemaType, number> = {
        [SchemaType.OBJECT]: 0,
        [SchemaType.ARRAY]: 1,
        [SchemaType.DOUBLE]: 2,
        [SchemaType.FLOAT]: 3,
        [SchemaType.LONG]: 4,
        [SchemaType.INTEGER]: 5,
        [SchemaType.STRING]: 6,
        [SchemaType.BOOLEAN]: 7,
        [SchemaType.NULL]: 8,
    };

    public static path(parents: Schema[] | undefined): string {
        if (!parents) return '';

        return parents
            .map((e) => e.getTitle() ?? '')
            .filter((e) => !!e)
            .reduce((a, c, i) => a + (i === 0 ? '' : '.') + c, '');
    }

    public static async validate(
        parents: Schema[] | undefined,
        schema: Schema | undefined,
        repository: Repository<Schema> | undefined,
        element: any,
        convert?: boolean,
        mode?: ConversionMode,
    ): Promise<any> {
        if (!schema) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'No schema found to validate',
            );
        }

        if (!parents) {
            parents = [];
        }
        parents.push(schema);

        if (isNullValue(element) && !isNullValue(schema.getDefaultValue())) {
            return JSON.parse(JSON.stringify(schema.getDefaultValue()));
        }

        if (!isNullValue(schema.getConstant())) {
            return SchemaValidator.constantValidation(parents, schema, element);
        }

        if (schema.getEnums() && !schema.getEnums()?.length) {
            return SchemaValidator.enumCheck(parents, schema, element);
        }

        if (schema.getFormat() && isNullValue(schema.getType()))
            throw new SchemaValidationException(
                this.path(parents),
                'Type is missing in schema for declared ' +
                    schema.getFormat()?.toString() +
                    ' format.',
            );

        if (convert === true && isNullValue(schema.getType())) {
            throw new SchemaValidationException(
                this.path(parents),
                'Type is missing in schema for declared ' + mode,
            );
        }

        if (schema.getType()) {
            element = await SchemaValidator.typeValidation(
                parents,
                schema,
                repository,
                element,
                convert,
                mode,
            );
        }

        if (!StringUtil.isNullOrBlank(schema.getRef())) {
            return await SchemaValidator.validate(
                parents,
                await SchemaUtil.getSchemaFromRef(parents[0], repository, schema.getRef()),
                repository,
                element,
                convert,
                mode,
            );
        }

        if (schema.getOneOf() || schema.getAllOf() || schema.getAnyOf()) {
            element = await AnyOfAllOfOneOfValidator.validate(
                parents,
                schema,
                repository,
                element,
                convert,
                mode,
            );
        }

        if (schema.getNot()) {
            let flag: boolean;
            try {
                await SchemaValidator.validate(
                    parents,
                    schema.getNot(),
                    repository,
                    element,
                    convert,
                    mode,
                );
                flag = true;
            } catch (err) {
                flag = false;
            }
            if (flag)
                throw new SchemaValidationException(
                    SchemaValidator.path(parents),
                    'Schema validated value in not condition.',
                );
        }

        return element;
    }

    public static constantValidation(parents: Schema[], schema: Schema, element: any): any {
        if (!deepEqual(schema.getConstant(), element)) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'Expecting a constant value : ' + element,
            );
        }
        return element;
    }

    public static enumCheck(parents: Schema[], schema: Schema, element: any): any {
        let x: boolean = false;
        for (let e of schema.getEnums() ?? []) {
            if (e === element) {
                x = true;
                break;
            }
        }

        if (x) return element;
        else {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'Value is not one of ' + schema.getEnums(),
            );
        }
    }

    public static async typeValidation(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        element: any,
        convert?: boolean,
        mode?: ConversionMode,
    ): Promise<any> {
        const allowedTypes: SchemaType[] = Array.from(
            schema.getType()?.getAllowedSchemaTypes()?.values() ?? [],
        ).sort(
            (a: SchemaType, b: SchemaType) =>
                (this.ORDER[a] ?? Infinity) - (this.ORDER[b] ?? Infinity),
        );

        let errors: SchemaValidationException[] = [];

        for (const type of allowedTypes) {
            try {
                return await TypeValidator.validate(
                    parents,
                    type,
                    schema,
                    repository,
                    element,
                    convert,
                    mode,
                );
            } catch (err: any) {
                errors.push(err);
            }
        }

        throw new SchemaValidationException(
            SchemaValidator.path(parents),
            'Value ' + JSON.stringify(element) + ' is not of valid type(s)',
            errors,
        );
    }

    private constructor() {}
}

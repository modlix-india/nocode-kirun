import { Repository } from '../../../Repository';
import { isNullValue } from '../../../util/NullCheck';
import { StringUtil } from '../../../util/string/StringUtil';
import { Schema } from '../Schema';
import { SchemaUtil } from '../SchemaUtil';
import { SchemaType } from '../type/SchemaType';
import { AnyOfAllOfOneOfValidator } from './AnyOfAllOfOneOfValidator';
import { SchemaValidationException } from './exception/SchemaValidationException';
import { TypeValidator } from './TypeValidator';

export class SchemaValidator {
    public static path(parents: Schema[] | undefined): string {
        if (!parents) return '';

        return parents
            .map((e) => e.getTitle() ?? '')
            .filter((e) => !!e)
            .reduce((a, c, i) => a + (i === 0 ? '' : '.') + c, '');
    }

    public static validate(
        parents: Schema[] | undefined,
        schema: Schema | undefined,
        repository: Repository<Schema> | undefined,
        element: any,
    ): any {
        if (!schema) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'No schema found to validate',
            );
        }

        if (!parents) {
            parents = new Array();
        }
        parents.push(schema);

        if (isNullValue(element) && !isNullValue(schema.getDefaultValue())) {
            return JSON.parse(JSON.stringify(schema.getDefaultValue()));
        }

        if (schema.getConstant()) {
            return SchemaValidator.constantValidation(parents, schema, element);
        }

        if (schema.getEnums() && !schema.getEnums()?.length) {
            return SchemaValidator.enumCheck(parents, schema, element);
        }

        if (schema.getType()) {
            SchemaValidator.typeValidation(parents, schema, repository, element);
        }

        if (!StringUtil.isNullOrBlank(schema.getRef())) {
            return SchemaValidator.validate(
                parents,
                SchemaUtil.getSchemaFromRef(parents[0], repository, schema.getRef()),
                repository,
                element,
            );
        }

        if (schema.getOneOf() || schema.getAllOf() || schema.getAnyOf()) {
            AnyOfAllOfOneOfValidator.validate(parents, schema, repository, element);
        }

        if (schema.getNot()) {
            let flag: boolean = false;
            try {
                SchemaValidator.validate(parents, schema.getNot(), repository, element);
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
        if (!schema.getConstant().equals(element)) {
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

    public static typeValidation(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        element: any,
    ) {
        let valid: boolean = false;
        let list: SchemaValidationException[] = [];

        for (const type of Array.from(schema.getType()?.getAllowedSchemaTypes()?.values() ?? [])) {
            try {
                TypeValidator.validate(parents, type, schema, repository, element);
                valid = true;
                break;
            } catch (err: any) {
                valid = false;
                list.push(err);
            }
        }

        if (!valid) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'Value ' + JSON.stringify(element) + ' is not of valid type(s)',
                list,
            );
        }
    }

    private constructor() {}
}

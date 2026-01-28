import { Repository } from '../../../Repository';
import { ErrorMessageFormatter } from '../../../util/ErrorMessageFormatter';
import { isNullValue } from '../../../util/NullCheck';
import { AdditionalType, Schema } from '../Schema';
import { SchemaUtil } from '../SchemaUtil';
import { SchemaValidationException } from './exception/SchemaValidationException';
import { SchemaValidator } from './SchemaValidator';
import { ConversionMode } from '../convertor/enums/ConversionMode';

export class ObjectValidator {
    public static async validate(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        element: any,
        convert?: boolean,
        mode?: ConversionMode,
    ): Promise<any> {
        if (isNullValue(element))
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'Expected an object but found null',
            );

        if (typeof element !== 'object' || Array.isArray(element))
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                `Expected an object but found ${ErrorMessageFormatter.formatValue(element)}`,
            );

        let jsonObject: any = element;
        let keys: Set<string> = new Set<string>(Object.keys(jsonObject));

        ObjectValidator.checkMinMaxProperties(parents, schema, keys);

        if (schema.getPropertyNames()) {
            await ObjectValidator.checkPropertyNameSchema(parents, schema, repository, keys);
        }

        if (schema.getRequired()) {
            ObjectValidator.checkRequired(parents, schema, jsonObject);
        }

        if (schema.getProperties()) {
            await ObjectValidator.checkProperties(
                parents,
                schema,
                repository,
                jsonObject,
                keys,
                convert,
                mode,
            );
        }

        if (schema.getPatternProperties()) {
            await ObjectValidator.checkPatternProperties(
                parents,
                schema,
                repository,
                jsonObject,
                keys,
            );
        }

        if (schema.getAdditionalProperties()) {
            await ObjectValidator.checkAdditionalProperties(
                parents,
                schema,
                repository,
                jsonObject,
                keys,
            );
        }

        return jsonObject;
    }

    private static async checkPropertyNameSchema(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        keys: Set<String>,
    ) {
        for (let key of Array.from(keys.values())) {
            try {
                await SchemaValidator.validate(
                    parents,
                    schema.getPropertyNames() as Schema,
                    repository,
                    key,
                );
            } catch (err) {
                throw new SchemaValidationException(
                    SchemaValidator.path(parents),
                    "Property name '" + key + "' does not fit the property schema",
                );
            }
        }
    }

    private static checkRequired(parents: Schema[], schema: Schema, jsonObject: any) {
        for (const key of schema.getRequired() ?? []) {
            if (isNullValue(jsonObject[key])) {
                throw new SchemaValidationException(
                    SchemaValidator.path(parents),
                    schema.getProperties()?.get(key)?.getDetails()?.getValidationMessage('mandatory') ??
                    key + ' is mandatory',
                );
            }
        }
    }

    private static async checkAdditionalProperties(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        jsonObject: any,
        keys: Set<string>,
    ) {
        let apt: AdditionalType = schema.getAdditionalProperties()!;
        if (apt.getSchemaValue()) {
            for (let key of Array.from(keys.values())) {
                let newParents: Schema[] = !parents ? [] : [...parents];

                jsonObject[key] = await SchemaValidator.validate(
                    newParents,
                    apt.getSchemaValue(),
                    repository,
                    jsonObject[key],
                );
            }
        } else {
            if (apt.getBooleanValue() === false && keys.size) {
                throw new SchemaValidationException(
                    SchemaValidator.path(parents),
                    Array.from(keys) + ' is/are additional properties which are not allowed.',
                );
            }
        }
    }

    private static async checkPatternProperties(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        jsonObject: any,
        keys: Set<string>,
    ) {
        const compiledPatterns: Map<string, RegExp> = new Map<string, RegExp>();
        for (const keyPattern of Array.from(schema.getPatternProperties()!.keys()))
            compiledPatterns.set(keyPattern, new RegExp(keyPattern));

        for (const key of Array.from(keys.values())) {
            const newParents: Schema[] = !parents ? [] : [...parents];

            for (const e of Array.from(compiledPatterns.entries())) {
                if (e[1].test(key)) {
                    jsonObject[key] = await SchemaValidator.validate(
                        newParents,
                        schema.getPatternProperties()!.get(e[0]),
                        repository,
                        jsonObject[key],
                    );
                    keys.delete(key);
                    break;
                }
            }
        }
    }

    private static async checkProperties(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        jsonObject: any,
        keys: Set<string>,
        convert?: boolean,
        mode?: ConversionMode,
    ) {
        for (const each of Array.from(schema.getProperties()!)) {
            let value: any = jsonObject[each[0]];

            if (!jsonObject.hasOwnProperty(each[0]) && isNullValue(value)) {
                const defValue = await SchemaUtil.getDefaultValue(each[1], repository);
                if (isNullValue(defValue)) continue;
            }

            let newParents: Schema[] = !parents ? [] : [...parents];
            jsonObject[each[0]] = await SchemaValidator.validate(
                newParents,
                each[1],
                repository,
                value,
                convert,
                mode,
            );
            keys.delete(each[0]);
        }
    }

    private static checkMinMaxProperties(parents: Schema[], schema: Schema, keys: Set<string>) {
        if (schema.getMinProperties() && keys.size < schema.getMinProperties()!) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'Object should have minimum of ' + schema.getMinProperties() + ' properties',
            );
        }

        if (schema.getMaxProperties() && keys.size > schema.getMaxProperties()!) {
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'Object can have maximum of ' + schema.getMaxProperties() + ' properties',
            );
        }
    }
}

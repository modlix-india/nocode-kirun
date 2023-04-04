import { Repository } from '../../../Repository';
import { isNullValue } from '../../../util/NullCheck';
import { AdditionalType, Schema } from '../Schema';
import { SchemaUtil } from '../SchemaUtil';
import { SchemaValidationException } from './exception/SchemaValidationException';
import { SchemaValidator } from './SchemaValidator';

export class ObjectValidator {
    public static validate(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        element: any,
    ) {
        if (isNullValue(element))
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                'Expected an object but found null',
            );

        if (typeof element !== 'object' || Array.isArray(element))
            throw new SchemaValidationException(
                SchemaValidator.path(parents),
                element.toString() + ' is not an Object',
            );

        let jsonObject: any = element;
        let keys: Set<string> = new Set<string>(Object.keys(jsonObject));

        ObjectValidator.checkMinMaxProperties(parents, schema, keys);

        if (schema.getPropertyNames()) {
            ObjectValidator.checkPropertyNameSchema(parents, schema, repository, keys);
        }

        if (schema.getRequired()) {
            ObjectValidator.checkRequired(parents, schema, jsonObject);
        }

        if (schema.getProperties()) {
            ObjectValidator.checkProperties(parents, schema, repository, jsonObject, keys);
        }

        if (schema.getPatternProperties()) {
            ObjectValidator.checkPatternProperties(parents, schema, repository, jsonObject, keys);
        }

        if (schema.getAdditionalProperties()) {
            ObjectValidator.checkAddtionalProperties(parents, schema, repository, jsonObject, keys);
        }
    }

    private static checkPropertyNameSchema(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        keys: Set<String>,
    ) {
        for (let key of Array.from(keys.values())) {
            try {
                SchemaValidator.validate(
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
                    key + ' is mandatory',
                );
            }
        }
    }

    private static checkAddtionalProperties(
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

                let element: any = SchemaValidator.validate(
                    newParents,
                    apt.getSchemaValue(),
                    repository,
                    jsonObject[key],
                );
                jsonObject[key] = element;
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

    private static checkPatternProperties(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        jsonObject: any,
        keys: Set<string>,
    ) {
        const compiledPatterns: Map<string, RegExp> = new Map<string, RegExp>();
        for (const keyPattern of Array.from(schema.getPatternProperties()!.keys()))
            compiledPatterns.set(keyPattern, new RegExp(keyPattern));

        let goodKeys: string[] = [];

        for (const key of Array.from(keys.values())) {
            const newParents: Schema[] = !parents ? [] : [...parents];

            for (const e of Array.from(compiledPatterns.entries())) {
                if (e[1].test(key)) {
                    const element: any = SchemaValidator.validate(
                        newParents,
                        schema.getPatternProperties()!.get(e[0]),
                        repository,
                        jsonObject[key],
                    );
                    jsonObject[key] = element;
                    keys.delete(key);
                    break;
                }
            }
        }
    }

    private static checkProperties(
        parents: Schema[],
        schema: Schema,
        repository: Repository<Schema> | undefined,
        jsonObject: any,
        keys: Set<string>,
    ) {
        for (const each of Array.from(schema.getProperties()!)) {
            let value: any = jsonObject[each[0]];

            if (!jsonObject.hasOwnProperty(each[0]) && isNullValue(value)) {
                const defValue = SchemaUtil.getDefaultValue(each[1], repository);
                if (isNullValue(defValue)) continue;
            }

            let newParents: Schema[] = !parents ? [] : [...parents];
            let element: any = SchemaValidator.validate(newParents, each[1], repository, value);
            jsonObject[each[0]] = element;
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

    private constructor() {}
}

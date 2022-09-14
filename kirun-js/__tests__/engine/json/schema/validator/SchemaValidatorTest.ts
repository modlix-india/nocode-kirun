import { HybridRepository } from '../../../../../src';
import { Schema } from '../../../../../src/engine/json/schema/Schema';
import { SchemaType } from '../../../../../src/engine/json/schema/type/SchemaType';
import { TypeUtil } from '../../../../../src/engine/json/schema/type/TypeUtil';
import { SchemaValidationException } from '../../../../../src/engine/json/schema/validator/exception/SchemaValidationException';
import { SchemaValidator } from '../../../../../src/engine/json/schema/validator/SchemaValidator';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';

const repo = new KIRunSchemaRepository();

test('Schema Validator Test 1', () => {
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.INTEGER));

    expect(SchemaValidator.validate([], schema, repo, 2)).toBe(2);

    let obj = { name: 'shagil' };
    let objSchema: Schema = Schema.ofObject('testObj')
        .setProperties(new Map<string, Schema>([['name', Schema.ofString('name')]]))
        .setRequired(['name']);
    expect(SchemaValidator.validate([], objSchema, repo, obj)).toBe(obj);

    expect(() => SchemaValidator.validate([], objSchema, repo, { name: 123 })).toThrow(
        'Value {"name":123} is not of valid type(s)\nValue 123 is not of valid type(s)\n123 is not String',
    );

    // expect(SchemaValidator.validate([], schema, repo, 2.5)).toThrowError(new SchemaValidationException('', '2.5 is not a number of type Integer'));
});

test('Schema Validator Test 2', () => {
    const locationSchema = Schema.from({
        name: 'Location',
        namespace: 'Test',
        type: 'Object',
        properties: {
            url: { name: 'url', type: 'String' },
        },
        required: ['url'],
    });

    const repo = new HybridRepository<Schema>(
        {
            find(namespace, name): Schema | undefined {
                if (namespace === 'Test' && name === 'Location') {
                    return locationSchema;
                }
                return undefined;
            },
        },
        new KIRunSchemaRepository(),
    );

    const obj = { url: 'http://xxxx.com' };

    expect(SchemaValidator.validate(undefined, Schema.ofRef('Test.Location'), repo, obj)).toBe(obj);
});

test('Validate null for ofAny schema', () => {
    expect(
        SchemaValidator.validate(undefined, Schema.ofAny('ofanyundefined'), undefined, undefined),
    ).toBe(undefined);
});

test('Schema Validator Test 3', () => {
    const obj = { url: 'http://xxxx.com' };

    const locationSchema = Schema.from({
        name: 'Location',
        namespace: 'Test',
        type: 'Object',
        properties: {
            url: { name: 'url', type: 'String' },
        },
        required: ['url'],
        defaultValue: obj,
    });

    const repo = new HybridRepository<Schema>(
        {
            find(namespace, name): Schema | undefined {
                if (namespace === 'Test' && name === 'Location') {
                    return locationSchema;
                }
                return undefined;
            },
        },
        new KIRunSchemaRepository(),
    );

    const obj1 = { url: 'http://yyyy.com' };

    expect(
        SchemaValidator.validate(
            undefined,
            Schema.ofRef('Test.Location').setDefaultValue(obj1),
            repo,
            undefined,
        ),
    ).toMatchObject(obj1);
});

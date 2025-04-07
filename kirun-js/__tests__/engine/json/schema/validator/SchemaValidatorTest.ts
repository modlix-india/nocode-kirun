import { AdditionalType, HybridRepository, Repository } from '../../../../../src';
import { Schema } from '../../../../../src/engine/json/schema/Schema';
import { SchemaType } from '../../../../../src/engine/json/schema/type/SchemaType';
import { TypeUtil } from '../../../../../src/engine/json/schema/type/TypeUtil';
import { SchemaValidationException } from '../../../../../src/engine/json/schema/validator/exception/SchemaValidationException';
import { SchemaValidator } from '../../../../../src/engine/json/schema/validator/SchemaValidator';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';

const repo = new KIRunSchemaRepository();

test('Schema Validator Test 1', async () => {
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.INTEGER));

    expect(await SchemaValidator.validate([], schema, repo, 2)).toBe(2);

    let obj = { name: 'shagil' };
    let objSchema: Schema = Schema.ofObject('testObj')
        .setProperties(new Map<string, Schema>([['name', Schema.ofString('name')]]))
        .setRequired(['name']);
    expect(await SchemaValidator.validate([], objSchema, repo, obj)).toBe(obj);

    expect(SchemaValidator.validate([], objSchema, repo, { name: 123 })).rejects.toThrow(
        'Value {"name":123} is not of valid type(s)\nValue 123 is not of valid type(s)\n123 is not String',
    );

    // expect(await SchemaValidator.validate([], schema, repo, 2.5)).toThrowError(new SchemaValidationException('', '2.5 is not a number of type Integer'));
});

test('Schema validation when ref of ref', async () => {
    const locationSchema = Schema.from({
        name: 'Location',
        namespace: 'Test',
        type: 'OBJECT',
        properties: {
            url: { name: 'url', type: 'STRING' },
        },
        required: ['url'],
    });

    const urlParamsSchema = Schema.ofObject('UrlParameters')
        .setNamespace('Test')
        .setAdditionalProperties(new AdditionalType().setSchemaValue(Schema.ofRef(`Test.Location`)))
        .setDefaultValue({});

    const testSchema = Schema.ofObject('TestSchema')
        .setNamespace('Test')
        .setAdditionalProperties(
            new AdditionalType().setSchemaValue(Schema.ofRef(`Test.UrlParameters`)),
        )
        .setDefaultValue({});

    const schemaMap = new Map([
        ['Location', locationSchema],
        ['UrlParameters', urlParamsSchema],
        ['TestSchema', testSchema],
    ]);

    class X implements Repository<Schema> {
        async find(namespace: string, name: string): Promise<Schema | undefined> {
            if (namespace !== 'Test') return undefined;
            return schemaMap.get(name);
        }

        async filter(name: string): Promise<string[]> {
            return Array.from(schemaMap.values())
                .map((e) => e!.getFullName())
                .filter((e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1);
        }
    }

    const repo = new HybridRepository<Schema>(new X(), new KIRunSchemaRepository());
    const obj = { obj: { url: 'http://xxxxxx.com' } };
    const queryParams = {
        obj: { obj: { url: 'http://xxxxxx.com' } },
    };

    expect(
        await SchemaValidator.validate(
            undefined,
            Schema.ofRef('Test.TestSchema'),
            repo,
            queryParams,
        ),
    ).toBe(queryParams);
});

test('Schema Validator Test 2', async () => {
    const locationSchema = Schema.from({
        name: 'Location',
        namespace: 'Test',
        type: 'OBJECT',
        properties: {
            url: { name: 'url', type: 'STRING' },
        },
        required: ['url'],
    });

    const repo = new HybridRepository<Schema>(
        {
            async find(namespace, name): Promise<Schema | undefined> {
                if (namespace === 'Test' && name === 'Location') {
                    return locationSchema;
                }
                return undefined;
            },
            async filter(name): Promise<string[]> {
                return [locationSchema!.getFullName()].filter((n) =>
                    n.toLowerCase().includes(name.toLowerCase()),
                );
            },
        },
        new KIRunSchemaRepository(),
    );

    const obj = { url: 'http://xxxx.com' };

    expect(
        await SchemaValidator.validate(undefined, Schema.ofRef('Test.Location'), repo, obj),
    ).toBe(obj);
});

test('Validate null for ofAny schema', async () => {
    expect(
        await SchemaValidator.validate(
            undefined,
            Schema.ofAny('ofanyundefined'),
            undefined,
            undefined,
        ),
    ).toBe(undefined);
});

test('Schema Validator Test 3', async () => {
    const obj = { url: 'http://xxxx.com' };

    const locationSchema = Schema.from({
        name: 'Location',
        namespace: 'Test',
        type: 'OBJECT',
        properties: {
            url: { name: 'url', type: 'String' },
        },
        required: ['url'],
        defaultValue: obj,
    });

    const repo = new HybridRepository<Schema>(
        {
            async find(namespace, name): Promise<Schema | undefined> {
                if (namespace === 'Test' && name === 'Location') {
                    return locationSchema;
                }
                return undefined;
            },

            async filter(name): Promise<string[]> {
                return [locationSchema!.getFullName()].filter((n) =>
                    n.toLowerCase().includes(name.toLowerCase()),
                );
            },
        },

        new KIRunSchemaRepository(),
    );

    const obj1 = { url: 'http://yyyy.com' };

    expect(
        await SchemaValidator.validate(
            undefined,
            Schema.ofRef('Test.Location').setDefaultValue(obj1),
            repo,
            undefined,
        ),
    ).toMatchObject(obj1);
});

test('Custom message validation - 1', async () => {
    const schema = Schema.from({
        type: 'STRING',
        name: 'test',
        namespace: 'test',
        minLength: 5,
        uiHelper: {
            validationMessages: {
                minLength:
                    'Custom minLength message is not matching {value}, you cannot use this value {value}',
            },
        },
    });

    const obj = '1234';
    expect(SchemaValidator.validate(undefined, schema, repo, obj)).rejects.toThrow(
        'Custom minLength message is not matching 1234, you cannot use this value 1234',
    );
});

test('Custom message validation - 2', async () => {
    const schema = Schema.from({
        type: 'STRING',
        name: 'test',
        namespace: 'test',
        maxLength: 15,
        uiHelper: {
            validationMessages: {
                maxLength:
                    'Custom maxLength message is not matching {value}, you cannot use this value {value}',
            },
        },
    });

    const obj = '1234567890123456';
    expect(SchemaValidator.validate(undefined, schema, repo, obj)).rejects.toThrow(
        'Custom maxLength message is not matching 1234567890123456, you cannot use this value 1234567890123456',
    );
});

test('Custom message validation - 3', async () => {
    const schema = Schema.from({
        type: 'STRING',
        name: 'test',
        namespace: 'test',
        format: 'REGEX',
        pattern: '^[A-Z]+$',
        uiHelper: {
            validationMessages: {
                pattern:
                    'Custom pattern message is not matching {value}, you cannot use this value {value}',
            },
        },
    });

    const obj = 'ABCDe';
    expect(SchemaValidator.validate(undefined, schema, repo, obj)).rejects.toThrow(
        'Custom pattern message is not matching ABCDe, you cannot use this value ABCDe',
    );
});

import {
    AdditionalType,
    ArraySchemaType,
    ArrayValidator,
    KIRunSchemaRepository,
    Schema,
    SchemaValidator,
} from '../../../../../src';

const repo = new KIRunSchemaRepository();

test('schema array validator tuple schema test for additional items with boolean different datatype', async () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: [
            {
                type: 'INTEGER',
            },
            {
                type: 'STRING',
            },
            {
                type: 'BOOLEAN',
            },
            {
                type: 'OBJECT',
            },
        ],
        additionalItems: {
            schemaValue: {
                type: 'OBJECT',
            },
        },
    });

    let obj = [1, 'asd', { val: 'stringtype' }, 'stringOnemore'];

    expect(SchemaValidator.validate([], schema, repo, obj)).rejects.toThrowError();
});

test('schema array validator tuple schema test for additional items with boolean true datatype', async () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: [
            {
                type: 'STRING',
            },
            {
                type: 'BOOLEAN',
            },
        ],
        additionalItems: {
            schemaValue: {
                type: 'OBJECT',
            },
        },
    });

    let obj = ['asd', true, { a: 'b' }];

    expect(await SchemaValidator.validate([], schema, repo, obj)).toStrictEqual(obj);
});

test('schema array validator test for additional items with boolean false different datatype', async () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: { type: 'INTEGER' },
        additionalItems: { booleanValue: false },
    });
    let obj = [1, 2, 3, 4, 'stringtype', true];

    expect(SchemaValidator.validate([], schema, repo, obj)).rejects.toThrowError();
});

test('schema array validator test for additional items with boolean true different datatype', async () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: {
            type: 'INTEGER',
        },
        additionalItems: {
            schemaValue: {
                type: 'STRING',
            },
        },
    });

    let obj = [1, 2, 3, 'stringtype', true];

    expect(SchemaValidator.validate([], schema, repo, obj)).rejects.toThrowError();
});

test('schema array validator tuple schema test for additional items with boolean different datatype', async () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: [
            {
                type: 'INTEGER',
            },
            {
                type: 'STRING',
            },
            {
                type: 'BOOLEAN',
            },
        ],
        additionalItems: {
            schemaValue: {
                type: 'OBJECT',
            },
        },
    });

    let obj = [1, 'asd', { val: 'stringtype' }, 'stringOnemore'];

    expect(SchemaValidator.validate([], schema, repo, obj)).rejects.toThrowError();
});

test('multi level validation inner object pollution test', async () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: {
            type: 'OBJECT',
            properties: {
                x: { type: 'INTEGER' },
            },
        },
        defaultValue: [{ x: 20 }, { x: 30 }],
    });

    let xschema = Schema.from({
        type: 'ARRAY',
        items: {
            type: 'OBJECT',
            properties: {
                x: { type: 'INTEGER' },
                y: { type: 'STRING', defaultValue: 'Kiran' },
            },
            required: ['x'],
        },
    });

    let value = await SchemaValidator.validate(
        undefined,
        xschema,
        repo,
        await SchemaValidator.validate(undefined, schema, repo, undefined),
    );

    expect(schema?.getDefaultValue()[0].y).toBeUndefined();
    expect(value[0].y).toBe('Kiran');
});

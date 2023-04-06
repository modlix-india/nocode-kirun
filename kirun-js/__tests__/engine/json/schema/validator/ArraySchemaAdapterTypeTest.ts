import { KIRunSchemaRepository, SchemaValidator } from '../../../../../src';
import { Schema } from '../../../../../src/engine/json/schema/Schema';

const repo = new KIRunSchemaRepository();
test('schemaArray With Single Test', () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: {
            singleSchema: {
                type: 'OBJECT',
                properties: { name: { type: 'STRING' }, age: { type: 'INTEGER' } },
            },
        },
        additionalItems: false,
    });

    let obj = [
        {
            name: 'amigo1',
        },
        {
            age: 24,
        },
        false,
        'exampleString',
    ];

    expect(() => SchemaValidator.validate([], schema, repo, obj)).toThrow();
});

test('schemaArray With out Single fail Test', () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: {
            type: 'OBJECT',
            properties: { name: { type: 'STRING' }, age: { type: 'INTEGER' } },
        },

        additionalItems: false,
    });

    let obj = [
        {
            name: 'amigo1',
        },
        {
            age: 24,
        },
        false,
        'exampleString',
    ];

    expect(() => SchemaValidator.validate([], schema, repo, obj)).toThrow();
});

test('schemaArrayWithSingle pass Test', () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: {
            singleSchema: {
                type: 'OBJECT',
                properties: { name: { type: 'STRING' }, age: { type: 'INTEGER' } },
            },
        },

        additionalItems: false,
    });

    let obj = [
        {
            name: 'amigo1',
        },
        {
            age: 24,
        },
    ];

    expect(SchemaValidator.validate([], schema, repo, obj)).toBe(obj);
});

test('schemaArray With Tuple Test ', () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: {
            tupleSchema: [
                {
                    type: 'OBJECT',
                    properties: { name: { type: 'STRING' }, age: { type: 'INTEGER' } },
                    required: ['age'],
                },
                { type: 'STRING', minLength: 2 },
                { type: 'INTEGER', minimum: 10 },
            ],
        },
        additionalItems: true,
    });

    let obj = [
        {
            name: 'amigo1',
            age: 24,
        },
        'string type',

        11,
        false,
        12.34,
        'mla',
    ];
    console.log(JSON.stringify(schema, undefined, 2));

    expect(SchemaValidator.validate([], schema, repo, obj)).toBe(obj);
});

test('schemaArray With out Tuple Test ', () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: [
            {
                type: 'OBJECT',
                properties: { name: { type: 'STRING' }, age: { type: 'INTEGER' } },
                required: ['age'],
            },
            { type: 'STRING', minLength: 2 },
            { type: 'ARRAY', items: { type: 'INTEGER' }, additionalItems: false },
        ],
        additionalItems: true,
    });

    let obj = [
        {
            name: 'amigo1',
            age: 21,
        },
        'second string',
        [1, 2, 31231],
        'additional items was added here with true and false',
        true,
        false,
    ];
    expect(SchemaValidator.validate([], schema, repo, obj)).toBe(obj);
});

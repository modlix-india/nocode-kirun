import {
    AdditionalType,
    ArraySchemaType,
    ArrayValidator,
    KIRunSchemaRepository,
    Schema,
    SchemaValidator,
} from '../../../../../src';

const repo = new KIRunSchemaRepository();

test('schema array validator test for additional items with boolean false different datatype', () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: { singleSchema: { type: 'INTEGER' } },
        additionalItems: false,
    });
    let obj = [1, 2, 3, 4, 'stringtype', true];

    expect(() => SchemaValidator.validate([], schema, repo, obj)).toThrow();
});

test('schema array validator test for additional items with boolean true different datatype', () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: {
            singleSchema: {
                type: 'INTEGER',
            },
        },
        additionalItems: {
            schemaValue: {
                type: 'STRING',
            },
        },
    });

    let obj = [1, 2, 3, 'stringtype', true];

    expect(() => SchemaValidator.validate([], schema, repo, obj)).toThrowError();
});

test('schema array validator tuple schema test for additional items with boolean true different datatype', () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: {
            tupleSchema: [
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
        },
        additionalItems: {
            schemaValue: {
                type: 'OBJECT',
            },
        },
    });

    let obj = [1, 'asd', { val: 'stringtype' }, 'stringOnemore'];

    expect(() => SchemaValidator.validate([], schema, repo, obj)).toThrowError();
});

test('schema array validator tuple schema test for additional items with boolean true datatype', () => {
    let schema = Schema.from({
        type: 'ARRAY',
        items: {
            tupleSchema: [
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
        },
        additionalItems: {
            schemaValue: {
                type: 'STRING',
            },
        },
    });

    let obj = [1, 'asd', { val: 'stringtype' }, 'stringOnemore'];

    console.log(schema);

    expect(SchemaValidator.validate([], schema, repo, obj)).toStrictEqual(obj);
});

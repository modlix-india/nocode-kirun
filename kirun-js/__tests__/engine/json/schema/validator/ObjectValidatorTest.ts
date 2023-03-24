import {
    KIRunSchemaRepository,
    ObjectValidator,
    Schema,
    SchemaType,
    SchemaValidator,
} from '../../../../../src';

const repo = new KIRunSchemaRepository();

test('schema Object validator test boolean value', () => {
    let schema = Schema.from({
        type: 'OBJECT',
        properties: { name: { type: 'STRING' } },
        additionalProperties: false,
    });
    expect(schema?.getType()?.contains(SchemaType.OBJECT)).toBe(true);

    expect(SchemaValidator.validate([], schema!, repo, { name: 'Kiran' })).toStrictEqual({
        name: 'Kiran',
    });

    expect(() =>
        SchemaValidator.validate([], schema!, repo, { name: 'Kiran', lastName: 'Grandhi' }),
    ).toThrowError();
});

test('schema Object validator test schema based', () => {
    let schema = Schema.from({
        type: 'OBJECT',
        properties: { name: { type: 'STRING' } },
        additionalProperties: { type: 'INTEGER' },
    });
    expect(schema?.getType()?.contains(SchemaType.OBJECT)).toBe(true);

    expect(SchemaValidator.validate([], schema!, repo, { name: 'Kiran', num: 23 })).toStrictEqual({
        name: 'Kiran',
        num: 23,
    });

    expect(() =>
        SchemaValidator.validate([], schema!, repo, {
            name: 'Kiran',
            num: 23,
            lastName: 'grandhi',
        }),
    ).toThrowError();
});

test('schema Object validator test boolean value old style', () => {
    let schema = Schema.from({
        type: 'OBJECT',
        properties: { name: { type: 'STRING' } },
        additionalProperties: { booleanValue: false },
    });

    console.log(schema?.getAdditionalProperties());

    expect(SchemaValidator.validate([], schema!, repo, { name: 'Kiran' })).toStrictEqual({
        name: 'Kiran',
    });

    expect(() =>
        SchemaValidator.validate([], schema!, repo, { name: 'Kiran', lastName: 'Grandhi' }),
    ).toThrowError();
});

test('schema Object validator test schema based old style', () => {
    let schema = Schema.from({
        type: 'OBJECT',
        properties: { name: { type: 'STRING' } },
        additionalProperties: { schemaValue: { type: 'INTEGER' } },
    });
    expect(schema?.getType()?.contains(SchemaType.OBJECT)).toBe(true);

    expect(SchemaValidator.validate([], schema!, repo, { name: 'Kiran', num: 23 })).toStrictEqual({
        name: 'Kiran',
        num: 23,
    });

    expect(() =>
        SchemaValidator.validate([], schema!, repo, {
            name: 'Kiran',
            num: 23,
            lastName: 'grandhi',
        }),
    ).toThrowError();
});

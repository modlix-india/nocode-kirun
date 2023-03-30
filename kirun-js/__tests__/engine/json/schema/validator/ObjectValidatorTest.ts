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

test('schema Object validator test schema based old ARRAY style', () => {
    let schema = Schema.from({
        type: 'OBJECT',
        properties: { name: { type: 'STRING' } },
        additionalProperties: { schemaValue: { type: 'ARRAY' } },
    });
    expect(schema?.getType()?.contains(SchemaType.OBJECT)).toBe(true);

    var obj = { name: 'Kiran', num: [1, 2, 3] };
    expect(SchemaValidator.validate([], schema!, repo, obj)).toStrictEqual(obj);

    expect(() =>
        SchemaValidator.validate([], schema!, repo, {
            name: 'Kiran',
            num: 23,
            lastName: 'grandhi',
        }),
    ).toThrowError();
});

test('schema Object validator test schema based old Object style', () => {
    let schema = Schema.from({
        type: 'OBJECT',
        properties: {
            name: { type: 'STRING' },
            lastname: { type: 'STRING' },
            age: { type: 'INTEGER' },
        },
        additionalProperties: { schemaValue: { type: 'OBJECT' } },
    });

    var obj = { name: 'Kiran', age: 23 };
    expect(SchemaValidator.validate([], schema!, repo, obj)).toStrictEqual(obj);

    var objwithAdditional = {
        name: 'Kiran',
        lastname: 'grandhi',
        addresses: {
            area: 'j.p.nagar',
            city: 'banga',
        },
    };

    expect(SchemaValidator.validate([], schema!, repo, objwithAdditional)).toStrictEqual(
        objwithAdditional,
    );

    var objwithMoreAdditional = {
        name: 'Kiran',
        lastname: 'grandhi',
        addresses: {
            area: 'j.p.nagar',
            city: 'banga',
        },
        city: 'kakinada',
    };

    expect(() => SchemaValidator.validate([], schema!, repo, objwithMoreAdditional)).toThrow();
});

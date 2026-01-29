import {
    HybridRepository,
    KIRunSchemaRepository,
    ObjectValidator,
    Repository,
    Schema,
    SchemaType,
    SchemaValidator,
} from '../../../../../src';

const repo = new KIRunSchemaRepository();

test('schema Object validator test boolean value', async () => {
    let schema = Schema.from({
        type: 'OBJECT',
        properties: { name: { type: 'STRING' } },
        additionalProperties: false,
    });
    expect(schema?.getType()?.contains(SchemaType.OBJECT)).toBe(true);

    expect(await SchemaValidator.validate([], schema!, repo, { name: 'Kiran' })).toStrictEqual({
        name: 'Kiran',
    });

    expect(
        SchemaValidator.validate([], schema!, repo, { name: 'Kiran', lastName: 'Grandhi' }),
    ).rejects.toThrow();
});

test('schema Object validator test schema based', async () => {
    let schema = Schema.from({
        type: 'OBJECT',
        properties: { name: { type: 'STRING' } },
        additionalProperties: { type: 'INTEGER' },
    });
    expect(schema?.getType()?.contains(SchemaType.OBJECT)).toBe(true);

    expect(
        await SchemaValidator.validate([], schema!, repo, { name: 'Kiran', num: 23 }),
    ).toStrictEqual({
        name: 'Kiran',
        num: 23,
    });

    expect(
        SchemaValidator.validate([], schema!, repo, {
            name: 'Kiran',
            num: 23,
            lastName: 'grandhi',
        }),
    ).rejects.toThrow();
});

test('schema Object validator test boolean value old style', async () => {
    let schema = Schema.from({
        type: 'OBJECT',
        properties: { name: { type: 'STRING' } },
        additionalProperties: { booleanValue: false },
    });

    expect(await SchemaValidator.validate([], schema!, repo, { name: 'Kiran' })).toStrictEqual({
        name: 'Kiran',
    });

    expect(
        SchemaValidator.validate([], schema!, repo, { name: 'Kiran', lastName: 'Grandhi' }),
    ).rejects.toThrow();
});

test('schema Object validator test schema based old style', async () => {
    let schema = Schema.from({
        type: 'OBJECT',
        properties: { name: { type: 'STRING' } },
        additionalProperties: { schemaValue: { type: 'INTEGER' } },
    });
    expect(schema?.getType()?.contains(SchemaType.OBJECT)).toBe(true);

    expect(
        await SchemaValidator.validate([], schema!, repo, { name: 'Kiran', num: 23 }),
    ).toStrictEqual({
        name: 'Kiran',
        num: 23,
    });

    expect(
        SchemaValidator.validate([], schema!, repo, {
            name: 'Kiran',
            num: 23,
            lastName: 'grandhi',
        }),
    ).rejects.toThrow();
});

test('schema Object validator test schema based old ARRAY style', async () => {
    let schema = Schema.from({
        type: 'OBJECT',
        properties: { name: { type: 'STRING' } },
        additionalProperties: { schemaValue: { type: 'ARRAY' } },
    });
    expect(schema?.getType()?.contains(SchemaType.OBJECT)).toBe(true);

    var obj = { name: 'Kiran', num: [1, 2, 3] };
    expect(await SchemaValidator.validate([], schema!, repo, obj)).toStrictEqual(obj);

    expect(
        SchemaValidator.validate([], schema!, repo, {
            name: 'Kiran',
            num: 23,
            lastName: 'grandhi',
        }),
    ).rejects.toThrow();
});

test('schema Object validator test schema based old Object style', async () => {
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
    expect(await SchemaValidator.validate([], schema!, repo, obj)).toStrictEqual(obj);

    var objwithAdditional = {
        name: 'Kiran',
        lastname: 'grandhi',
        addresses: {
            area: 'j.p.nagar',
            city: 'banga',
        },
    };

    expect(await SchemaValidator.validate([], schema!, repo, objwithAdditional)).toStrictEqual(
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

    expect(SchemaValidator.validate([], schema!, repo, objwithMoreAdditional)).rejects.toThrow();
});

test('Schema test with object value as null for any', async () => {
    let filterOperator = Schema.from({
        namespace: 'test',
        name: 'filterOperator',
        version: 1,
        type: 'STRING',
        defaultValue: 'EQUALS',
        enums: ['EQUALS', 'LESS_THAN', 'GREATER_THAN', 'LESS_THAN_EQUAL', 'BETWEEN', 'IN'],
    });

    let filterCondition = Schema.from({
        namespace: 'test',
        name: 'FilterCondition',
        version: 1,
        type: 'OBJECT',
        properties: {
            field: {
                namespace: '_',
                name: 'field',
                version: 1,
                type: 'STRING',
            },
            multiValue: {
                namespace: '_',
                name: 'multiValue',
                version: 1,
                type: 'ARRAY',
                items: {
                    namespace: '_',
                    name: 'singleType',
                    version: 1,
                    type: [
                        'FLOAT',
                        'BOOLEAN',
                        'STRING',
                        'DOUBLE',
                        'INTEGER',
                        'LONG',
                        'NULL',
                        'ARRAY',
                        'OBJECT',
                    ],
                },
            },
            isValue: {
                namespace: '_',
                name: 'isValue',
                version: 1,
                type: 'BOOLEAN',
                defaultValue: false,
            },
            toValue: {
                namespace: '_',
                name: 'toValue',
                version: 1,
                type: [
                    'FLOAT',
                    'BOOLEAN',
                    'STRING',
                    'DOUBLE',
                    'INTEGER',
                    'LONG',
                    'NULL',
                    'ARRAY',
                    'OBJECT',
                ],
            },
            operator: {
                namespace: '_',
                version: 1,
                ref: 'test.filterOperator',
            },
            negate: {
                namespace: '_',
                name: 'negate',
                version: 1,
                type: 'BOOLEAN',
                defaultValue: false,
            },
            value: {
                namespace: '_',
                name: 'value',
                version: 1,
                type: [
                    'FLOAT',
                    'BOOLEAN',
                    'STRING',
                    'DOUBLE',
                    'INTEGER',
                    'LONG',
                    'NULL',
                    'ARRAY',
                    'OBJECT',
                ],
            },
            isToValue: {
                namespace: '_',
                name: 'isToValue',
                version: 1,
                type: 'BOOLEAN',
                defaultValue: false,
            },
        },
        additionalProperties: false,
        required: ['operator', 'field'],
    });

    var schemaMap = new Map<string, Schema>();

    schemaMap.set('filterOperator', filterOperator!);
    schemaMap.set('FilterCondition', filterCondition!);

    class TestRepository implements Repository<Schema> {
        public async find(namespace: string, name: string): Promise<Schema | undefined> {
            if (!namespace) {
                return undefined;
            }
            return schemaMap.get(name);
        }

        public async filter(name: string): Promise<string[]> {
            return [];
        }
    }
    var repo = new HybridRepository(new TestRepository(), new KIRunSchemaRepository());

    var tempOb2 = { field: 'nullcheck', operator: 'LESS_THAN', value: null, isValue: true };

    var res3 = await SchemaValidator.validate(undefined, filterCondition, repo, tempOb2);
    expect(res3).toStrictEqual(tempOb2);
});

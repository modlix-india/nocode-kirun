import { KIRunSchemaRepository, Schema, SchemaType, SchemaValidator } from '../../../../../src';

const repo = new KIRunSchemaRepository();

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

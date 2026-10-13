import { Schema } from '../../../../../src/engine/json/schema/Schema';
import { SchemaValidator } from '../../../../../src/engine/json/schema/validator/SchemaValidator';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';

const repo = new KIRunSchemaRepository();

// TypeUtil.from reads a type in any case (and "number" as DOUBLE), so the Schema meta-schema
// must accept the same spellings now that anyOf is enforced.
test.each(['OBJECT', 'Object', 'object', 'Number', 'number', 'NUMBER'])(
    'Schema accepts type %s',
    async (type) => {
        const value = { type, properties: { a: { type: 'String' } } };
        expect(await SchemaValidator.validate([], Schema.SCHEMA, repo, value)).toBeTruthy();
    },
);

test('Schema accepts a type array in mixed spellings', async () => {
    const value = { type: ['Object', 'NULL', 'string'] };
    expect(await SchemaValidator.validate([], Schema.SCHEMA, repo, value)).toBeTruthy();
});

test('Schema still rejects an unknown type', async () => {
    await expect(
        SchemaValidator.validate([], Schema.SCHEMA, repo, { type: 'Thing' }),
    ).rejects.toThrow();
});

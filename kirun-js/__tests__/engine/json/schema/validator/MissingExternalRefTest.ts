import { Schema } from '../../../../../src/engine/json/schema/Schema';
import { SchemaValidator } from '../../../../../src/engine/json/schema/validator/SchemaValidator';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';

// A ref to a schema the repository does not have used to resolve to the referring schema
// itself, so validation followed the same ref forever.

test('a ref to a missing external schema fails instead of recursing', async () => {
    const schema = Schema.ofRef('MyApp.Missing');
    await expect(
        SchemaValidator.validate(undefined, schema, new KIRunSchemaRepository(), 5),
    ).rejects.toThrow('No schema found to validate');
}, 2000);

test('a ref with no repository fails instead of recursing', async () => {
    await expect(
        SchemaValidator.validate(undefined, Schema.ofRef('MyApp.Missing'), undefined, 5),
    ).rejects.toThrow('No schema found to validate');
}, 2000);

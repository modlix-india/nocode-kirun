import { Schema } from '../../../../../src/engine/json/schema/Schema';
import { SchemaValidator } from '../../../../../src/engine/json/schema/validator/SchemaValidator';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';

// anyOf / allOf / oneOf used to be skipped entirely (guarded by `x && !x`), and each candidate
// was checked only against its own anyOf list. These pin the checks kirun-java performs.

const repo = new KIRunSchemaRepository();

const anyOf = new Schema().setAnyOf([Schema.ofInteger('i'), Schema.ofString('s')]);
const allOf = new Schema().setAllOf([
    Schema.ofNumber('min').setMinimum(5),
    Schema.ofNumber('max').setMaximum(10),
]);
const oneOfOverlapping = new Schema().setOneOf([Schema.ofInteger('i'), Schema.ofNumber('n')]);
const oneOfDistinct = new Schema().setOneOf([Schema.ofString('s'), Schema.ofBoolean('b')]);

test('anyOf passes when one schema matches', async () => {
    expect(await SchemaValidator.validate(undefined, anyOf, repo, 10)).toBe(10);
    expect(await SchemaValidator.validate(undefined, anyOf, repo, 'x')).toBe('x');
});

test('anyOf rejects a value no schema matches', async () => {
    await expect(SchemaValidator.validate(undefined, anyOf, repo, true)).rejects.toThrow(
        "The value don't satisfy any of the schemas.",
    );
});

test('anyOf returns the value as validated by the matching schema', async () => {
    const withDefault = new Schema().setAnyOf([Schema.ofString('s').setDefaultValue('d')]);
    expect(await SchemaValidator.validate(undefined, withDefault, repo, undefined)).toBe('d');
});

test('allOf needs every schema to match', async () => {
    expect(await SchemaValidator.validate(undefined, allOf, repo, 7)).toBe(7);
    await expect(SchemaValidator.validate(undefined, allOf, repo, 12)).rejects.toThrow(
        "The value doesn't satisfy some of the schemas.",
    );
});

test('oneOf needs exactly one schema to match', async () => {
    expect(await SchemaValidator.validate(undefined, oneOfDistinct, repo, 'x')).toBe('x');
    await expect(SchemaValidator.validate(undefined, oneOfDistinct, repo, 7)).rejects.toThrow(
        'The value does not satisfy any schema',
    );
    await expect(SchemaValidator.validate(undefined, oneOfOverlapping, repo, 7)).rejects.toThrow(
        'The value satisfy more than one schema',
    );
});

import { Schema } from '../../../../../src/engine/json/schema/Schema';
import { SchemaType } from '../../../../../src/engine/json/schema/type/SchemaType';
import { TypeUtil } from '../../../../../src/engine/json/schema/type/TypeUtil';
import { AnyOfAllOfOneOfValidator } from '../../../../../src/engine/json/schema/validator/AnyOfAllOfOneOfValidator';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';

const repo = new KIRunSchemaRepository();

test('Any Of All Of One Validator Test 1', async () => {
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.INTEGER));

    expect(AnyOfAllOfOneOfValidator.validate([], schema, repo, 10)).toBe(10);
});

test('Any Of All Of One Validator Test 2', async () => {
    let arraySchema: Schema = new Schema().setType(TypeUtil.of(SchemaType.ARRAY));

    expect(AnyOfAllOfOneOfValidator.validate([], arraySchema, repo, [1, 2, 3])).toStrictEqual([
        1, 2, 3,
    ]);
});

test('Any Of All Of One Validator Test 3', async () => {
    let objSchema: Schema = Schema.ofObject('testObj').setProperties(
        new Map<string, Schema>([['key', Schema.ofString('key')]]),
    );

    expect(AnyOfAllOfOneOfValidator.validate([], objSchema, repo, { key: 'value' })).toStrictEqual({
        key: 'value',
    });
});

test('Any Of All Of One Validator Test 3', async () => {
    let nullSchema: Schema = new Schema().setType(TypeUtil.of(SchemaType.NULL));

    expect(AnyOfAllOfOneOfValidator.validate([], nullSchema, repo, null)).toBe(null);
});

test('Any Of All Of One Validator Test 3', async () => {
    let nullSchema: Schema = new Schema().setType(TypeUtil.of(SchemaType.BOOLEAN));

    expect(AnyOfAllOfOneOfValidator.validate([], nullSchema, repo, null)).toBe(null);
});

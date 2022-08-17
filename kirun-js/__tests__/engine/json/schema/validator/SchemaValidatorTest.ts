import { Schema } from '../../../../../src/engine/json/schema/Schema';
import { SchemaType } from '../../../../../src/engine/json/schema/type/SchemaType';
import { TypeUtil } from '../../../../../src/engine/json/schema/type/TypeUtil';
import { SchemaValidationException } from '../../../../../src/engine/json/schema/validator/exception/SchemaValidationException';
import { SchemaValidator } from '../../../../../src/engine/json/schema/validator/SchemaValidator';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';

const repo = new KIRunSchemaRepository();

test('Schema Validator Test 1', () => {
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.INTEGER));

    expect(SchemaValidator.validate([], schema, repo, 2)).toBe(2);

    let obj = { name: 'shagil' };
    let objSchema: Schema = Schema.ofObject('testObj')
        .setProperties(new Map<string, Schema>([['name', Schema.ofString('name')]]))
        .setRequired(['name']);
    expect(SchemaValidator.validate([], objSchema, repo, obj)).toBe(obj);

    expect(() => SchemaValidator.validate([], objSchema, repo, { name: 123 })).toThrow(
        'Value {"name":123} is not of valid type(s)\nValue 123 is not of valid type(s)\n123 is not String',
    );

    // expect(SchemaValidator.validate([], schema, repo, 2.5)).toThrowError(new SchemaValidationException('', '2.5 is not a number of type Integer'));
});

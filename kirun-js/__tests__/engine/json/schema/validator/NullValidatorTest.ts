import { Schema, SchemaValidator } from '../../../../../src';

test('Check for valid Null value', () => {
    let schema = Schema.from({
        type: 'NULL',
    });

    expect(() => SchemaValidator.validate([], schema!, undefined, 23)).toThrowError();
    expect(() => SchemaValidator.validate([], schema!, undefined, 0)).toThrowError();
    expect(() => SchemaValidator.validate([], schema!, undefined, '')).toThrowError();
    expect(SchemaValidator.validate([], schema!, undefined, null)).toBeNull();
    expect(SchemaValidator.validate([], schema!, undefined, undefined)).toBeUndefined();
});

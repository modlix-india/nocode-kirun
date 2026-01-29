import { Schema, SchemaValidator } from '../../../../../src';

test('Check for valid Null value', async () => {
    let schema = Schema.from({
        type: 'NULL',
    });

    expect(async () => SchemaValidator.validate([], schema!, undefined, 23)).rejects.toThrow();
    expect(async () => SchemaValidator.validate([], schema!, undefined, 0)).rejects.toThrow();
    expect(async () => SchemaValidator.validate([], schema!, undefined, '')).rejects.toThrow();
    expect(await SchemaValidator.validate([], schema!, undefined, null)).toBeNull();
    expect(await SchemaValidator.validate([], schema!, undefined, undefined)).toBeUndefined();
});

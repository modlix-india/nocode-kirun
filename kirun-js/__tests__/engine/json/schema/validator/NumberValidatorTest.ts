import { Schema, SchemaValidator } from '../../../../../src';

test('Check for valid Custom Messages in Numerical values', async () => {
    let schema = Schema.from({
        type: 'INTEGER',
        minimum: 10,
        details: {
            validationMessages: {
                "minimum": "Minimum value is 10",
            }
        }
    });

    expect(async () => SchemaValidator.validate([], schema!, undefined, -23))
        .rejects.toThrowError("Minimum value is 10");
});

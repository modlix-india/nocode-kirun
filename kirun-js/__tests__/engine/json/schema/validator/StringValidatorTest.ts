import { Schema, SchemaType, SchemaValidator, StringFormat, TypeUtil } from '../../../../../src';
import { StringValidator } from '../../../../../src';

test('String valid case', async () => {
    let value: String = 'surendhar';
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.STRING));

    expect(StringValidator.validate([], schema, value)).toBe(value);
});

test('String invalid case', async () => {
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.STRING));

    expect(() => StringValidator.validate([], schema, 123).toThrow(123 + ' is not String'));
});

test('String min length invalid', async () => {
    let value: String = 'abcd';
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.STRING)).setMinLength(5);

    expect(() =>
        StringValidator.validate([], schema, value).toThrow(
            'Expected a minimum of ' + value.length + ' characters',
        ),
    );
});

test('String max length invalid', async () => {
    let value: String = 'surendhar';
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.STRING)).setMaxLength(8);

    expect(() =>
        StringValidator.validate([], schema, value).toThrow(
            'Expected a maximum of ' + value.length + ' characters',
        ),
    );
});

test('String min length', async () => {
    let value: String = 'abcdefg';
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.STRING)).setMinLength(5);

    expect(StringValidator.validate([], schema, value)).toBe(value);
});

test('String max length', async () => {
    let value: String = 'surendhar';
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.STRING)).setMaxLength(12323);

    expect(StringValidator.validate([], schema, value)).toBe(value);
});

test('String date invalid case', async () => {
    let value: String = '1234-12-1245';

    let schema: Schema = new Schema().setFormat(StringFormat.DATE);

    expect(() =>
        StringValidator.validate([], schema, value).toThrow(
            value + ' is not matched with the ' + 'date pattern',
        ),
    );
});

test('String date valid case', async () => {
    let value: String = '2023-01-26';

    let schema: Schema = new Schema().setFormat(StringFormat.DATE);

    expect(StringValidator.validate([], schema, value)).toBe(value);
});

test('String time invalid case', async () => {
    let value: String = '231:45:56';

    let schema: Schema = new Schema().setFormat(StringFormat.TIME);

    expect(() => StringValidator.validate([], schema, value)).toThrow(
        value + ' is not matched with the ' + 'time pattern',
    );
});

test('String time valid case', async () => {
    let value: String = '22:32:45';

    let schema: Schema = new Schema().setFormat(StringFormat.TIME);

    expect(StringValidator.validate([], schema, value)).toBe(value);
});

test('String date time invalid case', async () => {
    let value: String = '26-jan-2023 231:45:56';

    let schema: Schema = new Schema().setFormat(StringFormat.DATETIME);

    expect(() => StringValidator.validate([], schema, value)).toThrow(
        value + ' is not matched with the ' + 'date time pattern',
    );
});

test('String date time valid case', async () => {
    let value: String = '2032-02-12T02:54:23';

    let schema: Schema = new Schema().setFormat(StringFormat.DATETIME);

    expect(StringValidator.validate([], schema, value)).toBe(value);
});

test('String date valid on the 10th and 20th', async () => {
    let schema: Schema = new Schema().setFormat(StringFormat.DATE);

    for (const value of ['2026-01-10', '2026-01-20', '2026-01-19', '2026-01-29']) {
        expect(StringValidator.validate([], schema, value)).toBe(value);
    }
});

test('String date time valid on the 10th and 20th', async () => {
    let schema: Schema = new Schema().setFormat(StringFormat.DATETIME);

    for (const value of ['2026-01-10T10:00:00', '2026-01-20T10:00:00', '2026-01-20T10:00:00+05:30']) {
        expect(StringValidator.validate([], schema, value)).toBe(value);
    }
});

function expectValid(format: StringFormat, ...values: string[]) {
    let schema: Schema = new Schema().setFormat(format);

    for (const value of values) {
        expect(StringValidator.validate([], schema, value)).toBe(value);
    }
}

function expectInvalid(format: StringFormat, patternName: string, ...values: string[]) {
    let schema: Schema = new Schema().setFormat(format);

    for (const value of values) {
        expect(() => StringValidator.validate([], schema, value)).toThrow(
            value + ' is not matched with the ' + patternName,
        );
    }
}

test('String date time and time with fractional seconds and Z', async () => {
    expectValid(
        StringFormat.DATETIME,
        '2026-01-15T10:00:00Z',
        '2026-01-15T10:00:00.123Z',
        '2026-01-15T10:00:00.123456789+05:30',
    );
    expectInvalid(
        StringFormat.DATETIME,
        'date time pattern',
        '2026-01-15T10:00.123',
        '2026-01-15T10:00:00.1234567890Z',
        '2026-01-15T10:00:00.Z',
    );

    expectValid(StringFormat.TIME, '10:00:00Z', '10:00:00.123', '10:00:00.123+05:30');
    expectInvalid(StringFormat.TIME, 'time pattern', '10:00.5', '10:00:00.');
});

test('String decimal', async () => {
    expectValid(StringFormat.DECIMAL, '0', '100', '-12.50', '+3.14159');
    expectInvalid(StringFormat.DECIMAL, 'decimal pattern', '1e5', '12.', '.5', '1,000', 'abc', '');
});

test('String id', async () => {
    expectValid(
        StringFormat.ID,
        '65f1c2a9e4b0a1b2c3d4e5f6',
        '01J9ZQ3V8K2M4N6P8R0S2T4V6W',
        '01j9zq3v8k2m4n6p8r0s2t4v6w',
    );
    expectInvalid(
        StringFormat.ID,
        'id pattern',
        '65f1c2a9e4b0a1b2c3d4e5fg',
        '01J9ZQ3V8K2M4N6P8R0S2T4V6U',
        '01J9ZQ3V8K2M4N6P8R0S2T4V6',
        'abc',
    );
});

test('String email invalid case', async () => {
    let value: String = 'testemail fai%6&8ls@gmail.com';

    let schema: Schema = new Schema().setFormat(StringFormat.EMAIL);

    expect(() => StringValidator.validate([], schema, value)).toThrow(
        value + ' is not matched with the ' + 'email pattern',
    );
});

test('String email valid case', async () => {
    let value: String = 'testemaifai%6&8lworkings@magil.com';

    let schema: Schema = new Schema().setFormat(StringFormat.EMAIL);

    expect(StringValidator.validate([], schema, value)).toBe(value);
});


test('String custom message', async () => {
    const schema = Schema.from({
        type: "STRING",
        minLength: 10,
        details: {
            validationMessages: {
                minLength: "You must enter something with minimum of ten characters"
            }
        }
    })

    expect(async () => SchemaValidator.validate([], schema!, undefined, "asdf"))
        .rejects.toThrow("You must enter something with minimum of ten characters");
});

test('String date rejects month zero', async () => {
    // The month alternation was ([0][0-9]|[1][0-2]), so 00 matched the first
    // branch. 2026-00-15 validated on all three runtimes and then failed wherever
    // something tried to make a real date out of it.
    const date: Schema = new Schema().setFormat(StringFormat.DATE);

    expect(() => StringValidator.validate([], date, '2026-00-15')).toThrow();
    expect(() => StringValidator.validate([], date, '2026-00-01')).toThrow();

    for (const valid of ['2026-01-15', '2026-09-30', '2026-10-01', '2026-12-31'])
        expect(StringValidator.validate([], date, valid)).toBe(valid);

    const dateTime: Schema = new Schema().setFormat(StringFormat.DATETIME);
    expect(() => StringValidator.validate([], dateTime, '2026-00-15T10:00:00Z')).toThrow();
});

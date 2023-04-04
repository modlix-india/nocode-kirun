import {
    KIRunSchemaRepository,
    Schema,
    SchemaType,
    StringFormat,
    TypeUtil,
} from '../../../../../src';
import { StringValidator } from '../../../../../src';

const repo = new KIRunSchemaRepository();

test('String valid case', () => {
    let value: String = 'surendhar';
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.STRING));

    expect(StringValidator.validate([], schema, value)).toBe(value);
});

test('String invalid case', () => {
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.STRING));

    expect(() => StringValidator.validate([], schema, 123).toThrow(123 + ' is not String'));
});

test('String min length invalid', () => {
    let value: String = 'abcd';
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.STRING)).setMinLength(5);

    expect(() =>
        StringValidator.validate([], schema, value).toThrow(
            'Expected a minimum of ' + value.length + ' characters',
        ),
    );
});

test('String max length invalid', () => {
    let value: String = 'surendhar';
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.STRING)).setMaxLength(8);

    expect(() =>
        StringValidator.validate([], schema, value).toThrow(
            'Expected a maximum of ' + value.length + ' characters',
        ),
    );
});

test('String min length', () => {
    let value: String = 'abcdefg';
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.STRING)).setMinLength(5);

    expect(StringValidator.validate([], schema, value)).toBe(value);
});

test('String max length', () => {
    let value: String = 'surendhar';
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.STRING)).setMaxLength(12323);

    expect(StringValidator.validate([], schema, value)).toBe(value);
});

test('String date invalid case', () => {
    let value: String = '1234-12-1245';

    let schema: Schema = new Schema().setFormat(StringFormat.DATE);

    expect(() =>
        StringValidator.validate([], schema, value).toThrow(
            value + ' is not matched with the ' + 'date pattern',
        ),
    );
});

test('String date valid case', () => {
    let value: String = '2023-01-26';

    let schema: Schema = new Schema().setFormat(StringFormat.DATE);

    expect(StringValidator.validate([], schema, value)).toBe(value);
});

test('String time invalid case', () => {
    let value: String = '231:45:56';

    let schema: Schema = new Schema().setFormat(StringFormat.TIME);

    expect(() => StringValidator.validate([], schema, value)).toThrow(
        value + ' is not matched with the ' + 'time pattern',
    );
});

test('String time valid case', () => {
    let value: String = '22:32:45';

    let schema: Schema = new Schema().setFormat(StringFormat.TIME);

    expect(StringValidator.validate([], schema, value)).toBe(value);
});

test('String date time invalid case', () => {
    let value: String = '26-jan-2023 231:45:56';

    let schema: Schema = new Schema().setFormat(StringFormat.DATETIME);

    expect(() => StringValidator.validate([], schema, value)).toThrow(
        value + ' is not matched with the ' + 'date time pattern',
    );
});

test('String date time valid case', () => {
    let value: String = '2032-02-12T02:54:23';

    let schema: Schema = new Schema().setFormat(StringFormat.DATETIME);

    expect(StringValidator.validate([], schema, value)).toBe(value);
});

test('String email invalid case', () => {
    let value: String = 'testemail fai%6&8ls@gmail.com';

    let schema: Schema = new Schema().setFormat(StringFormat.EMAIL);

    expect(() => StringValidator.validate([], schema, value)).toThrow(
        value + ' is not matched with the ' + 'email pattern',
    );
});

test('String email valid case', () => {
    let value: String = 'testemaifai%6&8lworkings@magil.com';

    let schema: Schema = new Schema().setFormat(StringFormat.EMAIL);

    expect(StringValidator.validate([], schema, value)).toBe(value);
});

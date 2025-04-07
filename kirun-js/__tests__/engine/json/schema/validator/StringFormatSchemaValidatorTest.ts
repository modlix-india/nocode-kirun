import {
    KIRunSchemaRepository,
    Schema,
    SchemaType,
    SchemaValidator,
    StringFormat,
    Type,
    TypeUtil,
} from '../../../../../src';
import { Minimum } from '../../../../../src/engine/function/system/math/Minimum';
import { UiHelper } from '../../../../../src/engine/json/schema/uiHelper/UiHelper';

const repo = new KIRunSchemaRepository();

test('Schema Validator fail with date test', async () => {
    let schema: Schema = new Schema().setType(TypeUtil.of(SchemaType.INTEGER));

    expect(await SchemaValidator.validate([], schema, repo, 2)).toBe(2);

    let obj = { name: 'shagil' };
    let objSchema: Schema = Schema.ofObject('testObj')
        .setProperties(
            new Map<string, Schema>([
                ['name', Schema.ofString('name')],
                ['date', new Schema().setFormat(StringFormat.DATE)],
            ]),
        )
        .setRequired(['name', 'date']);

    expect(SchemaValidator.validate([], objSchema, repo, obj)).rejects.toThrow('date is mandatory');
    const dateObj = { name: 'surendhar.s', date: '1999-13-12' };
    const errorMsg =
        'Value {"name":"surendhar.s","date":"1999-13-12"} is not of valid type(s)' +
        '\n' +
        'Type is missing in schema for declared DATE format.';

    expect(SchemaValidator.validate([], objSchema, repo, dateObj)).rejects.toThrow(errorMsg);
});

test('Schema Validator pass with date test ', async () => {
    let intSchema: Schema = new Schema().setType(TypeUtil.of(SchemaType.INTEGER)).setMinimum(100);

    let objSchema: Schema = Schema.ofObject('testObj')
        .setProperties(
            new Map<string, Schema>([
                ['intSchema', intSchema],
                ['name', Schema.ofString('name')],
                [
                    'date',
                    new Schema()
                        .setFormat(StringFormat.DATE)
                        .setType(TypeUtil.of(SchemaType.STRING)),
                ],
            ]),
        )
        .setRequired(['intSchema', 'date']);

    const dateObj = { intSchema: 1231, date: '1999-09-12' };

    expect(await SchemaValidator.validate([], objSchema, repo, dateObj)).toBe(dateObj);
});

test('Schema Validator fail with time string type missing test ', async () => {
    let intSchema: Schema = new Schema()
        .setType(TypeUtil.of(SchemaType.INTEGER))
        .setMaximum(100)
        .setMultipleOf(5);

    let objSchema: Schema = Schema.ofObject('testObj')
        .setProperties(
            new Map<string, Schema>([
                ['intSchema', intSchema],
                ['name', Schema.ofString('name').setMinLength(10)],
                ['time', new Schema().setFormat(StringFormat.TIME)],
            ]),
        )
        .setRequired(['intSchema', 'time', 'name']);

    const timeObj = { intSchema: 95, time: '22:23:61', name: 's.surendhar' };

    const errMsg =
        'Value {"intSchema":95,"time":"22:23:61","name":"s.surendhar"} is not of valid type(s)\n' +
        'Type is missing in schema for declared TIME format.';

    expect(SchemaValidator.validate([], objSchema, repo, timeObj)).rejects.toThrow(errMsg);
});

test('Schema Validator fail with time test ', async () => {
    let intSchema: Schema = new Schema()
        .setType(TypeUtil.of(SchemaType.INTEGER))
        .setMaximum(100)
        .setMultipleOf(5);

    let objSchema: Schema = Schema.ofObject('testObj')
        .setProperties(
            new Map<string, Schema>([
                ['intSchema', intSchema],
                ['name', Schema.ofString('name').setMinLength(10)],
                [
                    'time',
                    new Schema()
                        .setFormat(StringFormat.TIME)
                        .setType(TypeUtil.of(SchemaType.STRING)),
                ],
            ]),
        )
        .setRequired(['intSchema', 'time', 'name']);

    const timeObj = { intSchema: 95, time: '22:23:61', name: 's.surendhar' };

    const errMsg =
        'Value {"intSchema":95,"time":"22:23:61","name":"s.surendhar"} is not of valid type(s)\n' +
        'Value "22:23:61" is not of valid type(s)\n' +
        '22:23:61 is not matched with the time pattern';

    expect(SchemaValidator.validate([], objSchema, repo, timeObj)).rejects.toThrow(errMsg);
});

test('Schema Validator pass with time test ', async () => {
    let intSchema: Schema = new Schema()
        .setType(TypeUtil.of(SchemaType.INTEGER))
        .setMaximum(100)
        .setMultipleOf(5);

    let objSchema: Schema = Schema.ofObject('testObj')
        .setProperties(
            new Map<string, Schema>([
                ['intSchema', intSchema],
                ['name', Schema.ofString('name').setMinLength(10)],
                [
                    'time',
                    new Schema()
                        .setFormat(StringFormat.TIME)
                        .setType(TypeUtil.of(SchemaType.STRING)),
                ],
            ]),
        )
        .setRequired(['intSchema', 'time', 'name']);

    const timeObj = { intSchema: 95, time: '22:23:24', name: 's.surendhar' };

    expect(await SchemaValidator.validate([], objSchema, repo, timeObj)).toBe(timeObj);
});

test('Schema Validator fail with email string type missing test ', async () => {
    let intSchema: Schema = new Schema().setType(TypeUtil.of(SchemaType.INTEGER)).setMaximum(100);

    let objSchema: Schema = Schema.ofObject('testObj')
        .setProperties(
            new Map<string, Schema>([
                ['intSchema', intSchema],
                ['name', Schema.ofString('name').setMinLength(10)],
                ['email', new Schema().setFormat(StringFormat.EMAIL)],
            ]),
        )
        .setRequired(['intSchema', 'email', 'name']);

    const emailObj = { intSchema: 95, email: 'iosdjfdf123--@gmail.com', name: 's.surendhar' };

    const errMsg =
        'Value {"intSchema":95,"email":"iosdjfdf123--@gmail.com","name":"s.surendhar"} is not of valid type(s)\n' +
        'Type is missing in schema for declared EMAIL format.';

    expect(SchemaValidator.validate([], objSchema, repo, emailObj)).rejects.toThrow(errMsg);
});

test('Schema Validator fail with email test ', async () => {
    let intSchema: Schema = new Schema()
        .setType(TypeUtil.of(SchemaType.INTEGER))
        .setMaximum(3)
        .setMultipleOf(5);

    let objSchema: Schema = Schema.ofObject('testObj')
        .setProperties(
            new Map<string, Schema>([
                ['intSchema', intSchema],
                ['name', Schema.ofString('name').setMinLength(10)],
                [
                    'email',
                    new Schema()
                        .setFormat(StringFormat.EMAIL)
                        .setType(TypeUtil.of(SchemaType.STRING)),
                ],
            ]),
        )
        .setRequired(['intSchema', 'email']);

    const emailObj = { intSchema: 0, email: 'asdasdf@@*.com' };

    const errMsg =
        'Value {"intSchema":0,"email":"asdasdf@@*.com"} is not of valid type(s)\n' +
        'Value "asdasdf@@*.com" is not of valid type(s)\n' +
        'asdasdf@@*.com is not matched with the email pattern';

    expect(SchemaValidator.validate([], objSchema, repo, emailObj)).rejects.toThrow(errMsg);
});

test('Schema Validator pass with time test ', async () => {
    let intSchema: Schema = new Schema().setType(TypeUtil.of(SchemaType.INTEGER));

    let objSchema: Schema = Schema.ofObject('testObj')
        .setProperties(
            new Map<string, Schema>([
                ['intSchema', intSchema],
                ['name', Schema.ofString('name').setMinLength(10)],
                [
                    'email',
                    new Schema()
                        .setFormat(StringFormat.EMAIL)
                        .setType(TypeUtil.of(SchemaType.STRING)),
                ],
            ]),
        )
        .setRequired(['intSchema', 'name']);

    const emailObj = { intSchema: 95, email: 'surendhar.s@finc.c', name: 's.surendhar' };

    expect(await SchemaValidator.validate([], objSchema, repo, emailObj)).toBe(emailObj);
});

test('Schema Validator fail with dateTime string type missing test ', async () => {
    let intSchema: Schema = new Schema().setType(TypeUtil.of(SchemaType.INTEGER)).setMaximum(100);

    let objSchema: Schema = Schema.ofObject('testObj')
        .setProperties(
            new Map<string, Schema>([
                ['intSchema', intSchema],
                ['name', Schema.ofString('name').setMinLength(10)],
                ['dateTime', new Schema().setFormat(StringFormat.DATETIME)],
            ]),
        )
        .setRequired(['intSchema', 'dateTime', 'name']);

    const emailObj = { intSchema: 95, dateTime: '2023-08-21T07:56:45+12:12', name: 's.surendhar' };

    const errMsg =
        'Value {"intSchema":95,"dateTime":"2023-08-21T07:56:45+12:12","name":"s.surendhar"} is not of valid type(s)\n' +
        'Type is missing in schema for declared DATETIME format.';

    expect(SchemaValidator.validate([], objSchema, repo, emailObj)).rejects.toThrow(errMsg);
});

test('Schema Validator fail with dateTime test ', async () => {
    let intSchema: Schema = new Schema()
        .setType(TypeUtil.of(SchemaType.INTEGER))
        .setMaximum(3)
        .setMultipleOf(5);

    let objSchema: Schema = Schema.ofObject('testObj')
        .setProperties(
            new Map<string, Schema>([
                ['intSchema', intSchema],
                ['name', Schema.ofString('name').setMinLength(10)],
                [
                    'dateTime',
                    new Schema()
                        .setFormat(StringFormat.DATETIME)
                        .setType(TypeUtil.of(SchemaType.STRING)),
                ],
            ]),
        )
        .setRequired(['dateTime']);

    const dateTimeObj = { dateTime: '2023-08-221T07:56:45+12:12' };

    const errMsg =
        'Value {"dateTime":"2023-08-221T07:56:45+12:12"} is not of valid type(s)\n' +
        'Value "2023-08-221T07:56:45+12:12" is not of valid type(s)\n' +
        '2023-08-221T07:56:45+12:12 is not matched with the date time pattern';

    expect(SchemaValidator.validate([], objSchema, repo, dateTimeObj)).rejects.toThrow(errMsg);
});

test('Schema Validator pass with time test ', async () => {
    let intSchema: Schema = new Schema().setType(TypeUtil.of(SchemaType.INTEGER));

    let objSchema: Schema = Schema.ofObject('testObj')
        .setProperties(
            new Map<string, Schema>([
                ['intSchema', intSchema],
                ['name', Schema.ofString('name').setMinLength(10)],
                [
                    'dateTime',
                    new Schema()
                        .setFormat(StringFormat.DATETIME)
                        .setType(TypeUtil.of(SchemaType.STRING)),
                ],
            ]),
        )
        .setRequired(['intSchema', 'dateTime']);

    const dateTimeObj = {
        intSchema: 95,
        dateTime: '2023-08-21T07:56:45+12:12',
        name: 's.surendhar',
    };

    expect(await SchemaValidator.validate([], objSchema, repo, dateTimeObj)).toBe(dateTimeObj);
});

const dateTimeObj = { dateTime: '2023-08-21T07:56:45+12:12' };


test('Schema Validator fail with custom minValue and maxValue messages', async () => {
    let intSchema: Schema = new Schema()
        .setType(TypeUtil.of(SchemaType.INTEGER))
        .setMinimum(10)
        .setMaximum(20)
        .setUiHelper(
            new UiHelper()
                .setMinValueMessage('Custom minValue message for {value}')
                .setMaxValueMessage('Custom maxValue message for {value}')
        );

    let objSchema: Schema = Schema.ofObject('testObj')
        .setProperties(
            new Map<string, Schema>([
                ['intSchema', intSchema],
                ['name', Schema.ofString('name').setMinLength(5)],
            ]),
        )
        .setRequired(['intSchema', 'name']);

    const belowMinObj = { intSchema: 5, name: 'sampleName' };
    const aboveMaxObj = { intSchema: 25, name: 'sampleName' };

     expect(SchemaValidator.validate([], objSchema, repo, belowMinObj)).rejects.toThrow(
        'Custom minValue message for 5',
    );

     expect(SchemaValidator.validate([], objSchema, repo, aboveMaxObj)).rejects.toThrow(
        'Custom maxValue message for 25',
    );
});

import {
    ArrayValidator,
    BooleanValidator,
    KIRunSchemaRepository,
    MapUtil,
    NumberValidator,
    Schema,
    SchemaType,
    StringValidator,
    TypeValidator,
} from '../../../../../src';
import { StringConvertor } from '../../../../../src/engine/json/schema/convertor/StringConvertor';
import { ConversionMode } from '../../../../../src/engine/json/schema/convertor/enums/ConversionMode';
import { NumberConvertor } from '../../../../../src/engine/json/schema/convertor/NumberConvertor';
import { BooleanConvertor } from '../../../../../src/engine/json/schema/convertor/BooleanConvertor';
import { NullConvertor } from '../../../../../src/engine/json/schema/convertor/NullConvertor';

const repo = new KIRunSchemaRepository();

test('Type Validator for Number', async () => {
    const element = { value: 123 };
    const schema = new Schema();

    const result = await TypeValidator.validate(
        [],
        SchemaType.INTEGER,
        schema,
        repo,
        element.value,
    );
    expect(result).toEqual(NumberValidator.validate(SchemaType.INTEGER, [], schema, element.value));
});

test('Type Validator for String', async () => {
    const element = { value: 'string' };
    const schema = new Schema();

    const result = await TypeValidator.validate([], SchemaType.STRING, schema, repo, element.value);
    expect(result).toEqual(StringValidator.validate([], schema, element.value));
});

test('Type Validator for Boolean', async () => {
    const element = { value: true };
    const schema = new Schema();

    const result = await TypeValidator.validate(
        [],
        SchemaType.BOOLEAN,
        schema,
        repo,
        element.value,
    );
    expect(result).toEqual(BooleanValidator.validate([], schema, element.value));
});

test('Type Validator for Array', async () => {
    const schema = new Schema();
    const array = ['abc'];

    const result = await TypeValidator.validate([], SchemaType.ARRAY, schema, repo, array);
    const expected = await ArrayValidator.validate([], schema, repo, array);
    expect(result).toEqual(expected);
});

test('Type Validator for Null', async () => {
    const schema = new Schema();

    const result = await TypeValidator.validate([], SchemaType.NULL, schema, repo, null);
    expect(result).toBeNull();
});

test('Type Validator with String Conversion', async () => {
    const element = { value: 'string' };
    const schema = new Schema();

    const result = await TypeValidator.validate(
        [],
        SchemaType.STRING,
        schema,
        repo,
        element.value,
        true,
        ConversionMode.STRICT,
    );
    expect(result).toEqual(
        StringConvertor.convert([], schema, ConversionMode.STRICT, element.value),
    );
});

test('Type Validator with Number Conversion', async () => {
    const element = { value: '12345' };
    const schema = new Schema();

    const result = await TypeValidator.validate(
        [],
        SchemaType.INTEGER,
        schema,
        repo,
        element.value,
        true,
        ConversionMode.STRICT,
    );
    expect(result).toEqual(
        NumberConvertor.convert(
            [],
            SchemaType.INTEGER,
            schema,
            ConversionMode.STRICT,
            element.value,
        ),
    );
});

test('Type Validator with Boolean Conversion', async () => {
    const element = { value: 'true' };
    const schema = new Schema();

    const result = await TypeValidator.validate(
        [],
        SchemaType.BOOLEAN,
        schema,
        repo,
        element.value,
        true,
        ConversionMode.STRICT,
    );
    expect(result).toEqual(
        BooleanConvertor.convert([], schema, ConversionMode.STRICT, element.value),
    );
});

test('Type Validator with Null Conversion', async () => {
    const element = null;
    const schema = new Schema();

    const result = await TypeValidator.validate(
        [],
        SchemaType.NULL,
        schema,
        repo,
        element,
        true,
        ConversionMode.STRICT,
    );
    expect(result).toEqual(NullConvertor.convert([], schema, ConversionMode.STRICT, element));
});

test('Boolean Convertor Array', async () => {
    const schema = Schema.ofArray('boolean', Schema.ofBoolean('boolean'));

    const booleanArray: (string | number | boolean)[] = [true, false, 'yes', 'no', 'y', 'n', 1, 0];

    const expectedArray: boolean[] = [true, false, true, false, true, false, true, false];

    const result: any = await TypeValidator.validate(
        [],
        SchemaType.ARRAY,
        schema,
        repo,
        booleanArray,
        true,
        ConversionMode.STRICT,
    );

    expect(result).toEqual(expectedArray);
});

test('Number Convertor Array', async () => {
    const schema = Schema.ofArray('number', Schema.ofNumber('number'));

    const numberArray = ['11', '11.12', '11192371231', '1123.123', '0'];

    const expectedArray = [11, 11.12, 11192371231, 1123.123, 0];

    const result = await TypeValidator.validate(
        [],
        SchemaType.ARRAY,
        schema,
        repo,
        numberArray,
        true,
        ConversionMode.LENIENT,
    );

    expect(result).toEqual(expectedArray);
});

test('Object Conversion', async () => {
    const numberArray: string[] = ['11', '11.12', '11192371231', '1123.123', '0'];

    const object = {
        int: '1997',
        long: '123123123',
        float: '123.12',
        double: '123.1232',
        booleanTrue: 'true',
        booleanFalse: 'false',
        string: 12314,
        numberArray: numberArray,
    };

    const schema: Schema = Schema.ofObject('jsonObject').setProperties(
        MapUtil.of(
            'int',
            Schema.ofNumber('int'),
            'long',
            Schema.ofLong('long'),
            'float',
            Schema.ofFloat('float'),
            'double',
            Schema.ofNumber('double'),
            'booleanTrue',
            Schema.ofBoolean('booleanTrue'),
            'booleanFalse',
            Schema.ofBoolean('booleanFalse'),
            'string',
            Schema.ofString('string'),
            'numberArray',
            Schema.ofArray('number', Schema.ofNumber('number')),
        ),
    );

    const expectedObject = {
        int: 1997,
        long: 123123123,
        float: 123.12,
        double: 123.1232,
        booleanTrue: true,
        booleanFalse: false,
        string: '12314',
        numberArray: [11, 11.12, 11192371231, 1123.123, 0],
    };

    const result: any = await TypeValidator.validate(
        [],
        SchemaType.OBJECT,
        schema,
        repo,
        object,
        true,
        ConversionMode.STRICT,
    );

    expect(result).toEqual(expectedObject);
});

test('Object of Object Conversion', async () => {
    const innerObject = {
        innerInt: '42',
        innerString: 'innerValue',
    };

    const outerObject = {
        innerObject: innerObject,
        outerInt: '100',
    };

    const innerSchema: Schema = Schema.ofObject('innerObject').setProperties(
        MapUtil.of(
            'innerInt',
            Schema.ofNumber('innerInt'),
            'innerString',
            Schema.ofString('innerString'),
        ),
    );

    const outerSchema: Schema = Schema.ofObject('outerObject').setProperties(
        MapUtil.of('innerObject', innerSchema, 'outerInt', Schema.ofNumber('outerInt')),
    );

    const expectedOuterObject = {
        innerObject: {
            innerInt: 42,
            innerString: 'innerValue',
        },
        outerInt: 100,
    };

    const result: any = await TypeValidator.validate(
        [],
        SchemaType.OBJECT,
        outerSchema,
        repo,
        outerObject,
        true,
        ConversionMode.STRICT,
    );

    expect(result).toEqual(expectedOuterObject);
});

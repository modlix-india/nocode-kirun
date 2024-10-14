import { NumberConvertor } from '../../../../../src/engine/json/schema/convertor/NumberConvertor';
import { Schema, SchemaType } from '../../../../../src';
import { ConversionMode } from '../../../../../src/engine/json/schema/convertor/enums/ConversionMode';
import { SchemaConversionException } from '../../../../../src/engine/json/schema/convertor/exception/SchemaConversionException';

const convertElement = (schemaType: SchemaType, mode: ConversionMode, element: any) => {
    return NumberConvertor.convert([], schemaType, new Schema(), mode, element);
};

const convertElementWithSchema = (
    schemaType: SchemaType,
    schema: Schema,
    mode: ConversionMode,
    element: any,
) => {
    return NumberConvertor.convert([], schemaType, schema, mode, element);
};

test('Convert null element (strict mode)', async () => {
    expect(() => convertElement(SchemaType.INTEGER, ConversionMode.STRICT, null)).toThrow(
        SchemaConversionException,
    );
});

test('Convert non-number element (strict mode)', async () => {
    const nonNumber = 'not a number';
    expect(() => convertElement(SchemaType.INTEGER, ConversionMode.STRICT, nonNumber)).toThrow(
        SchemaConversionException,
    );
});

test('Convert valid integer (strict mode)', async () => {
    const result = convertElement(SchemaType.INTEGER, ConversionMode.STRICT, 42);
    expect(result).toBe(42);
});

test('Convert valid long (strict mode)', async () => {
    const result = convertElement(SchemaType.LONG, ConversionMode.STRICT, 42);
    expect(result).toBe(42);
});

test('Convert valid double (strict mode)', async () => {
    const result = convertElement(SchemaType.DOUBLE, ConversionMode.STRICT, 42.0);
    expect(result).toBe(42.0);
});

test('Convert valid float (strict mode)', async () => {
    const result = convertElement(SchemaType.FLOAT, ConversionMode.STRICT, 42.0);
    expect(result).toBe(42.0);
});

test('Convert invalid integer (strict mode)', async () => {
    const invalidInteger = 42.1;
    expect(() => convertElement(SchemaType.INTEGER, ConversionMode.STRICT, invalidInteger)).toThrow(
        SchemaConversionException,
    );
});

test('Convert invalid long (strict mode)', async () => {
    const invalidLong = 2312.451;
    expect(() => convertElement(SchemaType.LONG, ConversionMode.STRICT, invalidLong)).toThrow(
        SchemaConversionException,
    );
});

test('Convert integer use default mode', async () => {
    const result = convertElementWithSchema(
        SchemaType.INTEGER,
        new Schema(),
        ConversionMode.USE_DEFAULT,
        42,
    );
    expect(result).toBe(42);
});

test('Convert integer skip mode', async () => {
    const result = convertElementWithSchema(
        SchemaType.INTEGER,
        new Schema(),
        ConversionMode.SKIP,
        null,
    );
    expect(result).toBe(null);
});

test('Convert integer lenient mode', async () => {
    const result = convertElementWithSchema(
        SchemaType.INTEGER,
        new Schema(),
        ConversionMode.LENIENT,
        '42',
    );
    expect(result).toBe(42);
});

test('Convert long use default mode', async () => {
    const result = convertElementWithSchema(
        SchemaType.LONG,
        new Schema(),
        ConversionMode.USE_DEFAULT,
        42,
    );
    expect(result).toBe(42);
});

test('Convert long skip mode', async () => {
    const result = convertElementWithSchema(
        SchemaType.LONG,
        new Schema(),
        ConversionMode.SKIP,
        null,
    );
    expect(result).toBe(null);
});

test('Convert long lenient mode', async () => {
    const result = convertElementWithSchema(
        SchemaType.LONG,
        new Schema(),
        ConversionMode.LENIENT,
        '42',
    );
    expect(result).toBe(42);
});

test('Convert float use default mode', async () => {
    const result = convertElementWithSchema(
        SchemaType.FLOAT,
        new Schema(),
        ConversionMode.USE_DEFAULT,
        42.0,
    );
    expect(result).toBe(42.0);
});

test('Convert float skip mode', async () => {
    const result = convertElementWithSchema(
        SchemaType.FLOAT,
        new Schema(),
        ConversionMode.SKIP,
        null,
    );
    expect(result).toBe(null);
});

test('Convert float lenient mode', async () => {
    const result = convertElementWithSchema(
        SchemaType.FLOAT,
        new Schema(),
        ConversionMode.LENIENT,
        '42.0',
    );
    expect(result).toBe(42.0);
});

import { ConversionMode } from '../../../../../src/engine/json/schema/convertor/enums/ConversionMode';
import { Schema } from '../../../../../src';
import { NullConvertor } from '../../../../../src/engine/json/schema/convertor/NullConvertor';
import { SchemaConversionException } from '../../../../../src/engine/json/schema/convertor/exception/SchemaConversionException';

const convertElement = (mode: ConversionMode, element: any) => {
    return NullConvertor.convert([], new Schema(), mode, element);
};

test('Null convert null element (strict mode)', async () => {
    const result = convertElement(ConversionMode.STRICT, null);
    expect(result).toBe(null);
});

test('Null convert null JSON element (strict mode)', async () => {
    const result = convertElement(ConversionMode.STRICT, null);
    expect(result).toBe(null);
});

test('Null convert string "null" (strict mode)', async () => {
    const element = 'null';
    const result = convertElement(ConversionMode.STRICT, element);
    expect(result).toBe(null);
});

test('Null convert invalid element (strict mode)', async () => {
    const element = 'invalid';
    expect(() => convertElement(ConversionMode.STRICT, element)).toThrow(SchemaConversionException);
});

test('Null convert invalid element (lenient mode)', async () => {
    const element = 'invalid';
    const result = convertElement(ConversionMode.LENIENT, element);
    expect(result).toBe(null);
});

test('Null convert use default mode', async () => {
    const element = 'invalid';
    const result = convertElement(ConversionMode.USE_DEFAULT, element);
    expect(result).toBe(null);
});

test('Null convert skip mode', async () => {
    const element = 'invalid';
    const result = convertElement(ConversionMode.SKIP, element);
    expect(result).toBe(element);
});

import { BooleanConvertor } from '../../../../../src/engine/json/schema/convertor/BooleanConvertor';
import { Schema } from '../../../../../src';
import { ConversionMode } from '../../../../../src/engine/json/schema/convertor/enums/ConversionMode';
import { SchemaConversionException } from '../../../../../src/engine/json/schema/convertor/exception/SchemaConversionException';

const convertElement = (element: any, mode: ConversionMode) => {
    return BooleanConvertor.convert([], new Schema(), mode, element);
};

test('Boolean convert null element (strict mode)', async () => {
    expect(() => convertElement(null, ConversionMode.STRICT)).toThrow(SchemaConversionException);
});

test('Boolean convert true string (strict mode)', async () => {
    const result = convertElement('true', ConversionMode.STRICT);
    expect(result).toEqual(true);
});

test('Boolean convert false string (strict mode)', async () => {
    const result = convertElement('false', ConversionMode.STRICT);
    expect(result).toEqual(false);
});

test('Boolean convert one as number (strict mode)', async () => {
    const result = convertElement(1, ConversionMode.STRICT);
    expect(result).toEqual(true);
});

test('Boolean convert zero as number (strict mode)', async () => {
    const result = convertElement(0, ConversionMode.STRICT);
    expect(result).toEqual(false);
});

test('Boolean convert invalid string (strict mode)', async () => {
    const invalid = 'invalid';
    expect(() => convertElement(invalid, ConversionMode.STRICT)).toThrow(SchemaConversionException);
});

test('Boolean convert invalid string (lenient mode)', async () => {
    const result = convertElement('invalid', ConversionMode.LENIENT);
    expect(result).toEqual(null); // Assuming lenient mode returns null for invalid input
});

test('Boolean convert use default mode', async () => {
    const defaultValue = true;
    const schema = new Schema().setDefaultValue(defaultValue);
    const result = BooleanConvertor.convert([], schema, ConversionMode.USE_DEFAULT, 'invalid');
    expect(result).toEqual(defaultValue);
});

test('Boolean convert skip mode', async () => {
    const element = 'invalid';
    const result = convertElement(element, ConversionMode.SKIP);
    expect(result).toEqual(element);
});

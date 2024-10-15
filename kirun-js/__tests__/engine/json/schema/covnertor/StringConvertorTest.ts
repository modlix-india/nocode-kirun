import { StringConvertor } from '../../../../../src/engine/json/schema/convertor/StringConvertor';
import { Schema } from '../../../../../src';
import { ConversionMode } from '../../../../../src/engine/json/schema/convertor/enums/ConversionMode';
import { SchemaConversionException } from '../../../../../src/engine/json/schema/convertor/exception/SchemaConversionException';

const convertElement = (schema: Schema, mode: ConversionMode, element: any) => {
    return StringConvertor.convert([], schema, mode, element);
};

test('Convert valid string (strict mode)', async () => {
    const schema = new Schema();
    const element = 'test string';
    const result = convertElement(schema, ConversionMode.STRICT, element);
    expect(result).toBe('test string');
});

test('Convert null element (strict mode)', async () => {
    const schema = new Schema();
    expect(() => convertElement(schema, ConversionMode.STRICT, null)).toThrow(
        SchemaConversionException,
    );
});

test('Convert null object (strict mode)', async () => {
    const schema = new Schema();
    expect(() => convertElement(schema, ConversionMode.STRICT, null)).toThrow(
        SchemaConversionException,
    );
});

test('Convert empty element (strict mode)', async () => {
    const schema = new Schema();
    const element = '';
    const result = convertElement(schema, ConversionMode.STRICT, element);
    expect(result).toBe('');
});

test('Convert non-primitive element (strict mode)', async () => {
    const schema = new Schema();
    const element = 123;
    const result = convertElement(schema, ConversionMode.STRICT, element);
    expect(result).toBe('123');
});

test('Convert use default mode', async () => {
    const schema = new Schema();
    schema.setDefaultValue('default value');
    const result = convertElement(schema, ConversionMode.USE_DEFAULT, null);
    expect(result).toBe('default value');
});

test('Convert skip mode', async () => {
    const schema = new Schema();
    const result = convertElement(schema, ConversionMode.SKIP, null);
    expect(result).toBe(null);
});

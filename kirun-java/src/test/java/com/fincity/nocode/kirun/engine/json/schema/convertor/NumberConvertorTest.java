package com.fincity.nocode.kirun.engine.json.schema.convertor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.convertor.enums.ConversionMode;
import com.fincity.nocode.kirun.engine.json.schema.convertor.exception.SchemaConversionException;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

class NumberConvertorTest {

	private JsonElement convertElement(SchemaType schemaType, ConversionMode mode, JsonElement element) {
		return NumberConvertor.convert(Collections.emptyList(), schemaType, new Schema(), mode, element);
	}

	private JsonElement convertElement(SchemaType schemaType, Schema schema, ConversionMode mode, JsonElement element) {
		return NumberConvertor.convert(Collections.emptyList(), schemaType, schema, mode, element);
	}

	@Test
	void testConvertNullElement() {
		assertThrows(SchemaConversionException.class,
				() -> convertElement(SchemaType.INTEGER, ConversionMode.STRICT, JsonNull.INSTANCE));
	}

	@Test
	void testConvertNonNumberElement() {
		JsonElement nonNumber = new JsonPrimitive("not a number");

		assertThrows(SchemaConversionException.class,
				() -> convertElement(SchemaType.INTEGER, ConversionMode.STRICT, nonNumber));
	}

	@Test
	void testConvertValidInteger() {
		JsonElement result = convertElement(SchemaType.INTEGER, ConversionMode.STRICT, new JsonPrimitive(42));
		assertEquals(42, result.getAsInt());
	}

	@Test
	void testConvertValidLong() {
		JsonElement result = convertElement(SchemaType.LONG, ConversionMode.STRICT, new JsonPrimitive(42L));
		assertEquals(42L, result.getAsLong());
	}

	@Test
	void testConvertValidDouble() {
		JsonElement result = convertElement(SchemaType.DOUBLE, ConversionMode.STRICT, new JsonPrimitive(42.0));
		assertEquals(42.0, result.getAsDouble());
	}

	@Test
	void testConvertValidFloat() {
		JsonElement result = convertElement(SchemaType.FLOAT, ConversionMode.STRICT, new JsonPrimitive(42.0f));
		assertEquals(42.0f, result.getAsFloat());
	}

	@Test
	void testConvertInvalidInteger() {
		JsonElement invalidInteger = new JsonPrimitive(42.1);
		assertThrows(SchemaConversionException.class,
				() -> convertElement(SchemaType.INTEGER, ConversionMode.STRICT, invalidInteger));
	}

	@Test
	void testConvertInvalidLong() {
		JsonElement invalidLong = new JsonPrimitive(2312.451);
		assertThrows(SchemaConversionException.class,
				() -> convertElement(SchemaType.LONG, ConversionMode.STRICT, invalidLong));
	}

	@Test
	void testConvertIntegerUseDefaultMode() {
		JsonElement result = convertElement(SchemaType.INTEGER, new Schema(), ConversionMode.USE_DEFAULT, new JsonPrimitive(42));
		assertEquals(42, result.getAsInt());
	}

	@Test
	void testConvertIntegerSkipMode() {
		JsonElement result = convertElement(SchemaType.INTEGER, new Schema(), ConversionMode.SKIP, JsonNull.INSTANCE);
		assertEquals(JsonNull.INSTANCE, result);
	}

	@Test
	void testConvertIntegerLenientMode() {
		JsonElement result = convertElement(SchemaType.INTEGER, new Schema(), ConversionMode.LENIENT, new JsonPrimitive("42"));
		assertEquals(42, result.getAsInt());
	}

	@Test
	void testConvertLongUseDefaultMode() {
		JsonElement result = convertElement(SchemaType.LONG, new Schema(), ConversionMode.USE_DEFAULT,
				new JsonPrimitive(42L));
		assertEquals(42L, result.getAsLong());
	}

	@Test
	void testConvertLongSkipMode() {
		JsonElement result = convertElement(SchemaType.LONG, new Schema(), ConversionMode.SKIP, JsonNull.INSTANCE);
		assertEquals(JsonNull.INSTANCE, result);
	}

	@Test
	void testConvertLongLenientMode() {
		JsonElement result = convertElement(SchemaType.LONG, new Schema(), ConversionMode.LENIENT,
				new JsonPrimitive("42"));
		assertEquals(42L, result.getAsLong());
	}

	@Test
	void testConvertFloatUseDefaultMode() {
		JsonElement result = convertElement(SchemaType.FLOAT, new Schema(), ConversionMode.USE_DEFAULT,
				new JsonPrimitive(42.0f));
		assertEquals(42.0f, result.getAsFloat());
	}

	@Test
	void testConvertFloatSkipMode() {
		JsonElement result = convertElement(SchemaType.FLOAT, new Schema(), ConversionMode.SKIP, JsonNull.INSTANCE);
		assertEquals(JsonNull.INSTANCE, result);
	}

	@Test
	void testConvertFloatLenientMode() {
		JsonElement result = convertElement(SchemaType.FLOAT, new Schema(), ConversionMode.LENIENT,
				new JsonPrimitive("42.0"));
		assertEquals(42.0f, result.getAsFloat());
	}
}

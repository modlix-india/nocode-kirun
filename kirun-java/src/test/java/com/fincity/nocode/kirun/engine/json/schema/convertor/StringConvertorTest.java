package com.fincity.nocode.kirun.engine.json.schema.convertor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.convertor.enums.ConversionMode;
import com.fincity.nocode.kirun.engine.json.schema.convertor.exception.SchemaConversionException;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

class StringConvertorTest {

	private JsonElement convertElement(Schema schema, ConversionMode mode, JsonElement element) {
		return StringConvertor.convert(Collections.emptyList(), schema, mode, element);
	}

	@Test
	void testConvert_ValidString() {
		Schema schema = new Schema();
		JsonElement element = new JsonPrimitive("test string");
		JsonElement result = convertElement(schema, ConversionMode.STRICT, element);
		assertEquals("test string", result.getAsString());
	}

	@Test
	void testConvert_NullElement() {
		Schema schema = new Schema();
		assertThrows(SchemaConversionException.class,
				() -> convertElement(schema, ConversionMode.STRICT, null));
	}

	@Test
	void testConvert_NullJsonElement() {
		Schema schema = new Schema();
		assertThrows(SchemaConversionException.class,
				() -> convertElement(schema, ConversionMode.STRICT, JsonNull.INSTANCE));
	}

	@Test
	void testConvert_EmptyJsonPrimitive() {
		Schema schema = new Schema();
		JsonElement element = new JsonPrimitive("");
		JsonElement result = convertElement(schema, ConversionMode.STRICT, element);
		assertEquals("", result.getAsString());
	}

	@Test
	void testConvert_NonPrimitiveElement() {
		Schema schema = new Schema();
		JsonElement element = new JsonPrimitive(123); // Non-string primitive
		JsonElement result = convertElement(schema, ConversionMode.STRICT, element);
		assertEquals("123", result.getAsString());
	}

	@Test
	void testConvert_UseDefaultMode() {
		Schema schema = new Schema();
		schema.setDefaultValue(new JsonPrimitive("default value"));
		JsonElement result = convertElement(schema, ConversionMode.USE_DEFAULT, JsonNull.INSTANCE);
		assertEquals("default value", result.getAsString());
	}

	@Test
	void testConvert_SkipMode() {
		Schema schema = new Schema();
		JsonElement result = convertElement(schema, ConversionMode.SKIP, JsonNull.INSTANCE);
		assertEquals(JsonNull.INSTANCE, result);
	}
}

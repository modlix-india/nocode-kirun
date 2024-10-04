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

class NullConvertorTest {

	private JsonElement convertElement(ConversionMode mode, JsonElement element) {
		return NullConvertor.convert(Collections.emptyList(), new Schema(), mode, element);
	}

	@Test
	void testNullConvertNullElement() {
		JsonElement result = convertElement(ConversionMode.STRICT, null);
		assertEquals(JsonNull.INSTANCE, result);
	}

	@Test
	void testNullConvertNullJsonElement() {
		JsonElement result = convertElement(ConversionMode.STRICT, JsonNull.INSTANCE);
		assertEquals(JsonNull.INSTANCE, result);
	}

	@Test
	void testNullConvertStringNull() {
		JsonElement element = new JsonPrimitive("null");
		JsonElement result = convertElement(ConversionMode.STRICT, element);
		assertEquals(JsonNull.INSTANCE, result);
	}

	@Test
	void testNullConvertInvalidElement() {
		JsonElement element = new JsonPrimitive("invalid");
		assertThrows(SchemaConversionException.class, () -> convertElement(ConversionMode.STRICT, element));
	}

	@Test
	void testNullConvertLenientMode() {
		JsonElement element = new JsonPrimitive("invalid");
		JsonElement result = convertElement(ConversionMode.LENIENT, element);
		assertEquals(JsonNull.INSTANCE, result);
	}

	@Test
	void testNullConvertUseDefaultMode() {
		JsonElement element = new JsonPrimitive("invalid");
		JsonElement result = convertElement(ConversionMode.USE_DEFAULT, element);
		assertEquals(JsonNull.INSTANCE, result);
	}

	@Test
	void testNullConvertSkipMode() {
		JsonElement element = new JsonPrimitive("invalid");
		JsonElement result = convertElement(ConversionMode.SKIP, element);
		assertEquals(element, result);
	}

}

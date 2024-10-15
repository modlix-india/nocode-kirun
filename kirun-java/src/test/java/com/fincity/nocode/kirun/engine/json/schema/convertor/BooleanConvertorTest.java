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

class BooleanConvertorTest {

	private JsonElement convertElement(JsonElement element, ConversionMode mode) {
		return BooleanConvertor.convert(Collections.emptyList(), new Schema(), mode, element);
	}

	@Test
	void testBooleanConvertNullElement() {
		assertThrows(SchemaConversionException.class, () -> convertElement(null, ConversionMode.STRICT));
	}

	@Test
	void testBooleanConvertTrueString() {
		JsonElement result = convertElement(new JsonPrimitive("true"), ConversionMode.STRICT);
		assertEquals(new JsonPrimitive(true), result);
	}

	@Test
	void testBooleanConvertFalseString() {
		JsonElement result = convertElement(new JsonPrimitive("false"), ConversionMode.STRICT);
		assertEquals(new JsonPrimitive(false), result);
	}

	@Test
	void testBooleanConvertOneAsNumber() {
		JsonElement result = convertElement(new JsonPrimitive(1), ConversionMode.STRICT);
		assertEquals(new JsonPrimitive(true), result);
	}

	@Test
	void testBooleanConvertZeroAsNumber() {
		JsonElement result = convertElement(new JsonPrimitive(0), ConversionMode.STRICT);
		assertEquals(new JsonPrimitive(false), result);
	}

	@Test
	void testBooleanConvertInvalidString() {
		JsonPrimitive invalid = new JsonPrimitive("invalid");
		assertThrows(SchemaConversionException.class, () -> convertElement(invalid, ConversionMode.STRICT));
	}

	@Test
	void testBooleanConvertLenientMode() {
		JsonElement result = convertElement(new JsonPrimitive("invalid"), ConversionMode.LENIENT);
		assertEquals(JsonNull.INSTANCE, result);
	}

	@Test
	void testBooleanConvertUseDefaultMode() {
		JsonElement defaultValue = new JsonPrimitive("true");
		Schema schema = new Schema().setDefaultValue(defaultValue);
		JsonElement result = BooleanConvertor.convert(Collections.emptyList(), schema, ConversionMode.USE_DEFAULT, new JsonPrimitive("invalid"));
		assertEquals(defaultValue, result);
	}

	@Test
	void testBooleanConvertSkipMode() {
		JsonElement element = new JsonPrimitive("invalid");
		JsonElement result = convertElement(element, ConversionMode.SKIP);
		assertEquals(element, result);
	}
}

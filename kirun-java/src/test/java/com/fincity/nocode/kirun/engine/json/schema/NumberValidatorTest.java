package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertThrows;

import org.json.JSONStringer;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.validator.NumberValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidationException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class NumberValidatorTest {

	@Test
	void NumberValidatorValidateTestForNull() {

		SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
				() -> NumberValidator.validate(null, null, null, null));

		assertEquals("Expected a number but found null", schemaValidationException.getMessage());
	}

	@Test
	void NumberValidatorValidateTestForIsNumber() {

		JsonObject element = new JsonObject();
		element.addProperty("value", "123");

		SchemaType type = SchemaType.INTEGER;

		Schema schema = new Schema();
		
		SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
				() -> NumberValidator.validate(type, null, schema, element));

		assertEquals(element.toString() + " is not a " + type.getPrintableName(),
				schemaValidationException.getMessage());
	}

	@Test
	void NumberValidatorValidateTestForRangeCheckMinimum() {

		JsonObject element = new JsonObject();
		element.addProperty("value", 123);

		SchemaType type = SchemaType.INTEGER;

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.INTEGER));
		schema.setMinimum(124);

		SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
				() -> NumberValidator.validate(type, null, schema, element.get("value")));

		assertEquals(element.get("value").toString() + " should be greater than or equal to " + schema.getMinimum(),
				schemaValidationException.getMessage());
	}
	
	@Test
	void NumberValidatorValidateTestForRangeCheckMaximum() {
		
		JsonObject element = new JsonObject();
		element.addProperty("value", 129);
				
		SchemaType type = SchemaType.INTEGER;

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.INTEGER));
		schema.setMaximum(124);

		SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
				() -> NumberValidator.validate(type, null, schema, element.get("value")));

		assertEquals(element.get("value").toString() + " should be less than or equal to " + schema.getMaximum(),
				schemaValidationException.getMessage());
	}
	
	@Test
	void NumberValidatorValidateTestForMultipleCheck() {

		JsonObject element = new JsonObject();
		element.addProperty("value", 154774374);

		SchemaType type = SchemaType.LONG;

		Schema schema = new Schema();
		Long value = (long) 7738718;
		schema.setType(Type.of(SchemaType.LONG));
		schema.setMultipleOf(value);

		SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
				() -> NumberValidator.validate(type, null, schema, element.get("value")));

		assertEquals(element.get("value").toString() + " is not multiple of " + schema.getMultipleOf(),
				schemaValidationException.getMessage());
	}
}

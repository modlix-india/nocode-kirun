package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;


import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.string.StringFormat;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidationException;
import com.fincity.nocode.kirun.engine.json.schema.validator.StringValidator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class StringValidatorTest {
	
	@Test
	public void StringValidatorTestForValidation() {
		Schema schema = new Schema();
		JsonElement element = null;
		
		//Null Check
		SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
				() -> StringValidator.validate(null, schema, element));

		assertEquals("Expected a string but found null", schemaValidationException.getMessage());
	}
	
	@Test
	public void StringValidatorTestForValidationIfString() {
		
		//Is not Json primitive
		Schema schema = new Schema();
		JsonObject elementJsonPrimitive = new JsonObject();
		elementJsonPrimitive.addProperty("value", 123);
		
		SchemaValidationException schemaValidationExceptionForJsonPrimitive = assertThrows(SchemaValidationException.class,
				() -> StringValidator.validate(null, schema, elementJsonPrimitive));

		assertEquals(elementJsonPrimitive.toString() + " is not String", schemaValidationExceptionForJsonPrimitive.getMessage());
		
	}
	
	@Test
	public void StringValidatorTestForValidationIfTimePatternMatched() {
		
		//String format is Time
		Schema schema = new Schema();
		schema.setFormat(StringFormat.TIME);
		
		JsonObject formatElement = new JsonObject();
		formatElement.addProperty("value", "10-Dec-198 10:19:59");
		
		SchemaValidationException schemaValidationExceptionTimeFormat = assertThrows(SchemaValidationException.class,
				() -> StringValidator.validate(null, schema, formatElement.get("value")));

		assertEquals(formatElement.get("value").toString() + " is not matched with the " + "time pattern", schemaValidationExceptionTimeFormat.getMessage());
		
	}
	
	@Test
	public void StringValidatorTestForValidationIfDatePatternMatched() {
		
		//String format is Time
		Schema schema = new Schema();
		schema.setFormat(StringFormat.DATE);
		
		JsonObject formatElement = new JsonObject();
		formatElement.addProperty("value", "1998-20-12");
		
		SchemaValidationException schemaValidationExceptionTimeFormat = assertThrows(SchemaValidationException.class,
				() -> StringValidator.validate(null, schema, formatElement.get("value")));

		assertEquals(formatElement.get("value").toString() + " is not matched with the " + "date pattern", schemaValidationExceptionTimeFormat.getMessage());
		
	}
	
	@Test
	public void StringValidatorTestForValidationIfDateTimePatternMatched() {
		
		//String format is Time
		Schema schema = new Schema();
		schema.setFormat(StringFormat.DATETIME);
		
		JsonObject formatElement = new JsonObject();
		formatElement.addProperty("value", "2018-12-25 23:50:55.999");
		
		SchemaValidationException schemaValidationExceptionTimeFormat = assertThrows(SchemaValidationException.class,
				() -> StringValidator.validate(null, schema, formatElement.get("value")));

		assertEquals(formatElement.get("value").toString() + " is not matched with the " + "date time pattern", schemaValidationExceptionTimeFormat.getMessage());
		
	}
	
	

}

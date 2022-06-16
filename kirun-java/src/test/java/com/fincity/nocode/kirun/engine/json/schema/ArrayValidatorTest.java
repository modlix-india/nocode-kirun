package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.validator.ArrayValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidationException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ArrayValidatorTest {
	Schema schema = new Schema();
	
	@Test
	void ArrayValidatorValidateTestForNull() {
		
		JsonArray element = null;
		Schema schema = new Schema();
		
		SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
				() -> ArrayValidator.validate(null, schema, null, element));

		assertEquals("Expected an array but found null", schemaValidationException.getMessage());
	}
	
	@Test
	void booleanValidatorValidateTestForIsJsonArray() {
		
		JsonObject element = new JsonObject();
		element.addProperty("value", Boolean.FALSE);
		
		Schema schema = new Schema();
		
		SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
				() -> ArrayValidator.validate(null, schema, null,element));

		assertEquals(element.toString() + " is not an Array" ,schemaValidationException.getMessage());
	}
	
	@Test
	void ArrayValidatorValidateTestForMinItems() {
		
		JsonArray element = new JsonArray();
		
		element.add("chile");
		element.add(16);
		element.add("San Francisco");
		element.add(800.0);
		
		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setUniqueItems(Boolean.TRUE);
		schema.setMaxItems(3);
		schema.setMinItems(5);
	
		
		SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
				() -> ArrayValidator.validate(null, schema, null, element));

		assertEquals("Array should have minimum of " + schema.getMinItems() + " elements",schemaValidationException.getMessage());
		
	}
	
	@Test
	void ArrayValidatorValidateTestForMaxItems() {
		
		JsonArray element = new JsonArray();
		
		element.add("chile");
		element.add(16);
		element.add("San Francisco");
		element.add(800.0);
		
		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setUniqueItems(Boolean.TRUE);
		schema.setMaxItems(3);
		schema.setMinItems(2);
	
		
		SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
				() -> ArrayValidator.validate(null, schema, null, element));

		assertEquals("Array can have  maximum of " + schema.getMaxItems() + " elements",schemaValidationException.getMessage());
		
	}
	
	@Test
	void ArrayValidatorValidateTestForUniqueItems() {
		
		JsonArray element = new JsonArray();
		
		element.add("chile");
		element.add(16);
		element.add("San Francisco");
		element.add(16);
		
		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setUniqueItems(Boolean.TRUE);
		schema.setMaxItems(5);
		schema.setMinItems(2);
		
		SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
				() -> ArrayValidator.validate(null, schema, null, element));

		assertEquals("Items on the array are not unique",schemaValidationException.getMessage());
		
	}

}

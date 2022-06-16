package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.validator.NumberValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidationException;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SchemaValidatorTest{
	
		@Test
		void schemaValidatortestForNullSchema() {
			
			Schema schema = new Schema();
			
			JsonElement element = new JsonObject();
			
			assertEquals(SchemaValidator.validate(null, schema, null,  element), element);
			
	}
		
		@Test
		void schemaValidatortestForNullElement() {
			JsonObject defaultValue = new JsonObject();
			defaultValue.addProperty("value", 123);
			
			Schema schema = new Schema();
			schema.setType(Type.of(SchemaType.ARRAY));
			schema.setDefaultValue(defaultValue);
			
			JsonElement element = null;
					
			assertEquals(SchemaValidator.validate(null, schema, null,  element), schema.getDefaultValue());
			
	}
		
		@Test
		void schemaValidatortestIfConstantNotEqualsElement() {
			JsonObject defaultValue = new JsonObject();
			defaultValue.addProperty("value", 123);
			
			JsonObject constantElement = new JsonObject();
			constantElement.addProperty("value", "constant");
			
			Schema schema = new Schema();
			schema.setType(Type.of(SchemaType.ARRAY));
			schema.setDefaultValue(defaultValue);
			schema.setConstant(constantElement);
			
			JsonElement element = new JsonObject();
			
			SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
					() -> SchemaValidator.validate(null, schema, null, element));

			assertEquals("Expecting a constant value : " + element, schemaValidationException.getMessage());
					
			
	}
		
		@Test
		void schemaValidatortestIfConstantEqualsElement() {
			JsonObject defaultValue = new JsonObject();
			defaultValue.addProperty("value", 123);
			
			JsonObject element = new JsonObject();
			element.addProperty("value", "constant");
			
			Schema schema = new Schema();
			schema.setType(Type.of(SchemaType.ARRAY));
			schema.setDefaultValue(defaultValue);
			schema.setConstant(element);
			
			assertEquals(SchemaValidator.validate(null, schema, null,  element), element);
					
	}
		
		@Test 
		void schemaValidatortestForEnmumCheck() {
			
			JsonObject defaultValue = new JsonObject(); 
			defaultValue.addProperty("value", 123);
			
			JsonObject element = new JsonObject();
			element.addProperty("value", "constant");
			
			List<JsonElement> enums = new ArrayList<JsonElement>();
			enums.add(defaultValue);
			enums.add(element);
			
			Schema schema = new Schema(); 
			schema.setType(Type.of(SchemaType.ARRAY)); 
			schema.setDefaultValue(defaultValue);
			schema.setConstant(element); 
			schema.setEnums(enums);
			
			assertEquals(SchemaValidator.validate(null, schema, null,  element), element); }
		
		
		@Test 
		void schemaValidatorIntegratedTest() {
			
			JsonObject defaultValue = new JsonObject(); 
			defaultValue.addProperty("value", 123);
			
			JsonObject element = new JsonObject();
			element.addProperty("value", "constant");
			
			List<JsonElement> enums = new ArrayList<JsonElement>();
			enums.add(defaultValue);
			enums.add(element);
			
			Schema schema = new Schema(); 
			schema.setType(Type.of(SchemaType.ARRAY)); 
			schema.setDefaultValue(defaultValue);
			schema.setConstant(element);
			schema.setEnums(enums);
			schema.setRef("test_ref");
			schema.setNot(schema);
						
			assertEquals(SchemaValidator.validate(null, schema, null,  element), element); }
		
}
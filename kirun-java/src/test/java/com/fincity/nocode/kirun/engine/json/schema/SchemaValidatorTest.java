package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.HybridRepository;
import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalPropertiesType;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class SchemaValidatorTest {

	@Test
	void schemaValidatortestForNullElement() {
		JsonObject defaultValue = new JsonObject();
		defaultValue.addProperty("value", 123);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setDefaultValue(defaultValue);

		JsonElement element = null;

		assertEquals(SchemaValidator.validate(null, schema, null, element), schema.getDefaultValue());

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

		assertEquals("null - Expecting a constant value : " + element, schemaValidationException.getMessage());

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

		assertEquals(SchemaValidator.validate(null, schema, null, element), element);

	}

	@Test
	void schemaValidatorTestForRefOfRef() {
		var locationMap = new HashMap<String, Schema>();
		var schemaMap = new HashMap<String, Schema>();
		locationMap.put("url", Schema.ofString("url"));
		var locationSchema = Schema.ofObject("Location")
		        .setNamespace("Test")
		        .setProperties(locationMap);
		var urlParamsSchema = Schema.ofObject("UrlParameters")
		        .setNamespace("Test")
		        .setAdditionalProperties(new AdditionalPropertiesType().setSchemaValue(Schema.ofRef("Test.Location")));
		var testSchema = Schema.ofObject("TestSchema")
		        .setNamespace("Test")
		        .setAdditionalProperties(
		                new AdditionalPropertiesType().setSchemaValue(Schema.ofRef("Test.UrlParameters")));
		schemaMap.put("Location", locationSchema);
		schemaMap.put("UrlParameters", urlParamsSchema);
		schemaMap.put("TestSchema", testSchema);
		class TestRepository implements Repository<Schema> {

			@Override
			public Schema find(String namespace, String name) {
				if (namespace == null) {
					return null;
				}
				return schemaMap.get(name);
			}

			@Override
			public List<String> filter(String name) {

				return schemaMap.values()
				        .stream()
				        .map(Schema::getFullName)
				        .filter(e -> e.toLowerCase()
				                .contains(name.toLowerCase()))
				        .toList();
			}
		}
		var repo = new HybridRepository<Schema>(new TestRepository(), new KIRunSchemaRepository());
		var urlParams = new JsonObject();
		var testValue = new JsonObject();
		var location = new JsonObject();
		location.addProperty("url", "http://test/");
		urlParams.add("obj", location);
		testValue.add("obj", urlParams);

		assertEquals(SchemaValidator.validate(null, Schema.ofRef("Test.TestSchema"), repo, testValue), testValue);

	}

	@Test
	void schemaValidatorTestForEnumCheck() {

		JsonObject defaultValue = new JsonObject();
		defaultValue.addProperty("value", 123);

		JsonArray element = new JsonArray();
		element.add(1);
		element.add(2);

		List<JsonElement> enums = new ArrayList<JsonElement>();
		enums.add(defaultValue);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setDefaultValue(defaultValue);
		schema.setEnums(enums);

		schema.setRef("test_ref");
		schema.setNot(schema);

		SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
		        () -> SchemaValidator.validate(null, schema, null, element));

		assertEquals("null - Value is not one of " + schema.getEnums(), schemaValidationException.getMessage());

		// For positive case
		enums.add(element);
		schema.setEnums(enums);
		assertEquals(SchemaValidator.validate(null, schema, null, element), element);
	}

	@Test
	void schemaValidatorIfGetNot() {

		JsonObject defaultValue = new JsonObject();
		defaultValue.addProperty("value", 123);

		JsonArray element = new JsonArray();
		element.add(1);
		element.add(2);

		Schema schema = new Schema();
		schema.setDefaultValue(defaultValue);

		Schema setNotSchema = new Schema();
		schema.setNot(setNotSchema);

		SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
		        () -> SchemaValidator.validate(null, schema, null, element));
		assertEquals("null.null - Schema validated value in not condition.", schemaValidationException.getMessage());

	}

	@Test
	void schemaValidatorForTypeValidation() {

		JsonObject defaultValue = new JsonObject();
		defaultValue.addProperty("value", 123);

		JsonArray element = new JsonArray();
		element.add(1);
		element.add(2);

		Schema schema = new Schema();
		schema.setDefaultValue(defaultValue);
		schema.setType(Type.of(SchemaType.ARRAY));

		assertEquals(SchemaValidator.validate(null, schema, null, element), element);

		schema.setType(Type.of(SchemaType.NULL));

		assertThrows(SchemaValidationException.class, () -> SchemaValidator.validate(null, schema, null, element));

	}

	@Test
	void schemaValidatorForObjectWhenTypeMissing() {

		JsonObject defaultValue = new JsonObject();
		defaultValue.addProperty("value", 123);

		Schema schema = Schema.ofObject("testSchema")
		        .setProperties(Map.of("intType", new Schema()));

		assertEquals(defaultValue, SchemaValidator.validate(null, schema, null, defaultValue));
	}

	@Test
	void schemaValidatorForStringWhenTypeMissing() {

		JsonObject defaultValue = new JsonObject();
		defaultValue.addProperty("value", "surendhar.s");

		Schema schema = Schema.ofObject("testSchema")
		        .setProperties(Map.of("stringType", new Schema()));

		assertEquals(defaultValue, SchemaValidator.validate(null, schema, null, defaultValue));
	}
}
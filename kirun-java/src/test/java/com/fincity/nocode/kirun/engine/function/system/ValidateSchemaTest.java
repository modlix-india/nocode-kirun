package com.fincity.nocode.kirun.engine.function.system;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ValidateSchemaTest {

	@Test
	void testBooleanArrayConversion() {

		var json = """
				{
				    "name": "lead",
				    "type": ["OBJECT"],
				    "version": 1,
				    "properties": {
				        "name": {
				            "type": ["STRING"],
				            "minLength": 3
				        },
				        "mobileNumber": {
				            "type": ["STRING"],
				            "minLength": 3
				        },
				        "formType": {
				            "type": ["STRING"],
				            "enums": ["LEAD_FORM", "CONTACT_FORM"]
				        },
				        "email": {
				            "type": ["STRING"]
				        },
				        "address": {
				            "type": ["OBJECT"],
				            "properties": {
				                "street": {
				                    "type": ["STRING"],
				                    "minLength": 5
				                },
				                "city": {
				                    "type": ["STRING"]
				                },
				                "state": {
				                    "type": ["STRING"]
				                },
				                "postalCode": {
				                    "type": ["STRING"],
				                    "pattern": "^[0-9]{6}$"
				                }
				            },
				            "required": ["street", "city", "state"]
				        },
				        "employment": {
				            "type": ["OBJECT"],
				            "properties": {
				                "company": {
				                    "type": ["STRING"]
				                },
				                "position": {
				                    "type": ["STRING"]
				                },
				                "experience": {
				                    "type": ["INTEGER"],
				                    "minimum": 0
				                },
				                "skills": {
				                    "type": ["ARRAY"],
				                    "items": {
				                        "type": ["STRING"]
				                    },
				                    "minItems": 1
				                }
				            },
				            "required": ["company", "position"]
				        }
				    },
				    "required": ["name", "email", "formType"]
				}
				""";

		AdditionalType.AdditionalTypeAdapter additional = new AdditionalType.AdditionalTypeAdapter();
		ArraySchemaType.ArraySchemaTypeAdapter arraySchema = new ArraySchemaType.ArraySchemaTypeAdapter();

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new Type.SchemaTypeAdapter())
				.registerTypeAdapter(AdditionalType.class, additional)
				.registerTypeAdapter(ArraySchemaType.class, arraySchema)
				.create();

		additional.setGson(gson);
		arraySchema.setGson(gson);

		JsonObject schemaObject = gson.fromJson(json, JsonObject.class);

		JsonObject source = new JsonObject();

		source.addProperty("name", "John Doe");
		source.addProperty("mobileNumber", "9876543210");
		source.addProperty("formType", "LEAD_FORM");
		source.addProperty("email", "john.doe@example.com");

		JsonObject address = new JsonObject();
		address.addProperty("street", "123 Main Street");
		address.addProperty("city", "New York");
		address.addProperty("state", "NY");
		address.addProperty("postalCode", "100001");
		source.add("address", address);

		JsonObject employment = new JsonObject();
		employment.addProperty("company", "Tech Corp");
		employment.addProperty("position", "Senior Developer");
		employment.addProperty("experience", 5);

		JsonArray skills = new JsonArray();
		skills.add("Java");
		skills.add("Spring");
		skills.add("React");
		employment.add("skills", skills);

		source.add("employment", employment);

		assertValidation(schemaObject, source, true);
	}

	@Test
	void testSimpleStringValidation() {
		var json = """
				{
				    "type": ["STRING"],
				    "minLength": 3,
				    "maxLength": 10
				}
				""";

		JsonObject schemaObject = createSchemaObject(json);
		JsonPrimitive source = new JsonPrimitive("Hello");

		assertValidation(schemaObject, source, true);
	}

	@Test
	void testNumberValidation() {
		var json = """
				{
				    "type": ["INTEGER"],
				    "minimum": 0,
				    "maximum": 100
				}
				""";

		JsonObject schemaObject = createSchemaObject(json);
		JsonPrimitive source = new JsonPrimitive(50);

		assertValidation(schemaObject, source, true);
	}

	@Test
	void testSimpleArrayValidation() {
		var json = """
				{
				    "type": ["ARRAY"],
				    "items": {
				        "type": ["STRING"]
				    },
				    "minItems": 1,
				    "maxItems": 3
				}
				""";

		JsonObject schemaObject = createSchemaObject(json);
		JsonArray source = new JsonArray();
		source.add("item1");
		source.add("item2");

		assertValidation(schemaObject, source, true);
	}

	@Test
	void testValidationFailure() {
		var json = """
				{
				    "type": ["STRING"],
				    "minLength": 5
				}
				""";

		JsonObject schemaObject = createSchemaObject(json);
		JsonPrimitive source = new JsonPrimitive("Hi");

		assertValidation(schemaObject, source, false);
	}

	private JsonObject createSchemaObject(String json) {
		AdditionalType.AdditionalTypeAdapter additional = new AdditionalType.AdditionalTypeAdapter();
		ArraySchemaType.ArraySchemaTypeAdapter arraySchema = new ArraySchemaType.ArraySchemaTypeAdapter();

		Gson gson = new GsonBuilder()
				.registerTypeAdapter(Type.class, new Type.SchemaTypeAdapter())
				.registerTypeAdapter(AdditionalType.class, additional)
				.registerTypeAdapter(ArraySchemaType.class, arraySchema)
				.create();

		additional.setGson(gson);
		arraySchema.setGson(gson);

		return gson.fromJson(json, JsonObject.class);
	}

	private void assertValidation(JsonObject schema, JsonElement source, boolean expectedResult) {
		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(
						"source", source,
						"schema", schema))
				.setContext(Map.of())
				.setSteps(Map.of());

		ValidateSchema validator = new ValidateSchema();

		StepVerifier.create(validator.execute(fep).map(e -> e.next().getResult().get("isValid")))
				.expectNext(new JsonPrimitive(expectedResult))
				.verifyComplete();
	}
}

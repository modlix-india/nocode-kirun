package com.fincity.nocode.kirun.engine.json.schema.reactive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.SchemaDetails;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.fincity.nocode.kirun.engine.reactive.ReactiveHybridRepository;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReactiveSchemaValidatorTest {

	@Test
	void schemaValidatortestForNullElement() {
		JsonObject defaultValue = new JsonObject();
		defaultValue.addProperty("value", 123);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setDefaultValue(defaultValue);

		JsonElement element = null;

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, element))
		        .expectNext(schema.getDefaultValue())
		        .verifyComplete();

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

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, element))
		        .verifyErrorMessage("Expecting a constant value : {}");

	}

	@Test
	void schemaValidatortestIfConstantEqualsElement() {
//		JsonObject defaultValue = new JsonObject();
//		defaultValue.addProperty("value", 123);
//
//		JsonObject element = new JsonObject();
//		element.addProperty("value", "constant");
//
//		Schema schema = new Schema();
//		schema.setType(Type.of(SchemaType.ARRAY));
//		schema.setDefaultValue(defaultValue);
//		schema.setConstant(element);
//
//		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, element))
//		        .expectNext(schema.getDefaultValue())
//		        .verifyComplete();
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
		        .setAdditionalProperties(new AdditionalType().setSchemaValue(Schema.ofRef("Test.Location")));
		var testSchema = Schema.ofObject("TestSchema")
		        .setNamespace("Test")
		        .setAdditionalProperties(new AdditionalType().setSchemaValue(Schema.ofRef("Test.UrlParameters")));
		schemaMap.put("Location", locationSchema);
		schemaMap.put("UrlParameters", urlParamsSchema);
		schemaMap.put("TestSchema", testSchema);
		class TestRepository implements ReactiveRepository<Schema> {

			@Override
			public Mono<Schema> find(String namespace, String name) {
				if (namespace == null) {
					return Mono.empty();
				}
				return Mono.just(schemaMap.get(name));
			}

			@Override
			public Flux<String> filter(String name) {

				return Flux.fromIterable(schemaMap.values())
				        .map(Schema::getFullName)
				        .filter(e -> e.toLowerCase()
				                .contains(name.toLowerCase()));
			}
		}

		var repo = new ReactiveHybridRepository<>(new TestRepository(), new KIRunReactiveSchemaRepository());
		var urlParams = new JsonObject();
		var testValue = new JsonObject();
		var location = new JsonObject();
		location.addProperty("url", "http://test/");
		urlParams.add("obj", location);
		testValue.add("obj", urlParams);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, Schema.ofRef("Test.TestSchema"), repo, testValue))
		        .expectNext(testValue)
		        .verifyComplete();

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

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, element))
		        .verifyErrorMessage("Value is not one of [{\"value\":123}]");

		// For positive case
		enums.add(element);
		schema.setEnums(enums);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, element))
		        .expectNext(element)
		        .verifyComplete();
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

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, element))
		        .verifyErrorMessage("Schema validated value in not condition.");
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

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, element))
		        .expectNext(element)
		        .verifyComplete();

		schema.setType(Type.of(SchemaType.NULL));

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, element))
		        .verifyErrorMessage("Expected a null but found [1,2]");

	}

	@Test
	void schemaValidatorForObjectWhenTypeMissing() {

		JsonObject defaultValue = new JsonObject();
		defaultValue.addProperty("value", 123);

		Schema schema = Schema.ofObject("testSchema")
		        .setProperties(Map.of("intType", new Schema()));

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, defaultValue))
		        .expectNext(defaultValue)
		        .verifyComplete();

	}

	@Test
	void schemaValidatorForStringWhenTypeMissing() {

		JsonObject defaultValue = new JsonObject();
		defaultValue.addProperty("value", "surendhar.s");

		Schema schema = Schema.ofObject("testSchema")
		        .setProperties(Map.of("stringType", new Schema()));

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, defaultValue))
		        .expectNext(defaultValue)
		        .verifyComplete();
	}

	@Test
	void customMessageTest() {
		Schema schema = Schema.ofObject("testSchema")
				.setProperties(Map.of("stringType", new Schema().setType(Type.of(SchemaType.STRING)).setDetails(new SchemaDetails().setValidationMessages(Map.of(SchemaDetails.MANDATORY, "It is important to provide a value for String Type.")))))
				.setRequired(List.of("stringType"));

		JsonObject defaultValue = new JsonObject();
		defaultValue.addProperty("value", "Somethin");

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, defaultValue))
				.verifyErrorMessage("testSchema - testSchema - It is important to provide a value for String Type.");
	}
}
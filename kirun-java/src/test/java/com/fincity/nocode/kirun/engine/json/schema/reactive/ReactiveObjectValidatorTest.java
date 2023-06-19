package com.fincity.nocode.kirun.engine.json.schema.reactive;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import reactor.test.StepVerifier;

class ReactiveObjectValidatorTest {

	@Test
	void schemaObjectNewFalseBooleanPassTest() {

		Map<String, Schema> props = new HashMap<>();
		props.put("name", Schema.ofString("name"));
		props.put("phone", Schema.ofLong("phone"));
		Schema schema = Schema.ofObject("schema")
				.setProperties(props)
				.setAdditionalProperties(new AdditionalType(false));

		JsonObject job = new JsonObject();
		job.addProperty("name", "surendhar");
		job.addProperty("phone", 13423524);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
				.expectNext(job)
				.verifyComplete();
	}

	@Test
	void schemaObjectNewFalseBooleanFailTest() {

		Map<String, Schema> props = new HashMap<>();
		props.put("name", Schema.ofString("name"));
		props.put("phone", Schema.ofLong("phone"));
		Schema schema = Schema.ofObject("schema")
				.setProperties(props)
				.setAdditionalProperties(new AdditionalType(false));

		JsonObject job = new JsonObject();
		job.addProperty("name", "surendhar");
		job.addProperty("phone", 13423524);
		job.addProperty("age", 12);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
				.expectErrorMessage("schema - Value " + job.toString() + " is not of valid type(s)\n"
						+ "schema - [age] are additional properties which are not allowed.")
				.verify();
	}

	@Test
	void schemaObjectWithNewTrueBooleanPassTest() {

		Map<String, Schema> props = new HashMap<>();
		props.put("name", Schema.ofString("name"));
		props.put("phone", Schema.ofLong("phone"));
		Schema schema = Schema.ofObject("schema")
				.setProperties(props)
				.setAdditionalProperties(new AdditionalType(true));

		JsonObject job = new JsonObject();
		job.addProperty("name", "surendhar");
		job.addProperty("phone", 13423524);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
				.expectNext(job)
				.verifyComplete();
	}

	@Test
	void schemaObjectWithNewTrueBooleanTest() {

		Map<String, Schema> props = new HashMap<>();
		props.put("name", Schema.ofString("name"));
		props.put("phone", Schema.ofLong("phone"));
		Schema schema = Schema.ofObject("schema")
				.setProperties(props)
				.setAdditionalProperties(new AdditionalType(true));

		JsonObject job = new JsonObject();
		job.addProperty("name", "surendhar");
		job.addProperty("phone", 13423524);
		job.addProperty("age", 12);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
				.expectNext(job)
				.verifyComplete();
	}

	@Test
	void schemaObjectOldFalseBooleanPassTest() {

		Map<String, Schema> props = new HashMap<>();
		props.put("name", Schema.ofString("name"));
		props.put("phone", Schema.ofLong("phone"));
		Schema schema = Schema.ofObject("schema")
				.setProperties(props)
				.setAdditionalProperties(new AdditionalType().setBooleanValue(false));

		JsonObject job = new JsonObject();
		job.addProperty("name", "surendhar");
		job.addProperty("phone", 13423524);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
				.expectNext(job)
				.verifyComplete();
	}

	@Test
	void schemaObjectOldFalseBooleanFailTest() {

		Map<String, Schema> props = new HashMap<>();
		props.put("name", Schema.ofString("name"));
		props.put("phone", Schema.ofLong("phone"));
		Schema schema = Schema.ofObject("schema")
				.setProperties(props)
				.setAdditionalProperties(new AdditionalType().setBooleanValue(false));

		JsonObject job = new JsonObject();
		job.addProperty("name", "surendhar");
		job.addProperty("phone", 13423524);
		job.addProperty("age", 12);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
				.expectErrorMessage("schema - Value " + job.toString() + " is not of valid type(s)\n"
						+ "schema - [age] are additional properties which are not allowed.")
				.verify();
	}

	@Test
	void schemaObjectWithOldTrueBooleanPassTest() {

		Map<String, Schema> props = new HashMap<>();
		props.put("name", Schema.ofString("name"));
		props.put("phone", Schema.ofLong("phone"));
		Schema schema = Schema.ofObject("schema")
				.setProperties(props)
				.setAdditionalProperties(new AdditionalType().setBooleanValue(true));

		JsonObject job = new JsonObject();
		job.addProperty("name", "surendhar");
		job.addProperty("phone", 13423524);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
				.expectNext(job)
				.verifyComplete();
	}

	@Test
	void schemaObjectWithOldTrueBooleanTest() {

		Map<String, Schema> props = new HashMap<>();
		props.put("name", Schema.ofString("name"));
		props.put("phone", Schema.ofLong("phone"));
		Schema schema = Schema.ofObject("schema")
				.setProperties(props)
				.setAdditionalProperties(new AdditionalType().setBooleanValue(true));

		JsonObject job = new JsonObject();
		job.addProperty("name", "surendhar");
		job.addProperty("phone", 13423524);
		job.addProperty("age", 12);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
				.expectNext(job)
				.verifyComplete();
	}

	@Test
	void schemaObjectOldSchemaPassTest() {

		Map<String, Schema> props = new HashMap<>();
		props.put("name", Schema.ofString("name"));
		props.put("phone", Schema.ofLong("phone"));

		Schema addSchema = Schema.ofBoolean("addSchema");

		Schema schema = Schema.ofObject("schema")
				.setProperties(props)
				.setAdditionalProperties(new AdditionalType().setSchemaValue(addSchema));

		JsonObject job = new JsonObject();
		job.addProperty("name", "surendhar");
		job.addProperty("phone", 13423524);
		job.addProperty("married?", false);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
				.expectNext(job.deepCopy())
				.verifyComplete();
	}

	@Test
	void schemaObjectOldSchemaFailTest() {

		Map<String, Schema> props = new HashMap<>();
		props.put("name", Schema.ofString("name"));
		props.put("phone", Schema.ofLong("phone"));

		Schema addSchema = Schema.ofBoolean("addSchema");

		Schema schema = Schema.ofObject("schema")
				.setProperties(props)
				.setAdditionalProperties(new AdditionalType().setSchemaValue(addSchema));

		String city = "Yanam";

		JsonObject job = new JsonObject();
		job.addProperty("name", "surendhar");
		job.addProperty("phone", 13423524);
		job.addProperty("married?", false);
		job.addProperty("working", true);
		job.addProperty("city", city);

		ReactiveSchemaValidator.validate(null, schema, null, job)
				.subscribe(System.out::println);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
				.expectErrorMessage("schema - Value " + job.toString() + " is not of valid type(s)\n"
						+ "schema.addSchema - Value \"" + city + "\" is not of valid type(s)\n"
						+ "schema.addSchema - \"" + city + "\" is not a boolean")
				.verify();
	}

	@Test
	void schemaObjectOldSchemaTypePassTest() {

		AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
				.registerTypeAdapter(AdditionalType.class, addType)
				.create();

		addType.setGson(gson);

		Schema schema = gson.fromJson("""
				{"type": "OBJECT",
				"properties": {"name": { "type": "STRING" }},
				"additionalProperties": {"type" : "STRING" }
				}
				""", Schema.class);

		JsonObject job = new JsonObject();
		job.addProperty("name", "surendhar");
		job.addProperty("phone", 13423524);
		job.addProperty("married", false);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
				.expectErrorMessage(
						"Value {\"name\":\"surendhar\",\"phone\":13423524,\"married\":false} is not of valid type(s)\n"
								+ "Value 13423524 is not of valid type(s)\n" + "13423524 is not String")
				.verify();
	}

	@Test
	void schemaObjectOldSchemaValueTypePassTest() {

		AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
				.registerTypeAdapter(AdditionalType.class, addType)
				.create();

		addType.setGson(gson);

		Schema schema = gson.fromJson("""
				{"type": "OBJECT",
				"properties": {"name": { "type": "STRING" }, "phone" : {"type" :"INTEGER" }},
				"additionalProperties": {

				"schemaValue" : {"type" : "LONG" }
				}
				}
				""", Schema.class);

		JsonObject job = new JsonObject();
		job.addProperty("name", "surendhar");
		job.addProperty("phone", 1344);
		job.addProperty("age", 12);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
				.expectNext(job)
				.verifyComplete();
	}

}

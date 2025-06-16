package com.fincity.nocode.kirun.engine.json.schema.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveArrayValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import reactor.test.StepVerifier;

class ReactiveArraySchemaAdapterTypeTest {

	@Test
	void schemaObjectPollutionSchemaValueTypePassTest() {

		AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

		ArraySchemaTypeAdapter asType = new ArraySchemaTypeAdapter();

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
				.registerTypeAdapter(ArraySchemaType.class, asType)
				.registerTypeAdapter(AdditionalType.class, addType)
				.create();
		asType.setGson(gson);
		addType.setGson(gson);

		var schema = gson.fromJson(
				"""
						{"type":"ARRAY","items":{"type":"OBJECT","properties":{"x":{"type":"INTEGER"}}},"defaultValue":[{"x":20},{"x":30}]}
						""",
				Schema.class);

		var xschema = gson.fromJson(
				"""
						{"type":"ARRAY","items":{"type":"OBJECT","properties":{"x":{"type":"INTEGER"},"y":{"type":"STRING","defaultValue":"Kiran"}},"required":["x"]}}
						                        """,
				Schema.class);

		var repo = new KIRunReactiveSchemaRepository();

		JsonArray ja = new JsonArray();
		JsonObject job1 = new JsonObject();
		job1.addProperty("x", 20);
		job1.addProperty("y", "Kiran");

		JsonObject job2 = new JsonObject();
		job2.addProperty("x", 30);
		job2.addProperty("y", "Kiran");

		ja.add(job1);
		ja.add(job2);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, repo, null)
				.flatMap(firstValue -> ReactiveSchemaValidator.validate(null, xschema, repo, firstValue)))
				.expectNext(ja)
				.verifyComplete();

		assertNull(schema.getDefaultValue()
				.getAsJsonArray()
				.get(0)
				.getAsJsonObject()
				.get("y"));
	}

	@Test
	void schemaArrayWithSingleTest() {

		ArraySchemaTypeAdapter asta = new ArraySchemaTypeAdapter();
		SchemaTypeAdapter sta = new SchemaTypeAdapter();
		AdditionalTypeAdapter ata = new AdditionalTypeAdapter();

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, sta)
				.registerTypeAdapter(ArraySchemaType.class, asta)
				.registerTypeAdapter(AdditionalType.class, ata)
				.create();

		asta.setGson(gson);
		ata.setGson(gson);

		var array = gson.fromJson(
				"""
						{"type":"ARRAY","items":{"singleSchema":{"type":"OBJECT","properties":{"name":{"type":"STRING"},"age":{"type":"INTEGER"}}}},"additionalItems":true}
						""",
				Schema.class);

		JsonObject obj1 = new JsonObject();
		obj1.addProperty("name", "amigo1");
		JsonObject obj2 = new JsonObject();
		obj2.addProperty("age", 24);
		JsonArray arr = new JsonArray();
		arr.add(obj1);
		arr.add(obj2);
		arr.add(false);
		arr.add("string example");

		StepVerifier.create(ReactiveArrayValidator.validate(null, array, null, arr))
				.expectErrorMessage("false is not an Object")
				.verify();
	}

	@Test
	void schemaArrayWithoutSingleTest() {

		ArraySchemaTypeAdapter asta = new ArraySchemaTypeAdapter();
		SchemaTypeAdapter sta = new SchemaTypeAdapter();
		AdditionalTypeAdapter ata = new AdditionalTypeAdapter();

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, sta)
				.registerTypeAdapter(ArraySchemaType.class, asta)
				.registerTypeAdapter(AdditionalType.class, ata)
				.create();

		asta.setGson(gson);
		ata.setGson(gson);

		var array = gson.fromJson(
				"""
						{"type":"ARRAY","items":{"type":"OBJECT","properties":{"name":{"type":"STRING"},"age":{"type":"INTEGER"}}, "required":["name"]}}
						""",
				Schema.class);

		JsonObject obj1 = new JsonObject();
		obj1.addProperty("name", "amigo1");
		obj1.addProperty("age", 21);
		JsonObject obj2 = new JsonObject();
		obj2.addProperty("age", 24);
		JsonArray arr = new JsonArray();
		arr.add(obj1);
		arr.add(obj2);

		StepVerifier.create(ReactiveArrayValidator.validate(null, array, null, arr))
				.expectErrorMessage("name is mandatory")
				.verify();
	}

	@Test
	void schemaArrayWithTupleTest() {

		ArraySchemaTypeAdapter asta = new ArraySchemaTypeAdapter();
		SchemaTypeAdapter sta = new SchemaTypeAdapter();
		AdditionalTypeAdapter ata = new AdditionalTypeAdapter();

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, sta)
				.registerTypeAdapter(ArraySchemaType.class, asta)
				.registerTypeAdapter(AdditionalType.class, ata)
				.create();

		asta.setGson(gson);
		ata.setGson(gson);

		var array = gson.fromJson(
				"""
						{"type":"ARRAY","items":{"tupleSchema":[{"type":"OBJECT","properties":{"name":{"type":"STRING"},"age":{"type":"INTEGER"}}, "required":["age"]},{"type":"STRING","minLength":2},{"type":"INTEGER","minimum":10}]},"additionalItems":true}
						  """,
				Schema.class);

		JsonObject obj1 = new JsonObject();
		obj1.addProperty("name", "amigo1");
		obj1.addProperty("age", 24);
		JsonArray arr = new JsonArray();
		arr.add(obj1);
		arr.add("string example");
		arr.add(11);
		arr.add(false);
		arr.add(12.44);
		arr.add("mla");
		StepVerifier.create(ReactiveSchemaValidator.validate(null, array, null, arr))
				.expectNext(arr)
				.verifyComplete();
	}

	@Test
	void schemaArrayWithoutTupleTest() {

		ArraySchemaTypeAdapter asta = new ArraySchemaTypeAdapter();
		SchemaTypeAdapter sta = new SchemaTypeAdapter();
		AdditionalTypeAdapter ata = new AdditionalTypeAdapter();

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, sta)
				.registerTypeAdapter(ArraySchemaType.class, asta)
				.registerTypeAdapter(AdditionalType.class, ata)
				.create();

		asta.setGson(gson);
		ata.setGson(gson);

		var arraySchema = gson.fromJson(
				"""
						{ "type": "ARRAY", "items": [{ "type": "OBJECT", "properties": { "name": { "type": "STRING" }, "age": { "type": "INTEGER" } }, "required": ["age"] }, { "type": "STRING", "minLength": 2 }, { "type": "ARRAY", "items": { "type": "INTEGER" }, "additionalItems": false }], "additionalItems": true }
						     """,
				Schema.class);

		JsonObject obj1 = new JsonObject();
		obj1.addProperty("name", "amigo1");
		obj1.addProperty("age", 21);

		JsonArray arr = new JsonArray();
		arr.add(obj1);
		arr.add("secondstring");
		JsonArray secArr = new JsonArray();
		secArr.add(1);
		secArr.add(10000);
		arr.add(secArr);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, arraySchema, null, arr))
				.expectNext(arr)
				.verifyComplete();
	}

	@Test
	void regularJSONToSchema() {

		ArraySchemaTypeAdapter asta = new ArraySchemaTypeAdapter();
		SchemaTypeAdapter sta = new SchemaTypeAdapter();
		AdditionalTypeAdapter ata = new AdditionalTypeAdapter();

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, sta)
				.registerTypeAdapter(ArraySchemaType.class, asta)
				.registerTypeAdapter(AdditionalType.class, ata)
				.create();

		asta.setGson(gson);
		ata.setGson(gson);

		var jsonSchema = """
					{
					"type": "object",
					"properties": {
					  "productId": {
						"description": "The unique identifier for a product",
						"type": "integer"
					  }
					}
				}
						""";

		var schema = gson.fromJson(jsonSchema, Schema.class);

		assertTrue(schema.getType().contains(SchemaType.OBJECT));
		assertFalse(schema.getType().contains(SchemaType.ARRAY));
		assertTrue(schema.getProperties()
				.get("productId").getType().contains(SchemaType.INTEGER));
	}

	@Test
	void onlyRefTest() {

		ArraySchemaTypeAdapter asta = new ArraySchemaTypeAdapter();
		SchemaTypeAdapter sta = new SchemaTypeAdapter();
		AdditionalTypeAdapter ata = new AdditionalTypeAdapter();

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, sta)
				.registerTypeAdapter(ArraySchemaType.class, asta)
				.registerTypeAdapter(AdditionalType.class, ata)
				.create();

		asta.setGson(gson);
		ata.setGson(gson);

		var jsonSchema = """
					{
					"ref": "System.any"
				}
						""";

		var schema = gson.fromJson(jsonSchema, Schema.class);
		assertNull(schema.getType());
	}
}

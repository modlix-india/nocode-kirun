package com.fincity.nocode.kirun.engine.json.schema.reactive;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveArrayValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import reactor.test.StepVerifier;

class ReactiveArraySchemaTypeValidatorTest {

	@Test
	void schemaArrayWithSingleTypeTest() {

		ArraySchemaType ast = new ArraySchemaType().setSingleSchema(Schema.ofInteger("item"));

		Schema schema = Schema.ofArray("schema")
		        .setItems(ast);
		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add(2);
		arr.add(3);
		arr.add(4);
		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, arr))
		        .expectNext(arr)
		        .verifyComplete();
	}

	@Test
	void schemaArrayWithTupleTypeTest() {

		List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

		ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

		Schema schema = Schema.ofArray("schema")
		        .setItems(ast);
		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add("surendhar");
		arr.add(false);
		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, arr))
		        .expectNext(arr)
		        .verifyComplete();
	}

	@Test
	void schemaArrayWithSingleTypeWithAdditionalFalseNewTest() {

		ArraySchemaType ast = new ArraySchemaType().setSingleSchema(Schema.ofInteger("item"));

		Schema schema = Schema.ofArray("schema")
		        .setItems(ast)
		        .setAdditionalItems(new AdditionalType(false));
		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add(2);
		arr.add(3);
		arr.add(4);
		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, arr))
		        .expectNext(arr)
		        .verifyComplete();
	}

	@Test
	void schemaArrayWithSingleTypeWithAdditionalTrueNewTest() {

		ArraySchemaType ast = new ArraySchemaType().setSingleSchema(Schema.ofInteger("item"));

		Schema schema = Schema.ofArray("schema")
		        .setItems(ast)
		        .setAdditionalItems(new AdditionalType(false));
		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add(2);
		arr.add(3);
		arr.add(4);
		arr.add("surendhar");

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, arr))
		        .expectErrorMessage("schema - Value [1,2,3,4,\"surendhar\"] is not of valid type(s)\n"
		                + "schema.item - Value \"surendhar\" is not of valid type(s)\n"
		                + "schema.item - \"surendhar\" is not a Integer")
		        .verify();

	}

	@Test
	void schemaArrayWithSingleTypeWithAdditionalFalseOldTest() {

		ArraySchemaType ast = new ArraySchemaType().setSingleSchema(Schema.ofInteger("item"));

		Schema schema = Schema.ofArray("schema")
		        .setItems(ast)
		        .setAdditionalItems(new AdditionalType().setBooleanValue(true));
		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add(2);
		arr.add(3);
		arr.add(4);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, arr))
		        .expectNext(arr)
		        .verifyComplete();
	}

	@Test
	void schemaArrayWithSingleTypeWithAdditionalTrueOldTest() {

		ArraySchemaType ast = new ArraySchemaType().setSingleSchema(Schema.ofInteger("item"));

		Schema schema = Schema.ofArray("schema")
		        .setItems(ast)
		        .setAdditionalItems(new AdditionalType().setBooleanValue(true));
		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add(2);
		arr.add(3);
		arr.add(4);
		arr.add("surendhar");

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, arr))
		        .expectErrorMessage("schema - Value [1,2,3,4,\"surendhar\"] is not of valid type(s)\n"
		                + "schema.item - Value \"surendhar\" is not of valid type(s)\n"
		                + "schema.item - \"surendhar\" is not a Integer")
		        .verify();
	}

	@Test
	void schemaArrayWithTupleTypeWithAdditionalFalseNewTest() {

		List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

		ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

		Schema schema = Schema.ofArray("schema")
		        .setItems(ast)
		        .setAdditionalItems(new AdditionalType(false));

		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add("surendhar");
		arr.add(false);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, arr))
		        .expectNext(arr)
		        .verifyComplete();
	}

	@Test
	void schemaArrayWithTupleTypeWithAdditionalFalseOldTest() {

		List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

		ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

		Schema schema = Schema.ofArray("schema")
		        .setItems(ast)
		        .setAdditionalItems(new AdditionalType().setBooleanValue(false));

		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add("surendhar");
		arr.add(false);
		arr.add("asd");

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, arr))
		        .expectErrorMessage("schema - Value [1,\"surendhar\",false,\"asd\"] is not of valid type(s)\n"
		                + "schema - Expected an array with only 3 but found 4")
		        .verify();
	}

	@Test
	void schemaArrayWithTupleTypeWithAdditionalFalseOldFailTest() {

		List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

		ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

		Schema schema = Schema.ofArray("schema")
		        .setItems(ast)
		        .setAdditionalItems(new AdditionalType().setBooleanValue(false));

		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add("surendhar");
		arr.add(false);
		arr.add("additional");

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, arr))
		        .expectErrorMessage("schema - Value [1,\"surendhar\",false,\"additional\"] is not of valid type(s)\n"
		                + "schema - Expected an array with only 3 but found 4")
		        .verify();
	}

	@Test
	void schemaArrayWithTupleTypeWithAdditionalTrueNewTest() {

		List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

		ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

		Schema schema = Schema.ofArray("schema")
		        .setItems(ast)
		        .setAdditionalItems(new AdditionalType(true));

		JsonObject job = new JsonObject();
		job.addProperty("company", "fincity");

		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add("surendhar");
		arr.add(false);
		arr.add(job);
		arr.add("name");
		arr.add("additional");

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, arr))
		        .expectNext(arr)
		        .verifyComplete();
	}

	@Test
	void schemaArrayWithTupleTypeWithAdditionalTrueOldTest() {

		List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

		ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

		Schema schema = Schema.ofArray("schema")
		        .setItems(ast)
		        .setAdditionalItems(new AdditionalType().setBooleanValue(true));

		JsonObject job = new JsonObject();
		job.addProperty("company", "fincity");

		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add("surendhar");
		arr.add(false);
		arr.add(job);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, arr))
		        .expectNext(arr)
		        .verifyComplete();
	}

	@Test
	void schemaArrayWithSingleTypeWithAdditionalSchemaNewTest() {

		ArraySchemaType ast = new ArraySchemaType().setSingleSchema(Schema.ofInteger("item"));
		Schema addSchema = Schema.ofString("addSchema");

		Schema schema = Schema.ofArray("schema")
		        .setItems(ast)
		        .setAdditionalItems(new AdditionalType().setSchemaValue(addSchema));

		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add(2);
		arr.add(3);
		arr.add(4);
		arr.add("additional");

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, arr))
		        .expectErrorMessage("schema - Value [1,2,3,4,\"additional\"] is not of valid type(s)\n"
		                + "schema.item - Value \"additional\" is not of valid type(s)\n"
		                + "schema.item - \"additional\" is not a Integer")
		        .verify();
	}

	@Test
	void schemaArrayWithTupleTypeWithAdditionalSchemaStringFailTest() {

		List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

		Schema stringSchema = Schema.ofString("stringSchema");

		ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

		Schema schema = Schema.ofArray("schema")
		        .setItems(ast)
		        .setAdditionalItems(new AdditionalType().setSchemaValue(stringSchema));

		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add("surendhar");
		arr.add(false);
		arr.add("additional");
		arr.add(true);
		arr.add(1);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, arr))
		        .expectErrorMessage(
		                "schema - Value [1,\"surendhar\",false,\"additional\",true,1] is not of valid type(s)\nschema.stringSchema - Value true is not of valid type(s)\nschema.stringSchema - true is not String")
		        .verify();

	}

	@Test
	void schemaArrayWithTupleTypeWithAdditionalSchemaPassTest() {

		List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

		ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

		Schema objSchema = Schema.ofObject("objSchema");

		Schema schema = Schema.ofArray("schema")
		        .setItems(ast)
		        .setAdditionalItems(new AdditionalType().setSchemaValue(objSchema));

		JsonObject job = new JsonObject();
		job.addProperty("company", "fincity");

		JsonObject job1 = new JsonObject();
		job.addProperty("area", "indiranagar");

		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add("surendhar");
		arr.add(false);
		arr.add(job);
		arr.add(job1);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, arr))
		        .expectNext(arr)
		        .verifyComplete();
	}

	@Test
	void schemaArrayWithTupleTypeWithAdditionalSchemaFailTest() {

		List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

		ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

		Schema objSchema = Schema.ofObject("objSchema");

		Schema schema = Schema.ofArray("schema")
		        .setItems(ast)
		        .setAdditionalItems(new AdditionalType().setSchemaValue(objSchema));

		JsonObject job = new JsonObject();
		job.addProperty("company", "fincity");

		JsonObject job1 = new JsonObject();
		job.addProperty("area", "indiranagar");

		JsonArray jarr = new JsonArray();
		jarr.add(1);
		jarr.add(2);

		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add("surendhar");
		arr.add(false);
		arr.add(job);
		arr.add(job1);
		arr.add(jarr);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, arr))
		        .expectErrorMessage(
		                "schema - Value [1,\"surendhar\",false,{\"company\":\"fincity\",\"area\":\"indiranagar\"},{},[1,2]] is not of valid type(s)\n"
		                        + "schema.objSchema - Value [1,2] is not of valid type(s)\n"
		                        + "schema.objSchema - [1,2] is not an Object")
		        .verify();
	}

	@Test
	void arrayGsonSchemaTypeAdditionalSingleFalseTest() {

		AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

		Gson gson = new GsonBuilder().setPrettyPrinting()
		        .registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .registerTypeAdapter(AdditionalType.class, new AdditionalTypeAdapter())
		        .create();

		addType.setGson(gson);

		Schema schema = gson.fromJson("""
		        { "type" : "ARRAY",
		           "items": {"singleSchema" : { "type" :"INTEGER"}},
		           "additionalItems" : false
		        }
		           """, Schema.class);

		JsonArray job = new JsonArray();

		job.add(1);
		job.add(1);
		job.add(2);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, job))
		        .expectNext(job)
		        .verifyComplete();
	}

	@Test
	void arrayGsonSchemaTypeAdditionalSingleFalseFailTest() {

		AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

		Gson gson = new GsonBuilder().setPrettyPrinting()
		        .registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .registerTypeAdapter(AdditionalType.class, new AdditionalTypeAdapter())
		        .create();

		addType.setGson(gson);

		Schema schema = gson.fromJson("""
		        { "type" : "ARRAY",
		           "items": {"singleSchema" : { "type" :"INTEGER"}},
		           "additionalItems" : false
		        }
		           """, Schema.class);

		JsonArray job = new JsonArray();

		job.add(1);
		job.add(1);
		job.add(2);
		job.add("name");

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
		        .expectErrorMessage("Value [1,1,2,\"name\"] is not of valid type(s)\n"
		                + "Value \"name\" is not of valid type(s)\n" + "\"name\" is not a Integer")
		        .verify();

	}

	@Test
	void arrayGsonSchemaTypeAdditionalSingleTrueTest() {

		AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

		Gson gson = new GsonBuilder().setPrettyPrinting()
		        .registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .registerTypeAdapter(AdditionalType.class, new AdditionalTypeAdapter())
		        .create();

		addType.setGson(gson);

		Schema schema = gson.fromJson("""
		        { "type" : "ARRAY",
		           "items": {"singleSchema" : { "type" :"INTEGER"}},
		           "additionalItems" : true
		        }
		           """, Schema.class);

		JsonArray job = new JsonArray();

		job.add(1);
		job.add(1);
		job.add(2);
		job.add(true);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
		        .expectErrorMessage("Value [1,1,2,true] is not of valid type(s)\n"
		                + "Value true is not of valid type(s)\n" + "true is not a Integer")
		        .verify();
	}

	@Test
	void arrayGsonSchemaTypeAdditionalTupleSchemaTest() {

		AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

		Gson gson = new GsonBuilder().setPrettyPrinting()
		        .registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .registerTypeAdapter(AdditionalType.class, addType)
		        .create();

		addType.setGson(gson);

		Schema schema = gson.fromJson("""
		        { "type" : "ARRAY",
		           "items": {"tupleSchema" : [ { "type" :"INTEGER"} ,{ "type" :"STRING"}, { "type" :"BOOLEAN"} ] },
		           "additionalItems" : {
		               "type": "OBJECT"
		           }
		        }
		           """, Schema.class);

		JsonArray job = new JsonArray();

		job.add(1);
		job.add("asd");
		job.add(false);
		job.add(2.34);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
		        .expectErrorMessage("Value [1,\"asd\",false,2.34] is not of valid type(s)\n"
		                + "Value 2.34 is not of valid type(s)\n" + "2.34 is not an Object")
		        .verify();
	}

	@Test
	void arrayGsonSchemaTypeAdditionalTupleFalseTest() {

		AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

		Gson gson = new GsonBuilder().setPrettyPrinting()
		        .registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .registerTypeAdapter(AdditionalType.class, addType)
		        .create();

		addType.setGson(gson);

		Schema schema = gson.fromJson("""
		        { "type" : "ARRAY",
		           "items": {"tupleSchema" : [ { "type" :"INTEGER"} ,{ "type" :"STRING"}, { "type" :"BOOLEAN"} ] },
		           "additionalItems" : {

		           "booleanValue" : false
		           }
		        }
		           """, Schema.class);

		JsonArray job = new JsonArray();

		job.add(1);
		job.add("asd");
		job.add(false);
		job.add(2.34);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
		        .expectErrorMessage("Value [1,\"asd\",false,2.34] is not of valid type(s)\n"
		                + "Expected an array with only 3 but found 4")
		        .verify();
	}

	@Test
	void arrayGsonSchemaTypeAdditionalTupleSchemaPassTest() {

		AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

		Gson gson = new GsonBuilder().setPrettyPrinting()
		        .registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .registerTypeAdapter(AdditionalType.class, addType)
		        .create();

		addType.setGson(gson);

		Schema schema = gson.fromJson("""
		        { "type" : "ARRAY",
		           "items": {"tupleSchema" : [ { "type" :"INTEGER"} ,{ "type" :"STRING"}, { "type" :"BOOLEAN"} ] },
		           "additionalItems" : {

		           "schemaValue" : {
		               "type" : "OBJECT"
		           }
		           }
		        }
		           """, Schema.class);

		JsonArray job = new JsonArray();

		job.add(1);
		job.add("asd");
		job.add(false);
		JsonObject jo = new JsonObject();
		jo.addProperty("name", "thinking");
		jo.addProperty("age", 1);
		job.add(jo);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, job))
		        .expectNext(job)
		        .verifyComplete();
	}

	@Test
	void arrayGsonSchemaTypeAdditionalTupleSchemaPaTest() {

		AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

		Gson gson = new GsonBuilder().setPrettyPrinting()
		        .registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .registerTypeAdapter(AdditionalType.class, addType)
		        .create();

		addType.setGson(gson);

		Schema schema = gson.fromJson("""
		        { "type" : "ARRAY",
		           "items": {"tupleSchema" : [ { "type" :"INTEGER"} ,{ "type" :"STRING"}, { "type" :"BOOLEAN"} ] },
		           "additionalItems" : {

		           "schemaValue" : {
		               "type" : "OBJECT"
		           }
		           }
		        }
		           """, Schema.class);

		JsonArray job = new JsonArray();

		job.add(1);
		job.add("asd");
		JsonObject jo = new JsonObject();
		jo.addProperty("name", "thinking");
		jo.addProperty("age", 1);
		job.add(jo);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
		        .expectErrorMessage("Value [1,\"asd\",{\"name\":\"thinking\",\"age\":1}] is not of valid type(s)\n"
		                + "Value {\"name\":\"thinking\",\"age\":1} is not of valid type(s)\n"
		                + "{\"name\":\"thinking\",\"age\":1} is not a boolean")
		        .verify();
	}

	@Test
	void arrayGsonSchemaTypeAdditionalFalseTupleSchemaPaTest() {

		AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

		Gson gson = new GsonBuilder().setPrettyPrinting()
		        .registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .registerTypeAdapter(AdditionalType.class, addType)
		        .create();

		addType.setGson(gson);

		Schema schema = gson.fromJson("""
		        { "type" : "ARRAY",
		           "items": {"tupleSchema" : [ { "type" :"INTEGER"} ,{ "type" :"STRING"}, { "type" :"BOOLEAN"} ] },
		           "additionalItems" : {

		           "booleanValue" :  false
		           }
		        }
		           """, Schema.class);

		JsonArray job = new JsonArray();

		job.add(1);
		job.add("asd");
		job.add(false);
		JsonObject jo = new JsonObject();
		jo.addProperty("name", "thinking");
		jo.addProperty("age", 1);
		job.add(jo);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, job))
		        .expectErrorMessage(
		                "Value [1,\"asd\",false,{\"name\":\"thinking\",\"age\":1}] is not of valid type(s)\n"
		                        + "Expected an array with only 3 but found 4")
		        .verify();
	}

}

package com.fincity.nocode.kirun.engine.json.schema.reactive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.HybridRepository;
import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.fincity.nocode.kirun.engine.reactive.ReactiveHybridRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.ReactiveRepositoryWrapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ReactiveSchemaAnyofValidatorTest {

	private static final Schema CONDITION_FILTER;

	static {
		String schemaString = """
		        	              {
		        "type": "OBJECT",
		        "oneOf": [ {"ref": "#/$defs/filterCondition"}, {"ref": "#/$defs/complexCondition"}],
		        "$defs": {
		        "filterOperator": {
		          "type": "STRING",
		          "defaultValue": "EQUALS",
		          "enums": [
		            "EQUALS",
		            "LESS_THAN",
		            "GREATER_THAN",
		            "LESS_THAN_EQUAL",
		            "GREATER_THAN_EQUAL",
		            "BETWEEN",
		            "IN"
		          ]
		        },
		        "complexOperator": {
		          "type": "STRING",
		          "defaultValue": "EQUALS",
		          "enums": [ "OR", "AND" ]
		        },
		        "filterCondition":{
		        "type": "OBJECT",
		        "properties": {
		          "negate": {"type": "BOOLEAN", "defaultValue": false},
		          "operator": { "ref": "#/$defs/filterOperator" },
		          "field": { "type": "STRING" },
		          "value": { "ref": "System.any" },
		          "toValue": { "ref": "System.any" },
		          "multiValue": { "type": "ARRAY", "items": { "ref": "System.any" } },
		          "isValue": {"type": "BOOLEAN", "defaultValue": true},
		          "isToValue": {"type": "BOOLEAN", "defaultValue": false}
		        },
		        "required": ["operator", "field"],
		        "additonalProperties": false
		        },
		        "complexCondition": {
		        "type": "OBJECT",
		        "properties": {
		          "conditions": {
		            "type": "ARRAY",
		            "items": {
		              "type": "OBJECT",
		              "oneOf": [ {"ref": "#/$defs/filterCondition"}, {"ref": "#/$defs/complexCondition"}]
		            }
		          },
		          "negate": {"type": "BOOLEAN", "defaultValue": false},
		          "operator": { "ref": "#/$defs/complexOperator" }
		        },
		        "required": ["operator", "conditions"],
		        "additonalProperties": false
		        }
		        }
		        }
		        	              """;

		AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

		ArraySchemaTypeAdapter asType = new ArraySchemaTypeAdapter();

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .registerTypeAdapter(ArraySchemaType.class, asType)
		        .registerTypeAdapter(AdditionalType.class, addType)
		        .create();
		asType.setGson(gson);
		addType.setGson(gson);

		CONDITION_FILTER = gson.fromJson(schemaString, Schema.class);
	}

	@Test
	void test() {

		Schema complexOperator = Schema.ofString("complexOperator")
		        .setNamespace("test")
		        .setEnums(List.of(new JsonPrimitive("AND"), new JsonPrimitive("OR")));

		Schema arraySchema = Schema.ofArray("conditions", Schema.ofRef("#"));

		Schema ComplexCondition = Schema.ofObject("ComplexCondition")
		        .setNamespace("test")
		        .setProperties(Map.of("conditions", arraySchema, "negate", Schema.ofBoolean("negate"),
		                "complexConditionOperator", Schema.ofRef("test.complexOperator")));

		var schemaMap = new HashMap<String, Schema>();
		schemaMap.put("complexOperator", complexOperator);
		schemaMap.put("ComplexCondition", ComplexCondition);

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

		var repo = new ReactiveRepositoryWrapper<>(
		        new HybridRepository<>(new TestRepository(), new KIRunSchemaRepository()));

		JsonObject job = new JsonObject();
		JsonArray ja = new JsonArray();
		JsonElement je = new JsonObject();
		var temp = new JsonArray();
		var tempOb = new JsonObject();
		tempOb.addProperty("field", 2);
		tempOb.addProperty("value", "surendhar");
		tempOb.addProperty("operator", "LESS_THAN");
		temp.add(tempOb);
		je.getAsJsonObject()
		        .addProperty("negate", false);
		je.getAsJsonObject()
		        .addProperty("complexConditionOperator", "OR");
		je.getAsJsonObject()
		        .add("conditions", temp);

		ja.add(je);
		job.add("completeConditions", ja);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, ComplexCondition, repo, job))
		        .expectNext(job)
		        .verifyComplete();
	}

	@Test
	void filterConditionTest() {

		Schema filterOperator = Schema.ofString("filterOperator")
		        .setNamespace("test")
		        .setEnums(List.of(new JsonPrimitive("EQUALS"), new JsonPrimitive("LESS_THAN"),
		                new JsonPrimitive("GREATER_THAN"), new JsonPrimitive("LESS_THAN_EQUAL")));

		Schema FilterCondition = Schema.ofObject("FilterCondition")
		        .setNamespace("test")
		        .setProperties(Map.of("negate", Schema.ofBoolean("negate"), "filterConditionOperator",
		                Schema.ofRef("test.filterOperator"), "field", Schema.ofString("field"), "value",
		                Schema.ofAny("value"), "toValue", Schema.ofAny("toValue"), "isValue",
		                Schema.ofBoolean("isValue"), "isToValue", Schema.ofBoolean("isToValue")));

		var schemaMap = new HashMap<String, Schema>();

		schemaMap.put("filterOperator", filterOperator);
		schemaMap.put("FilterCondition", FilterCondition);

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

		var repo = new ReactiveHybridRepository<>(new ReactiveRepositoryWrapper<>(new TestRepository()),
		        new KIRunReactiveSchemaRepository());

		var temp = new JsonArray();
		var tempOb = new JsonObject();
		tempOb.addProperty("field", "a.b.c.d");
		tempOb.addProperty("value", "surendhar");
		tempOb.addProperty("filterConditionOperator", "LESS_THAN");
		tempOb.addProperty("negate", true);
		tempOb.addProperty("isValue", false);
		temp.add(tempOb);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, FilterCondition, repo, tempOb))
		        .expectNext(tempOb)
		        .verifyComplete();
	}

	@Test
	void complexConditionTest() {

		Schema complexOperator = Schema.ofString("complexOperator")
		        .setNamespace("test")
		        .setEnums(List.of(new JsonPrimitive("AND"), new JsonPrimitive("OR")));

		Schema arraySchema = Schema.ofArray("conditions", Schema.ofRef("#"));

		Schema ComplexCondition = Schema.ofObject("ComplexCondition")
		        .setNamespace("test")
		        .setProperties(Map.of("conditions", arraySchema, "negate", Schema.ofBoolean("negate"),
		                "complexConditionOperator", Schema.ofRef("test.complexOperator")));

		var schemaMap = new HashMap<String, Schema>();
		schemaMap.put("complexOperator", complexOperator);
		schemaMap.put("ComplexCondition", ComplexCondition);

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

		var repo = new ReactiveHybridRepository<>(new ReactiveRepositoryWrapper<>(new TestRepository()),
		        new KIRunReactiveSchemaRepository());

		JsonObject mjob = new JsonObject();
		JsonObject bjob = new JsonObject();
		JsonArray ja = new JsonArray();
		bjob.add("conditions", ja);
		bjob.addProperty("negate", true);
		bjob.addProperty("complexConditionOperator", "OR");
		mjob = bjob.deepCopy();
		mjob.remove("complexConditionOperator");
		mjob.addProperty("complexConditionOperator", "AND");
		var njob = bjob.deepCopy();
		ja.add(mjob);
		ja.add(njob);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, ComplexCondition, repo, bjob))
		        .expectNext(bjob)
		        .verifyComplete();
	}

	@Test
	void filterComplexConditionTest() {

		var tempOb = new JsonObject();
		tempOb.addProperty("field", "a.b.c.d");
		tempOb.addProperty("value", "surendhar");
		tempOb.addProperty("operator", "LESS_THAN");
		tempOb.addProperty("negate", true);
		tempOb.addProperty("isValue", false);

		var tempOb1 = new JsonObject();
		tempOb1.addProperty("field", "a.b.c.d");
		tempOb1.addProperty("value", "surendhar");
		tempOb1.addProperty("operator", "GREATER_THAN");
		tempOb1.addProperty("negate", true);
		tempOb1.addProperty("isValue", true);

		var jsonArrayI = new JsonArray();
		jsonArrayI.add("a");
		jsonArrayI.add("b");
		jsonArrayI.add("c");

		var tempOb2 = new JsonObject();
		tempOb2.addProperty("field", "a.b.c.d");
		tempOb2.add("multiValue", jsonArrayI);
		tempOb2.addProperty("operator", "IN");

		JsonObject mjob = new JsonObject();
		JsonObject bjob = new JsonObject();
		JsonArray ja = new JsonArray();
		bjob.add("conditions", ja);
		bjob.addProperty("negate", true);
		bjob.addProperty("operator", "OR");
		bjob.get("conditions")
		        .getAsJsonArray()
		        .add(tempOb);
		bjob.get("conditions")
		        .getAsJsonArray()
		        .add(tempOb1);
		bjob.get("conditions")
		        .getAsJsonArray()
		        .add(tempOb2);
		mjob.add("conditions", new JsonArray());
		mjob.addProperty("negate", false);
		mjob.addProperty("operator", "AND");
		bjob.get("conditions")
		        .getAsJsonArray()
		        .add(mjob);

		StepVerifier
		        .create(ReactiveSchemaValidator.validate(null, CONDITION_FILTER, new KIRunReactiveSchemaRepository(),
		                bjob))
		        .expectNext(bjob)
		        .verifyComplete();

	}

	@Test
	void filterAndComplexConditionTest() {

		var tempOb = new JsonObject();
		tempOb.addProperty("field", "a.b.c.d");
		tempOb.addProperty("value", "surendhar");
		tempOb.addProperty("operator", "LESS_THAN");
		tempOb.addProperty("negate", true);
		tempOb.addProperty("isValue", false);

		var tempOb1 = new JsonObject();
		tempOb1.addProperty("field", "a.b.c.d");
		tempOb1.addProperty("value", "surendhar");
		tempOb1.addProperty("operator", "GREATER_THAN");
		tempOb1.addProperty("negate", true);
		tempOb1.addProperty("isValue", true);

		var jsonArrayI = new JsonArray();
		jsonArrayI.add("a");
		jsonArrayI.add("b");
		jsonArrayI.add("c");

		var tempOb2 = new JsonObject();
		tempOb2.addProperty("field", "a.b.c.d");
		tempOb2.add("multiValue", jsonArrayI);
		tempOb2.addProperty("operator", "IN");

		JsonObject mjob = new JsonObject();
		JsonObject bjob = new JsonObject();
		JsonArray ja = new JsonArray();
		bjob.add("conditions", ja);
		bjob.addProperty("negate", true);
		bjob.addProperty("operator", "OR");
		bjob.get("conditions")
		        .getAsJsonArray()
		        .add(tempOb);
		bjob.get("conditions")
		        .getAsJsonArray()
		        .add(tempOb1);
		bjob.get("conditions")
		        .getAsJsonArray()
		        .add(tempOb2);
		mjob.add("conditions", new JsonArray());
		mjob.addProperty("negate", false);
		mjob.addProperty("operator", "AND");
		bjob.get("conditions")
		        .getAsJsonArray()
		        .add(mjob);

		StepVerifier
		        .create(ReactiveSchemaValidator.validate(null, CONDITION_FILTER, new KIRunReactiveSchemaRepository(),
		                bjob))
		        .expectNext(bjob)
		        .verifyComplete();
	}

	@Test
	void enumTest() {

		Schema filterOperator = Schema.ofObject("filterOperator")
		        .setNamespace("test")
		        .setProperties(Map.of("operator", Schema.ofString("operator")
		                .setEnums(List.of(new JsonPrimitive("EQUALS"), new JsonPrimitive("LESS_THAN"),
		                        new JsonPrimitive("GREATER_THAN"), new JsonPrimitive("LESS_THAN_EQUAL")))));

		JsonObject job = new JsonObject();
		job.addProperty("operator", "EQUALS");
		
		StepVerifier
        .create(ReactiveSchemaValidator.validate(null, filterOperator, new KIRunReactiveSchemaRepository(),
        		job))
        .expectNext(job)
        .verifyComplete();

	}
}
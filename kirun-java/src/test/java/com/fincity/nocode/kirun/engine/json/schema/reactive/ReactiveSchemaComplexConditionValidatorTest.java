package com.fincity.nocode.kirun.engine.json.schema.reactive;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import reactor.test.StepVerifier;

class ReactiveSchemaComplexConditionValidatorTest {

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

		JsonObject bjob = new JsonObject();
		JsonArray ja = new JsonArray();
		ja.add(tempOb);
		ja.add(tempOb1);

		bjob.add("conditions", ja);
		bjob.addProperty("operator", "OR");

		StepVerifier
		        .create(ReactiveSchemaValidator.validate(null, CONDITION_FILTER, new KIRunReactiveSchemaRepository(),
		                bjob))
		        .expectNext(bjob)
		        .verifyComplete();

	}

	@Test
	void filterComplexonlyConditionTest() {

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

		JsonObject outerObj = new JsonObject();
		JsonArray ja2 = new JsonArray();
		ja2.add(bjob);
		outerObj.add("conditions", ja2);
		outerObj.addProperty("operator", "OR");

		StepVerifier
		        .create(ReactiveSchemaValidator.validate(null, CONDITION_FILTER, new KIRunReactiveSchemaRepository(),
		                outerObj))
		        .expectNext(outerObj)
		        .verifyComplete();

	}

}
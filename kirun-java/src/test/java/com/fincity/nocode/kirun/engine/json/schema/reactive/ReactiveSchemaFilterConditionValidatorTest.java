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
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import reactor.test.StepVerifier;

class ReactiveSchemaFilterConditionValidatorTest {

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

		JsonObject jo1 = new JsonObject();
		JsonArray ja1 = new JsonArray();
		ja1.add(12312);
		ja1.add(45634);
		jo1.addProperty("name", "surendhar");
		jo1.add("phone", ja1);

		var tempOb = new JsonObject();
		tempOb.addProperty("field", "a.b.c.d");
		tempOb.add("value", jo1); // adding object in place of value as it is any schema type
		tempOb.addProperty("operator", "LESS_THAN");
		tempOb.addProperty("negate", true);
		tempOb.addProperty("isValue", true);

		var tempOb1 = new JsonObject();
		tempOb1.addProperty("field", "PhoneNumber");
		tempOb1.add("multiValue", ja1); // adding an array in place of value as it is any schema type
		tempOb1.addProperty("operator", "IN");
		tempOb1.addProperty("negate", true);
		tempOb1.addProperty("isValue", false);
		tempOb1.addProperty("isToValue", false);

		var tempOb2 = new JsonObject();
		tempOb2.addProperty("field", "nullcheck");
		tempOb2.addProperty("operator", "LESS_THAN");
		tempOb2.add("value", JsonNull.INSTANCE); // adding null object in place of value as it is any schema type
		tempOb2.addProperty("isValue", true);
		tempOb2.addProperty("negate", false);
		tempOb2.addProperty("isToValue", false);

		StepVerifier
		        .create(ReactiveSchemaValidator.validate(null, CONDITION_FILTER, new KIRunReactiveSchemaRepository(),
		                tempOb))
		        .expectNext(tempOb)
		        .verifyComplete();
		StepVerifier
		        .create(ReactiveSchemaValidator.validate(null, CONDITION_FILTER, new KIRunReactiveSchemaRepository(),
		                tempOb1))
		        .expectNext(tempOb1)
		        .verifyComplete();
		StepVerifier
		        .create(ReactiveSchemaValidator.validate(null, CONDITION_FILTER, new KIRunReactiveSchemaRepository(),
		                tempOb2))
		        .expectNext(tempOb2)
		        .verifyComplete();
	}

}
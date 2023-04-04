package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.HybridRepository;
import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class SchemaFilterConditionValidatorTest {

	@Test
	void filterComplexConditionTest() {

		Schema filterOperator = Schema.ofString("filterOperator")
				.setNamespace("test")
				.setEnums(List.of(new JsonPrimitive("EQUALS"), new JsonPrimitive("LESS_THAN"),
						new JsonPrimitive("GREATER_THAN"), new JsonPrimitive("LESS_THAN_EQUAL"),
						new JsonPrimitive("BETWEEN"), new JsonPrimitive("IN")))
				.setDefaultValue(new JsonPrimitive("EQUALS"));

		Schema filterCondition = Schema.ofObject("FilterCondition")
				.setNamespace("test")
				.setProperties(Map.of("negate", Schema.ofBoolean("negate")
						.setDefaultValue(new JsonPrimitive(Boolean.FALSE)), "operator",
						Schema.ofRef("test.filterOperator"), "field", Schema.ofString("field"), "value",
						Schema.ofAny("value"), "toValue", Schema.ofAny("toValue"),

						"multiValue", Schema.ofArray("multiValue")
								.setItems(new ArraySchemaType().setSingleSchema(Schema.ofAny("singleType"))),
						"isValue", Schema.ofBoolean("isValue")
								.setDefaultValue(new JsonPrimitive(false)),
						"isToValue", Schema.ofBoolean("isToValue")
								.setDefaultValue(new JsonPrimitive(false))))
				.setRequired(List.of("operator", "field"))
				.setAdditionalProperties(new AdditionalType().setBooleanValue(false));

		var schemaMap = new HashMap<String, Schema>();

		schemaMap.put("filterOperator", filterOperator);
		schemaMap.put("FilterCondition", filterCondition);
	
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
		var repo = new HybridRepository<>(new TestRepository(), new KIRunSchemaRepository());

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

		var tempOb2 = new JsonObject();
		tempOb2.addProperty("field", "nullcheck");
		tempOb2.addProperty("operator", "LESS_THAN");
		tempOb2.add("value", JsonNull.INSTANCE); // adding null object in place of value as it is any schema type
		tempOb2.addProperty("isValue", true);

		var res1 = SchemaValidator.validate(null, filterCondition, repo, tempOb);
		assertEquals(tempOb, res1); // value passed as object

		var res2 = SchemaValidator.validate(null, filterCondition, repo, tempOb1);
		assertEquals(tempOb1, res2); // multivalue passed as array

		var res3 = SchemaValidator.validate(null, filterCondition, repo, tempOb2);
		assertEquals(tempOb2, res3); // value passed as null object
	}

}
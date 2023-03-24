package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.HybridRepository;
import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalPropertiesType;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class SchemaComplexConditionValidatorTest {

//    @Test
    void filterComplexConditionTest() {

        Schema filterOperator = Schema.ofString("filterOperator").setNamespace("test")
                .setEnums(List.of(new JsonPrimitive("EQUALS"),
                        new JsonPrimitive("LESS_THAN"), new JsonPrimitive("GREATER_THAN"),
                        new JsonPrimitive("LESS_THAN_EQUAL"), new JsonPrimitive("BETWEEN"), new JsonPrimitive("IN")))
                .setDefaultValue(new JsonPrimitive("EQUALS"));

        Schema FilterCondition = Schema.ofObject("FilterCondition").setNamespace("test").setProperties(Map.of("negate",
                Schema.ofBoolean("negate").setDefaultValue(new JsonPrimitive(Boolean.FALSE)), "operator",
                Schema.ofRef("test.filterOperator"), "field",
                Schema.ofString("field"),
                "value", Schema.ofAny("value"), "toValue", Schema.ofAny("toValue"),

                "multiValue",
                Schema.ofArray("multiValue")
                        .setItems(new ArraySchemaType().setSingleSchema(Schema.ofAny("singleType"))),
                "isValue",
                Schema.ofBoolean("isValue").setDefaultValue(new JsonPrimitive(false)), "isToValue",
                Schema.ofBoolean("isToValue").setDefaultValue(new JsonPrimitive(false))))
                .setRequired(List.of("operator", "field"))
                .setAdditionalProperties(new AdditionalPropertiesType().setBooleanValue(false));

        Schema complexOperator = Schema.ofString("complexOperator").setNamespace("test")
                .setEnums(List.of(new JsonPrimitive("AND"), new JsonPrimitive("OR")));

        Schema arraySchema = Schema.ofArray("conditions",
                new Schema().setAnyOf(
                        List.of(Schema.ofRef("#"), Schema.ofRef("test.FilterCondition"))));

        Schema ComplexCondition = Schema.ofObject("ComplexCondition")
                .setNamespace("test")
                .setProperties(Map.of("conditions", arraySchema, "negate",
                        Schema.ofBoolean("negate").setDefaultValue(new JsonPrimitive(Boolean.FALSE)),
                        "operator", Schema.ofRef("test.complexOperator")))
                .setRequired(List.of("conditions", "operator"))
                .setAdditionalProperties(new AdditionalPropertiesType().setBooleanValue(false));

        Schema onlyFilterSchema = Schema.ofArray("conditions",
                new Schema().setAnyOf(
                        List.of(Schema.ofRef("test.FilterCondition"))));

        Schema compConditions = Schema.ofObject("compConditions").setNamespace("test")
                .setProperties(Map.of("conditions", onlyFilterSchema))
                .setRequired(List.of("conditions"));

        var schemaMap = new HashMap<String, Schema>();

        schemaMap.put("filterOperator", filterOperator);
        schemaMap.put("FilterCondition", FilterCondition);
        schemaMap.put("complexOperator", complexOperator);
        schemaMap.put("ComplexCondition", ComplexCondition);
        schemaMap.put("compConditions", compConditions);

        class TestRepository implements Repository<Schema> {

            @Override
            public Schema find(String namespace, String name) {
                if (namespace == null) {
                    return null;
                }
                return schemaMap.get(name);
            }

        }
        var repo = new HybridRepository<>(new TestRepository(), new KIRunSchemaRepository());

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

        var res = SchemaValidator.validate(null, compConditions, repo, bjob);

        assertEquals(bjob, res);

    }

    @Test
    void filterComplexonlyConditionTest() {

        Schema filterOperator = Schema.ofString("filterOperator").setNamespace("test")
                .setEnums(List.of(new JsonPrimitive("EQUALS"),
                        new JsonPrimitive("LESS_THAN"), new JsonPrimitive("GREATER_THAN"),
                        new JsonPrimitive("LESS_THAN_EQUAL"), new JsonPrimitive("BETWEEN"), new JsonPrimitive("IN")))
                .setDefaultValue(new JsonPrimitive("EQUALS"));

        Schema FilterCondition = Schema.ofObject("FilterCondition").setNamespace("test").setProperties(Map.of("negate",
                Schema.ofBoolean("negate").setDefaultValue(new JsonPrimitive(Boolean.FALSE)), "operator",
                Schema.ofRef("test.filterOperator"), "field",
                Schema.ofString("field"),
                "value", Schema.ofAny("value"), "toValue", Schema.ofAny("toValue"),

                "multiValue",
                Schema.ofArray("multiValue")
                        .setItems(new ArraySchemaType().setSingleSchema(Schema.ofAny("singleType"))),
                "isValue",
                Schema.ofBoolean("isValue").setDefaultValue(new JsonPrimitive(false)), "isToValue",
                Schema.ofBoolean("isToValue").setDefaultValue(new JsonPrimitive(false))))
                .setRequired(List.of("operator", "field"))
                .setAdditionalProperties(new AdditionalPropertiesType().setBooleanValue(false));

        Schema complexOperator = Schema.ofString("complexOperator").setNamespace("test")
                .setEnums(List.of(new JsonPrimitive("AND"), new JsonPrimitive("OR")));

        Schema arraySchema = Schema.ofArray("conditions",
                new Schema().setAnyOf(
                        List.of(Schema.ofRef("#"), Schema.ofRef("test.FilterCondition"))));

        Schema ComplexCondition = Schema.ofObject("ComplexCondition")
                .setNamespace("test")
                .setProperties(Map.of("conditions", arraySchema, "negate",
                        Schema.ofBoolean("negate").setDefaultValue(new JsonPrimitive(Boolean.FALSE)),
                        "operator", Schema.ofRef("test.complexOperator")))
                .setRequired(List.of("conditions", "operator"))
                .setAdditionalProperties(new AdditionalPropertiesType().setBooleanValue(false));

        Schema onlyComplexSchema = Schema.ofArray("conditions",
                new Schema().setAnyOf(
                        List.of(Schema.ofRef("test.ComplexCondition"))));

        Schema compConditions = Schema.ofObject("compConditions").setNamespace("test")
                .setProperties(Map.of("conditions", onlyComplexSchema))
                .setRequired(List.of("conditions"));

        var schemaMap = new HashMap<String, Schema>();

        schemaMap.put("filterOperator", filterOperator);
        schemaMap.put("FilterCondition", FilterCondition);
        schemaMap.put("complexOperator", complexOperator);
        schemaMap.put("ComplexCondition", ComplexCondition);
        schemaMap.put("compConditions", compConditions);

        class TestRepository implements Repository<Schema> {

            @Override
            public Schema find(String namespace, String name) {
                if (namespace == null) {
                    return null;
                }
                return schemaMap.get(name);
            }

        }
        var repo = new HybridRepository<>(new TestRepository(), new KIRunSchemaRepository());

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
        bjob.get("conditions").getAsJsonArray().add(tempOb);
        bjob.get("conditions").getAsJsonArray().add(tempOb1);
        bjob.get("conditions").getAsJsonArray().add(tempOb2);
        mjob.add("conditions", new JsonArray());
        mjob.addProperty("negate", false);
        mjob.addProperty("operator", "AND");
        bjob.get("conditions").getAsJsonArray().add(mjob);

        JsonObject outerObj = new JsonObject();
        JsonArray ja2 = new JsonArray();
        ja2.add(bjob);
        outerObj.add("conditions", ja2);
        System.out.println(outerObj);
        var res = SchemaValidator.validate(null, compConditions, repo, outerObj);

        assertEquals(outerObj, res);

    }

}
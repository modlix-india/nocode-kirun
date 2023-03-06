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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class SchemaAnyofValidatorTest {

    @Test
    void test() {

        Schema complexOperator = Schema.ofString("complexOperator").setNamespace("test")
                .setEnums(List.of(new JsonPrimitive("AND"), new JsonPrimitive("OR")));

        Schema filterOperator = Schema.ofString("filterOperator").setNamespace("test")
                .setProperties(Map.of("operator",
                        Schema.ofString("operator").setEnums(List.of(new JsonPrimitive("EQUALS"),
                                new JsonPrimitive("LESS_THAN"), new JsonPrimitive("GREATER_THAN"),
                                new JsonPrimitive("LESS_THAN_EQUAL")))));

        Schema arraySchema = Schema.ofArray("conditions");

        Schema ComplexCondition = Schema.ofObject("ComplexCondition")
                .setNamespace("test")
                .setProperties(Map.of("conditions", arraySchema, "negate",
                        Schema.ofBoolean("negate"), "complexConditionOperator", Schema.ofRef("test.complexOperator")));

        Schema FilterCondition = Schema.ofObject("FilterCondition").setNamespace("test").setProperties(Map.of("negate",
                Schema.ofBoolean("negate"), "filterConditionOperator", Schema.ofRef("test.filterOperator"), "field",
                Schema.ofString("field"),
                "value", Schema.ofAny("value"), "toValue", Schema.ofAny("toValue"), "isValue",
                Schema.ofBoolean("isValue"), "isToValue", Schema.ofBoolean("isToValue")));

        Schema completeConditions = Schema.ofArray("completeConditions").setItems(new ArraySchemaType()
                .setSingleSchema(Schema.ofObject("singleSchema")
                        .setAnyOf(
                                List.of(Schema.ofRef("test.ComplexCondition"), Schema.ofRef("test.FilterCondition")))));

        Schema cconditions = Schema.ofObject("cconditions").setProperties(
                Map.of("completeConditions", completeConditions));

        var schemaMap = new HashMap<String, Schema>();
        schemaMap.put("complexOperator", complexOperator);
        schemaMap.put("filterOperator", filterOperator);
        schemaMap.put("ComplexCondition", ComplexCondition);
        schemaMap.put("FilterCondition", FilterCondition);

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

        JsonObject job = new JsonObject();
        JsonArray ja = new JsonArray();
        JsonElement je = new JsonObject();
        var temp = new JsonArray();
        var tempOb = new JsonObject();
        tempOb.addProperty("field", 2);
        tempOb.addProperty("value", "surendhar");
        tempOb.addProperty("operator", "LESS_THAN");
        temp.add(tempOb);
        je.getAsJsonObject().addProperty("negate", false);
        je.getAsJsonObject().addProperty("complexConditionOperator", "ORR");
        je.getAsJsonObject().add("conditions", temp);

        ja.add(je);
        job.add("completeConditions", ja);

        JsonElement result = SchemaValidator.validate(null, cconditions, repo, job);
        assertEquals(job, result);
    }

    @Test
    void filterConditionTest() {

        Schema filterOperator = Schema.ofString("filterOperator").setNamespace("test")
                .setEnums(List.of(new JsonPrimitive("EQUALS"),
                        new JsonPrimitive("LESS_THAN"), new JsonPrimitive("GREATER_THAN"),
                        new JsonPrimitive("LESS_THAN_EQUAL")));

        Schema FilterCondition = Schema.ofObject("FilterCondition").setNamespace("test").setProperties(Map.of("negate",
                Schema.ofBoolean("negate"), "filterConditionOperator", Schema.ofRef("test.filterOperator"), "field",
                Schema.ofString("field"),
                "value", Schema.ofAny("value"), "toValue", Schema.ofAny("toValue"), "isValue",
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

        }

        var repo = new HybridRepository<>(new TestRepository(), new KIRunSchemaRepository());

        var temp = new JsonArray();
        var tempOb = new JsonObject();
        tempOb.addProperty("field", "a.b.c.d");
        tempOb.addProperty("value", "surendhar");
        tempOb.addProperty("filterConditionOperator", "LESS_THAN");
        tempOb.addProperty("negate", true);
        tempOb.addProperty("isValue", false);
        temp.add(tempOb);
        var res = SchemaValidator.validate(null, FilterCondition, repo, tempOb);
        assertEquals(tempOb, res);
    }

    @Test
    void complexConditionTest() {

        Schema complexOperator = Schema.ofString("complexOperator").setNamespace("test")
                .setEnums(List.of(new JsonPrimitive("AND"), new JsonPrimitive("OR")));

        Schema arraySchema = Schema.ofArray("conditions", Schema.ofRef("#"));

        Schema ComplexCondition = Schema.ofObject("ComplexCondition")
                .setNamespace("test")
                .setProperties(Map.of("conditions", arraySchema, "negate",
                        Schema.ofBoolean("negate"), "complexConditionOperator", Schema.ofRef("test.complexOperator")));

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

        }

        var repo = new HybridRepository<>(new TestRepository(), new KIRunSchemaRepository());

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
        var res = SchemaValidator.validate(null, ComplexCondition, repo, bjob);
        assertEquals(bjob, res);
    }

    // for 1 level complex condition schema is working but more than one level
    // schema validation going wrong

    @Test
    void filterComplexConditionTest() { // not working for self referencing

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

        var schemaMap = new HashMap<String, Schema>();

        schemaMap.put("filterOperator", filterOperator);
        schemaMap.put("FilterCondition", FilterCondition);
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

        System.out.println(bjob);
        var res = SchemaValidator.validate(null, ComplexCondition, repo, bjob);

        assertEquals(bjob, res);

    }

    @Test
    void enumTest() {

        Schema filterOperator = Schema.ofObject("filterOperator").setNamespace("test")
                .setProperties(Map.of("operator",
                        Schema.ofString("operator").setEnums(List.of(new JsonPrimitive("EQUALS"),
                                new JsonPrimitive("LESS_THAN"), new JsonPrimitive("GREATER_THAN"),
                                new JsonPrimitive("LESS_THAN_EQUAL")))));

        JsonObject job = new JsonObject();
        job.addProperty("operator", "EQUALS");

        SchemaValidator.validate(null, filterOperator, null, job);

    }
}
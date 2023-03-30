package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

class ObjectValidatorTest {

    @Test
    void schemaObjectNewFalseBooleanPassTest() {

        Map<String, Schema> props = new HashMap<>();
        props.put("name", Schema.ofString("name"));
        props.put("phone", Schema.ofLong("phone"));
        Schema schema = Schema.ofObject("schema").setProperties(props)
                .setAdditionalProperties(new AdditionalType(false));

        JsonObject job = new JsonObject();
        job.addProperty("name", "surendhar");
        job.addProperty("phone", 13423524);

        assertEquals(job, SchemaValidator.validate(null, schema, null, job));
    }

    @Test
    void schemaObjectNewFalseBooleanFailTest() {

        Map<String, Schema> props = new HashMap<>();
        props.put("name", Schema.ofString("name"));
        props.put("phone", Schema.ofLong("phone"));
        Schema schema = Schema.ofObject("schema").setProperties(props)
                .setAdditionalProperties(new AdditionalType(false));

        JsonObject job = new JsonObject();
        job.addProperty("name", "surendhar");
        job.addProperty("phone", 13423524);
        job.addProperty("age", 12);

        SchemaValidationException sve = assertThrows(SchemaValidationException.class,
                () -> SchemaValidator.validate(null, schema, null, job));

        assertEquals("schema - Value " + job.toString() + " is not of valid type(s)\n"
                + "schema - [age] are additional properties which are not allowed.", sve.getMessage());
    }

    @Test
    void schemaObjectWithNewTrueBooleanPassTest() {

        Map<String, Schema> props = new HashMap<>();
        props.put("name", Schema.ofString("name"));
        props.put("phone", Schema.ofLong("phone"));
        Schema schema = Schema.ofObject("schema").setProperties(props)
                .setAdditionalProperties(new AdditionalType(true));

        JsonObject job = new JsonObject();
        job.addProperty("name", "surendhar");
        job.addProperty("phone", 13423524);

        assertEquals(job, SchemaValidator.validate(null, schema, null, job));
    }

    @Test
    void schemaObjectWithNewTrueBooleanTest() {

        Map<String, Schema> props = new HashMap<>();
        props.put("name", Schema.ofString("name"));
        props.put("phone", Schema.ofLong("phone"));
        Schema schema = Schema.ofObject("schema").setProperties(props)
                .setAdditionalProperties(new AdditionalType(true));

        JsonObject job = new JsonObject();
        job.addProperty("name", "surendhar");
        job.addProperty("phone", 13423524);
        job.addProperty("age", 12);

        assertEquals(job, SchemaValidator.validate(null, schema, null, job));
    }

    @Test
    void schemaObjectOldFalseBooleanPassTest() {

        Map<String, Schema> props = new HashMap<>();
        props.put("name", Schema.ofString("name"));
        props.put("phone", Schema.ofLong("phone"));
        Schema schema = Schema.ofObject("schema").setProperties(props)
                .setAdditionalProperties(new AdditionalType().setBooleanValue(false));

        JsonObject job = new JsonObject();
        job.addProperty("name", "surendhar");
        job.addProperty("phone", 13423524);

        assertEquals(job, SchemaValidator.validate(null, schema, null, job));
    }

    @Test
    void schemaObjectOldFalseBooleanFailTest() {

        Map<String, Schema> props = new HashMap<>();
        props.put("name", Schema.ofString("name"));
        props.put("phone", Schema.ofLong("phone"));
        Schema schema = Schema.ofObject("schema").setProperties(props)
                .setAdditionalProperties(new AdditionalType().setBooleanValue(false));

        JsonObject job = new JsonObject();
        job.addProperty("name", "surendhar");
        job.addProperty("phone", 13423524);
        job.addProperty("age", 12);

        SchemaValidationException sve = assertThrows(SchemaValidationException.class,
                () -> SchemaValidator.validate(null, schema, null, job));

        assertEquals("schema - Value " + job.toString() + " is not of valid type(s)\n"
                + "schema - [age] are additional properties which are not allowed.", sve.getMessage());
    }

    @Test
    void schemaObjectWithOldTrueBooleanPassTest() {

        Map<String, Schema> props = new HashMap<>();
        props.put("name", Schema.ofString("name"));
        props.put("phone", Schema.ofLong("phone"));
        Schema schema = Schema.ofObject("schema").setProperties(props)
                .setAdditionalProperties(new AdditionalType().setBooleanValue(true));

        JsonObject job = new JsonObject();
        job.addProperty("name", "surendhar");
        job.addProperty("phone", 13423524);

        assertEquals(job, SchemaValidator.validate(null, schema, null, job));
    }

    @Test
    void schemaObjectWithOldTrueBooleanTest() {

        Map<String, Schema> props = new HashMap<>();
        props.put("name", Schema.ofString("name"));
        props.put("phone", Schema.ofLong("phone"));
        Schema schema = Schema.ofObject("schema").setProperties(props)
                .setAdditionalProperties(new AdditionalType().setBooleanValue(true));

        JsonObject job = new JsonObject();
        job.addProperty("name", "surendhar");
        job.addProperty("phone", 13423524);
        job.addProperty("age", 12);

        assertEquals(job, SchemaValidator.validate(null, schema, null, job));
    }

    @Test
    void schemaObjectOldSchemaPassTest() {

        Map<String, Schema> props = new HashMap<>();
        props.put("name", Schema.ofString("name"));
        props.put("phone", Schema.ofLong("phone"));

        Schema addSchema = Schema.ofBoolean("addSchema");

        Schema schema = Schema.ofObject("schema").setProperties(props)
                .setAdditionalProperties(new AdditionalType().setSchemaValue(addSchema));

        JsonObject job = new JsonObject();
        job.addProperty("name", "surendhar");
        job.addProperty("phone", 13423524);
        job.addProperty("married?", false);

        assertEquals(job, SchemaValidator.validate(null, schema, null, job));
    }

    @Test
    void schemaObjectOldSchemaFailTest() {

        Map<String, Schema> props = new HashMap<>();
        props.put("name", Schema.ofString("name"));
        props.put("phone", Schema.ofLong("phone"));

        Schema addSchema = Schema.ofBoolean("addSchema");

        Schema schema = Schema.ofObject("schema").setProperties(props)
                .setAdditionalProperties(new AdditionalType().setSchemaValue(addSchema));

        String city = "Yanam";

        JsonObject job = new JsonObject();
        job.addProperty("name", "surendhar");
        job.addProperty("phone", 13423524);
        job.addProperty("married?", false);
        job.addProperty("working", true);
        job.addProperty("city", city);

        SchemaValidationException sve = assertThrows(SchemaValidationException.class,
                () -> SchemaValidator.validate(null, schema, null, job));

        assertEquals("schema - Value " + job.toString() + " is not of valid type(s)\n" +
                "schema.addSchema - Value \"" + city + "\" is not of valid type(s)\n" +
                "schema.addSchema - \"" + city + "\" is not a boolean", sve.getMessage());
    }

    @Test
    void schemaObjectOldSchemaTypePassTest() {

        AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Type.class, new SchemaTypeAdapter())
                .registerTypeAdapter(AdditionalType.class,
                        addType)
                .create();

        addType.setGson(gson);

        Map<String, Schema> props = new HashMap<>();
        props.put("name", Schema.ofString("name"));
        props.put("phone", Schema.ofLong("phone"));

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

        SchemaValidationException sve = assertThrows(SchemaValidationException.class,
                () -> SchemaValidator.validate(null, schema, null, job));

        assertEquals(
                "null - Value {\"name\":\"surendhar\",\"phone\":13423524,\"married\":false} is not of valid type(s)\n"
                        + "null.null - Value 13423524 is not of valid type(s)\n"
                        + "null.null - 13423524 is not String",
                sve.getMessage());

    }

}

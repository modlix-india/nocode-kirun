package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ArraySchemaAdapterTypeTest {

    @Test
    void schemaObjectPollutionSchemaValueTypePassTest() {

        AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

        ArraySchemaTypeAdapter asType = new ArraySchemaTypeAdapter();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Type.class, new SchemaTypeAdapter())
                .registerTypeAdapter(ArraySchemaType.class, asType)
                .registerTypeAdapter(AdditionalType.class,
                        addType)
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

        var repo = new KIRunSchemaRepository();

        var firstValue = SchemaValidator.validate(null, schema, repo, null);

        var value = SchemaValidator.validate(
                null,
                xschema,
                repo,
                firstValue);

        assertEquals("Kiran", value.getAsJsonArray().get(0).getAsJsonObject().get("y").getAsString());

        assertNull(schema.getDefaultValue().getAsJsonArray().get(0).getAsJsonObject().get("y"));
    }

    @Test
    void schemaItemsAdapterTest() {

        ArraySchemaTypeAdapter asta = new ArraySchemaTypeAdapter();
        SchemaTypeAdapter sta = new SchemaTypeAdapter();
        AdditionalTypeAdapter ata = new AdditionalTypeAdapter();

        Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, sta)
                .registerTypeAdapter(ArraySchemaType.class, asta)
                .registerTypeAdapter(AdditionalType.class, ata)
                .create();

        asta.setGson(gson);
        ata.setGson(gson);

        var arrayAdap = gson.fromJson("""
                [{"type":"OBJECT","properties":{"x":{"type":"INTEGER"}}}]
                """, ArraySchemaType.class);

        assert (arrayAdap.getSingleSchema() == null);
        assert (arrayAdap.getTupleSchema() != null);
        assert (arrayAdap.getTupleSchema().get(0).getProperties().get("x").getType()
                .equals(Type.of(SchemaType.INTEGER)));
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

        SchemaValidationException sve = assertThrows(SchemaValidationException.class,
                () -> SchemaValidator.validate(null, array, null, arr));

        // for single schema type of array additional items are invalid.
        assert (sve.getMessage().contains("false is not an Object"));
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

        SchemaValidationException sve = assertThrows(SchemaValidationException.class,
                () -> SchemaValidator.validate(null, array, null, arr));

        assert (sve.getMessage().contains("name is mandatory"));
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
        assertEquals(arr, SchemaValidator.validate(null, array, null, arr));
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
        assertEquals(arr, SchemaValidator.validate(null, arraySchema, null, arr));
    }

}

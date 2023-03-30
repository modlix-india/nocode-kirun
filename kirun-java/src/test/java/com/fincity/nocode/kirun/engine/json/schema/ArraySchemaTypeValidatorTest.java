package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.validator.ArrayValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ArraySchemaTypeValidatorTest {

    private static String ERROR_MSG = "No Additional Items are defined";

   @Test
    void schemaArrayWithSingleTypeTest() {

        ArraySchemaType ast = new ArraySchemaType().setSingleSchema(Schema.ofInteger("item"));

        Schema schema = Schema.ofArray("schema").setItems(ast);
        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add(2);
        arr.add(3);
        arr.add(4);
        assertEquals(arr, ArrayValidator.validate(null, schema, null, arr));
    }

   @Test
    void schemaArrayWithTupleTypeTest() {

        List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

        ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

        Schema schema = Schema.ofArray("schema").setItems(ast);
        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add("surendhar");
        arr.add(false);
        assertEquals(arr, ArrayValidator.validate(null, schema, null, arr));
    }

   @Test
    void schemaArrayWithSingleTypeWithAdditionalFalseNewTest() {

        ArraySchemaType ast = new ArraySchemaType().setSingleSchema(Schema.ofInteger("item"));

        Schema schema = Schema.ofArray("schema").setItems(ast).setAdditionalItems(new AdditionalType(false));
        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add(2);
        arr.add(3);
        arr.add(4);
        assertEquals(arr, ArrayValidator.validate(null, schema, null, arr));
    }

   @Test
    void schemaArrayWithSingleTypeWithAdditionalTrueNewTest() {

        ArraySchemaType ast = new ArraySchemaType().setSingleSchema(Schema.ofInteger("item"));

        Schema schema = Schema.ofArray("schema").setItems(ast).setAdditionalItems(new AdditionalType(false));
        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add(2);
        arr.add(3);
        arr.add(4);
        arr.add("surendhar");

        SchemaValidationException sve = assertThrows(SchemaValidationException.class,
                () -> ArrayValidator.validate(null, schema, null, arr));

        assertEquals("item - Value \"surendhar\" is not of valid type(s)\n"
                + "item - \"surendhar\" is not a Integer", sve.getMessage());
    }

   @Test
    void schemaArrayWithSingleTypeWithAdditionalFalseOldTest() {

        ArraySchemaType ast = new ArraySchemaType().setSingleSchema(Schema.ofInteger("item"));

        Schema schema = Schema.ofArray("schema").setItems(ast)
                .setAdditionalItems(new AdditionalType().setBooleanValue(true));
        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add(2);
        arr.add(3);
        arr.add(4);
        assertEquals(arr, ArrayValidator.validate(null, schema, null, arr));
    }

   @Test
    void schemaArrayWithSingleTypeWithAdditionalTrueOldTest() {

        ArraySchemaType ast = new ArraySchemaType().setSingleSchema(Schema.ofInteger("item"));

        Schema schema = Schema.ofArray("schema").setItems(ast)
                .setAdditionalItems(new AdditionalType().setBooleanValue(true));
        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add(2);
        arr.add(3);
        arr.add(4);
        arr.add("surendhar");

        SchemaValidationException sve = assertThrows(SchemaValidationException.class,
                () -> ArrayValidator.validate(null, schema, null, arr));

        assertEquals("item - Value \"surendhar\" is not of valid type(s)\n"
                + "item - \"surendhar\" is not a Integer", sve.getMessage());
    }

   @Test
    void schemaArrayWithTupleTypeWithAdditionalFalseNewTest() {

        List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

        ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

        Schema schema = Schema.ofArray("schema").setItems(ast).setAdditionalItems(new AdditionalType(false));

        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add("surendhar");
        arr.add(false);
        assertEquals(arr, ArrayValidator.validate(null, schema, null, arr));
    }

   @Test
    void schemaArrayWithTupleTypeWithAdditionalFalseOldTest() {

        List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

        ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

        Schema schema = Schema.ofArray("schema").setItems(ast)
                .setAdditionalItems(new AdditionalType().setBooleanValue(false));

        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add("surendhar");
        arr.add(false);
        arr.add("asd");
        SchemaValidationException sve = assertThrows(SchemaValidationException.class,
                () -> ArrayValidator.validate(null, schema, null, arr));
        assertEquals(ERROR_MSG, sve.getMessage());
    }

   @Test
    void schemaArrayWithTupleTypeWithAdditionalFalseOldFailTest() {

        List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

        ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

        Schema schema = Schema.ofArray("schema").setItems(ast)
                .setAdditionalItems(new AdditionalType().setBooleanValue(false));

        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add("surendhar");
        arr.add(false);
        arr.add("additional");
        SchemaValidationException sve = assertThrows(SchemaValidationException.class,
                () -> ArrayValidator.validate(null, schema, null, arr));
        assertEquals(ERROR_MSG, sve.getMessage());
    }

   @Test
    void schemaArrayWithTupleTypeWithAdditionalTrueNewTest() {

        List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

        ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

        Schema schema = Schema.ofArray("schema").setItems(ast).setAdditionalItems(new AdditionalType(true));

        JsonObject job = new JsonObject();
        job.addProperty("company", "fincity");

        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add("surendhar");
        arr.add(false);
        arr.add(job);
        arr.add("name");
        arr.add("additional");
        assertEquals(arr, ArrayValidator.validate(null, schema, null, arr));
    }

   @Test
    void schemaArrayWithTupleTypeWithAdditionalTrueOldTest() {

        List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

        ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

        Schema schema = Schema.ofArray("schema").setItems(ast)
                .setAdditionalItems(new AdditionalType().setBooleanValue(true));

        JsonObject job = new JsonObject();
        job.addProperty("company", "fincity");

        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add("surendhar");
        arr.add(false);
        arr.add(job);
        assertEquals(arr, ArrayValidator.validate(null, schema, null, arr));
    }

   @Test
    void schemaArrayWithSingleTypeWithAdditionalSchemaNewTest() {

        ArraySchemaType ast = new ArraySchemaType().setSingleSchema(Schema.ofInteger("item"));
        Schema addSchema = Schema.ofString("addSchema");

        Schema schema = Schema.ofArray("schema").setItems(ast)
                .setAdditionalItems(new AdditionalType().setSchemaValue(addSchema));

        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add(2);
        arr.add(3);
        arr.add(4);
        arr.add("additional");

        SchemaValidationException sve = assertThrows(SchemaValidationException.class,
                () -> ArrayValidator.validate(null, schema, null, arr));

        assertEquals("item - Value \"additional\" is not of valid type(s)\n"
                + "item - \"additional\" is not a Integer", sve.getMessage());
    }

   @Test
    void schemaArrayWithTupleTypeWithAdditionalSchemaStringFailTest() {

        List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

        Schema stringSchema = Schema.ofString("stringSchema");

        ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

        Schema schema = Schema.ofArray("schema").setItems(ast)
                .setAdditionalItems(new AdditionalType().setSchemaValue(stringSchema));

        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add("surendhar");
        arr.add(false);
        arr.add("additional");
        arr.add(true);
        arr.add(1);
        SchemaValidationException sve = assertThrows(SchemaValidationException.class,
                () -> ArrayValidator.validate(null, schema, null, arr));
        assertEquals("stringSchema - Value true is not of valid type(s)\n"
                + "stringSchema - true is not String", sve.getMessage());
    }

   @Test
    void schemaArrayWithTupleTypeWithAdditionalSchemaPassTest() {

        List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

        ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

        Schema objSchema = Schema.ofObject("objSchema");

        Schema schema = Schema.ofArray("schema").setItems(ast)
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
        assertEquals(arr, ArrayValidator.validate(null, schema, null, arr));
    }

    @Test
    void schemaArrayWithTupleTypeWithAdditionalSchemaFailTest() {

        List<Schema> schemas = List.of(Schema.ofInteger("item1"), Schema.ofString("item2"), Schema.ofBoolean("item3"));

        ArraySchemaType ast = new ArraySchemaType().setTupleSchema(schemas);

        Schema objSchema = Schema.ofObject("objSchema");

        Schema schema = Schema.ofArray("schema").setItems(ast)
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

        SchemaValidationException sve = assertThrows(SchemaValidationException.class,
                () -> ArrayValidator.validate(null, schema, null, arr));
        assertEquals("objSchema - Value [1,2] is not of valid type(s)\n"
                + "objSchema - [1,2] is not an Object", sve.getMessage());
    }

    @Test
    void arrayGsonSchemaTypeAdditionalFalseTest() {

        AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
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

        assertEquals(job, ArrayValidator.validate(null, schema, null, job));
    }

    @Test
    void arrayGsonSchemaTypeAdditionalTrueTest() {

        AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
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

        SchemaValidationException sve = assertThrows(SchemaValidationException.class,
                () -> ArrayValidator.validate(null, schema, null, job));
        assertEquals("null - Value true is not of valid type(s)\n"
                + "null - true is not a Integer", sve.getMessage());
    }
}

package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ArrayAdditionalItemsValidatorTest {

    @Test
    void arrayGsonSchemaTypeAdditionalTupleSchemaPassTest() {

        AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
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

        assertEquals(job, SchemaValidator.validate(null, schema, null, job));
    }

}

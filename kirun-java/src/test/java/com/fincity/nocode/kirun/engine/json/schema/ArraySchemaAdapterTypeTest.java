package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
                firstValue
            );
        
        System.out.println(value);

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

	}

}

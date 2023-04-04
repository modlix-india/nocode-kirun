package com.fincity.nocode.kirun.engine.json.schema;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.bind.CollectionTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

class ArraySchemaAdapterTypeTest {

//    @Test
    void schemaObjectPollutionSchemaValueTypePassTest() {

        AdditionalTypeAdapter addType = new AdditionalTypeAdapter();

        ArraySchemaTypeAdapter asType = new ArraySchemaTypeAdapter();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Type.class, new SchemaTypeAdapter())
                .registerTypeAdapter(ArraySchemaTypeAdapter.class, asType)
                .registerTypeAdapter(AdditionalType.class,
                        addType)
                .create();
        asType.setGson(gson);
        addType.setGson(gson);

        var schema = gson.fromJson(
                """
                        {"type":"ARRAY","items":[{"type":"OBJECT","properties":{"x":{"type":"INTEGER"}}}],"defaultValue":[{"x":20},{"x":30}]}
                        """,
                Schema.class);

        System.out.println(schema);
        var repo = new KIRunSchemaRepository();
    }

    @Test
    void schemaItemsAdapterTest() {

        ArraySchemaTypeAdapter asta = new ArraySchemaTypeAdapter();
        SchemaTypeAdapter sta = new SchemaTypeAdapter();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(SchemaTypeAdapter.class, sta)
                .registerTypeAdapter(ArraySchemaTypeAdapter.class, asta)
                .registerTypeAdapterFactory(new CollectionTypeAdapterFactory(
                        new ConstructorConstructor(Map.of(new TypeToken<ArraySchemaTypeAdapter>() {
                        }.getType(), t -> new ArraySchemaType()))))
                .create();
        asta.setGson(gson);

        var arrayAdap = gson.fromJson("""
                [{"type":"OBJECT","properties":{"x":{"type":"INTEGER"}}}]
                """, ArraySchemaType.class);

        System.out.println(arrayAdap);
    }

}

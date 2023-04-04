package com.fincity.nocode.kirun.engine.json.schema.array;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ArraySchemaType implements Serializable {

    private static final long serialVersionUID = -3312223652146445370L;

    private Schema singleSchema;
    private List<Schema> tupleSchema;

    public static ArraySchemaType of(Schema... schemas) {

        if (schemas.length == 1)
            return new ArraySchemaType().setSingleSchema(schemas[0]);

        return new ArraySchemaType().setTupleSchema(List.of(schemas));
    }

    public boolean isSingleType() {
        return singleSchema != null;
    }

    public ArraySchemaType(ArraySchemaType ast) {

        this.singleSchema = ast.singleSchema == null ? null : new Schema(ast.singleSchema);
        this.tupleSchema = ast.tupleSchema == null ? null
                : this.tupleSchema.stream()
                        .map(Schema::new)
                        .toList();
    }

    public static class ArraySchemaTypeAdapter extends TypeAdapter<ArraySchemaType> {

        private Gson gson;

        @Override
        public void write(JsonWriter out, ArraySchemaType value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            TypeAdapter<Schema> typeAdapter = gson.getAdapter(Schema.class);

            if (value.singleSchema != null) {
                typeAdapter.write(out, value.singleSchema);
            } else if (value.tupleSchema != null)
                out.beginArray();
            value.tupleSchema.stream().forEach(e -> {
                try {
                    typeAdapter.write(out, e);
                } catch (IOException e1) {

                    throw new KIRuntimeException("Unable to parse the json in ArraySchemaTypeAdapter", e1);
                }
            });
            out.endArray();
        }

        @Override
        public ArraySchemaType read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            ArraySchemaType type = new ArraySchemaType();

            if (token == JsonToken.BEGIN_ARRAY)
                type.tupleSchema = List.of(this.gson.fromJson(in, (new TypeToken<Schema[]>() {
                }).getType()));
            else if (token == JsonToken.BEGIN_OBJECT) {
                JsonObject jsonObj = gson.fromJson(in, JsonObject.class);

                if (jsonObj.has("singleSchema")) {
                    type.singleSchema = gson.fromJson(jsonObj.get("singleSchema").getAsJsonObject(),
                            Schema.class);
                } else if (jsonObj.has("tupleSchema")) {
                    type.tupleSchema = List.of(gson.fromJson(jsonObj.get("tupleSchema").getAsJsonArray(),
                            (new TypeToken<Schema[]>() {
                            }).getType()));
                } else {
                    type.singleSchema = gson.fromJson(jsonObj.getAsJsonObject(), Schema.class);
                }
            }

            return type;
        }

        public void setGson(Gson gson) {
            this.gson = gson;
        }

    }
}

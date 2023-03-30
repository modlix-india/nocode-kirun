package com.fincity.nocode.kirun.engine.json.schema.object;

import java.io.IOException;
import java.io.Serializable;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AdditionalType implements Serializable {

    private static final long serialVersionUID = -3710026689972221380L;

    private Boolean booleanValue;
    private Schema schemaValue;

    public AdditionalType(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public AdditionalType(AdditionalType props) {

        this.booleanValue = props.booleanValue;
        this.schemaValue = new Schema(props.schemaValue);
    }

    public static class AdditionalTypeAdapter extends TypeAdapter<AdditionalType> {

        @Override
        public void write(JsonWriter out, AdditionalType value) throws IOException {
            if (value == null)
                return;
            if (value.getBooleanValue() != null) {
                out.value(value.getBooleanValue());
            } else if (value.getSchemaValue() != null) {
                out.value(value.getSchemaValue().toString());
            }
        }

        @Override
        public AdditionalType read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            AdditionalType additionalType = new AdditionalType();

            if (token == JsonToken.BOOLEAN)
                additionalType.booleanValue = in.nextBoolean();

            else if (token == JsonToken.BEGIN_OBJECT) {
                in.beginObject();
                String content = in.nextString();
                if (content.indexOf("booleanValue") != -1) {
                    String[] boolString = content.split(":");
                    additionalType.booleanValue = Boolean.valueOf(boolString[1].trim());
                } else if (content.indexOf("schemaValue") != -1) {
                    String[] schemaString = content.split(":");
                    additionalType.schemaValue = new Schema()
                            .setType(Type.of(SchemaType.valueOf(schemaString[1].trim())));
                }
                in.endObject();
            }

            return additionalType;
        }

    }
}

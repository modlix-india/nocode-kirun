package com.fincity.nocode.kirun.engine.json.schema.object;

import java.io.IOException;
import java.io.Serializable;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

	public static boolean canHaveAddtionalItems(AdditionalType at) {

		if (at == null)
			return true;
		
		if (at.booleanValue != null)
			return at.booleanValue.booleanValue();
		
		return at.schemaValue != null;
	}

	public static class AdditionalTypeAdapter extends TypeAdapter<AdditionalType> {

		private Gson gson;

		@Override
		public void write(JsonWriter out, AdditionalType value) throws IOException {
			if (value == null) {
				out.nullValue();
				return;
			}

			if (value.getBooleanValue() != null) {
				out.value(value.getBooleanValue());
			} else if (value.getSchemaValue() != null) {

				TypeAdapter<Schema> typeAdapter = gson.getAdapter(Schema.class);
				typeAdapter.write(out, value.schemaValue);
			}
		}

		@Override
		public AdditionalType read(JsonReader in) throws IOException {
			JsonToken token = in.peek();
			AdditionalType additionalType = new AdditionalType();

			if (token == JsonToken.BOOLEAN)
				additionalType.booleanValue = in.nextBoolean();

			else if (token == JsonToken.BEGIN_OBJECT) {
				JsonObject jsonObj = gson.fromJson(in, JsonObject.class);

				if (jsonObj.has("booleanValue")) {
					additionalType.booleanValue = jsonObj.get("booleanValue")
					        .getAsBoolean();
				} else if (jsonObj.has("schemaValue")) {
					additionalType.schemaValue = gson.fromJson(jsonObj.get("schemaValue")
					        .getAsJsonObject(), Schema.class);
				} else {
					additionalType.schemaValue = gson.fromJson(jsonObj.getAsJsonObject(), Schema.class);
				}
			}

			return additionalType;
		}

		public void setGson(Gson gson) {
			this.gson = gson;
		}

	}
}

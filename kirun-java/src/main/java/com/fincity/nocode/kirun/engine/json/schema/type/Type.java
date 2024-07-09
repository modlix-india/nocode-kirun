package com.fincity.nocode.kirun.engine.json.schema.type;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public abstract class Type implements Serializable {

	private static final long serialVersionUID = -6746822297269031219L;

	public abstract Set<SchemaType> getAllowedSchemaTypes();

	public static Type of(SchemaType... types) {

		if (types.length == 1)
			return new SingleType(types[0]);

		return new MultipleType().setType(Set.of(types));
	}

	public boolean contains(SchemaType type) {

		if (this instanceof SingleType st) {
			return st.getType() == type;
		}

		return ((MultipleType) this).getAllowedSchemaTypes()
				.contains(type);
	}

	public static class SchemaTypeAdapter extends TypeAdapter<Type> {

		private static String fromJSONType(String jtype) {

			String newType = jtype.toUpperCase();

			if (newType.equals("NUMBER"))
				return "DOUBLE";

			return newType;
		}

		@Override
		public void write(JsonWriter out, Type value) throws IOException {

			if (value == null) {
				out.nullValue();
				return;
			}

			if (value instanceof MultipleType multipleType) {
				out.beginArray();
				for (SchemaType typ : multipleType.getAllowedSchemaTypes()) {
					out.value(typ.toString());
				}
				out.endArray();
			} else {
				out.value(((SingleType) value).getType()
						.toString());
			}
		}

		@Override
		public Type read(JsonReader in) throws IOException {

			JsonToken token = in.peek();
			Type t = null;

			if (token == JsonToken.STRING)
				t = of(SchemaType.valueOf(fromJSONType(in.nextString())));
			else if (token == JsonToken.BEGIN_ARRAY) {
				in.beginArray();
				Set<SchemaType> types = new HashSet<>();
				while (in.hasNext()) {
					types.add(SchemaType.valueOf(fromJSONType(in.nextString())));
				}
				in.endArray();
				t = new MultipleType().setType(types);
			}

			return t;
		}
	}

}

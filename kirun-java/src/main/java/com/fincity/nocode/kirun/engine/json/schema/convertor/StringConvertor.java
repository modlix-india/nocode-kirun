package com.fincity.nocode.kirun.engine.json.schema.convertor;

import static com.fincity.nocode.kirun.engine.util.json.ConvertorUtil.handleUnConvertibleValue;

import java.util.List;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.convertor.enums.ConversionMode;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public class StringConvertor {

	public static JsonElement convert(List<Schema> parents, Schema schema, ConversionMode mode, JsonElement element) {

		if (element == null || element.isJsonNull())
			return handleUnConvertibleValue(parents, mode, element, getDefault(schema),
					"Expected a string but found null");

		String value = element.isJsonPrimitive() ? element.getAsJsonPrimitive().getAsString()
				: element.toString();

		return getConvertedString(value, mode);
	}

	private static JsonPrimitive getConvertedString(String value, ConversionMode mode) {
		if (mode == ConversionMode.STRICT)
			return new JsonPrimitive(value);

		return new JsonPrimitive(value.trim());
	}

	private static JsonElement getDefault(Schema schema) {
		return schema.getDefaultValue() != null ? schema.getDefaultValue() : JsonNull.INSTANCE;
	}

	private StringConvertor() {
	}
}

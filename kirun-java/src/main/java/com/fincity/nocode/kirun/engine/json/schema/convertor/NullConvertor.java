package com.fincity.nocode.kirun.engine.json.schema.convertor;

import static com.fincity.nocode.kirun.engine.util.json.ConvertorUtil.handleUnConvertibleValue;

import java.util.List;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.convertor.enums.ConversionMode;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public class NullConvertor {

	public static JsonElement convert(List<Schema> parents, Schema schema, ConversionMode mode, JsonElement element) {

		if (element == null || element.isJsonNull())
			return JsonNull.INSTANCE;

		if (element.isJsonPrimitive()) {
			JsonPrimitive primitive = element.getAsJsonPrimitive();
			if (primitive.isString() && primitive.getAsString().equalsIgnoreCase("null"))
				return JsonNull.INSTANCE;
		}

		return handleUnConvertibleValue(parents, mode, element, JsonNull.INSTANCE, "Unable to convert to null");
	}

	private NullConvertor() {
	}
}

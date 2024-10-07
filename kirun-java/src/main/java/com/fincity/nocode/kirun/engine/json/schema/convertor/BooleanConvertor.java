package com.fincity.nocode.kirun.engine.json.schema.convertor;

import static com.fincity.nocode.kirun.engine.util.json.ConvertorUtil.handleUnConvertibleValue;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.convertor.enums.ConversionMode;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanConvertor {

	private static final Map<String, Boolean> BOOLEAN_MAP = Map.of(
			"true", true, "t", true, "yes", true, "y", true, "1", true,
			"false", false, "f", false, "no", false, "n", false, "0", false);

	public static JsonElement convert(List<Schema> parents, Schema schema, ConversionMode mode, JsonElement element) {

		if (element == null || element.isJsonNull())
			return handleUnConvertibleValue(parents, mode, element, getDefault(schema),
					"Expected a boolean but found null");

		JsonPrimitive primitive = getBooleanPrimitive(element);

		return primitive != null ? primitive
				: handleUnConvertibleValue(parents, mode, element, getDefault(schema), "Unable to convert to boolean");
	}

	private static JsonPrimitive getBooleanPrimitive(JsonElement element) {

		if (!element.isJsonPrimitive()) {
			return null;
		}

		JsonPrimitive primitive = element.getAsJsonPrimitive();

		if (primitive.isBoolean()) {
			return primitive;
		} else if (primitive.isString()) {
			return handleStringValue(primitive);
		} else if (primitive.isNumber()) {
			return handleNumberValue(primitive);
		}
		return null;
	}

	private static JsonPrimitive handleStringValue(JsonPrimitive primitive) {

		String value = primitive.getAsString().toLowerCase().trim();

		Boolean result = BOOLEAN_MAP.getOrDefault(value, null);

		return (result != null) ? new JsonPrimitive(result) : null;
	}

	private static JsonPrimitive handleNumberValue(JsonPrimitive primitive) {
		double number = primitive.getAsDouble();
		return (number == 0 || number == 1) ? new JsonPrimitive(number == 1) : null;
	}

	private static JsonElement getDefault(Schema schema) {
		return schema.getDefaultValue() != null ? schema.getDefaultValue() : new JsonPrimitive(false);
	}

	private BooleanConvertor() {
	}
}

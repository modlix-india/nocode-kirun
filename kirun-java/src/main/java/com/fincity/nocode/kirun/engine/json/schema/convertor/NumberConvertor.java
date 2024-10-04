package com.fincity.nocode.kirun.engine.json.schema.convertor;

import static com.fincity.nocode.kirun.engine.util.json.ConvertorUtil.handleUnConvertibleValue;

import java.util.List;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.convertor.enums.ConversionMode;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public class NumberConvertor {

	private static final String NOT_A = " is not a ";

	public static JsonElement convert(List<Schema> parents, SchemaType type, Schema schema, ConversionMode mode,
			JsonElement element) {

		if (element == null || element.isJsonNull())
			return handleUnConvertibleValue(parents, mode, element, getDefaultValue(schema),
					"Expected a Number but found null");

		if (!element.isJsonPrimitive()) {
			return handleUnConvertibleValue(parents, mode, element, getDefaultValue(schema),
					element + NOT_A + type.getPrintableName());
		}

		JsonPrimitive jp = element.getAsJsonPrimitive();

		if (jp.isString())
			try {
				jp = new JsonPrimitive(Double.parseDouble(jp.getAsString()));
			} catch (NumberFormatException exception) {
				return handleUnConvertibleValue(parents, mode, element, getDefaultValue(schema),
						element + NOT_A + type.getPrintableName());
			}

		Number number = extractNumber(type, jp, mode);

		if (number == null) {
			return handleUnConvertibleValue(parents, mode, element, getDefaultValue(schema),
					element + NOT_A + type.getPrintableName());
		}

		return new JsonPrimitive(number);
	}

	private static Number extractNumber(SchemaType schemaType, JsonPrimitive jp, ConversionMode mode) {

		if (!jp.isNumber())
			return null;

		try {
			return switch (schemaType) {
				case INTEGER -> isInteger(jp, mode) ? jp.getAsInt() : null;
				case LONG -> isLong(jp, mode) ? jp.getAsLong() : null;
				case DOUBLE -> jp.getAsDouble();
				case FLOAT -> isFloat(jp, mode) ? jp.getAsFloat() : null;
				default -> null;
			};
		} catch (NumberFormatException numberFormatException) {
			return null;
		}
	}

	private static boolean isInteger(JsonPrimitive jp, ConversionMode mode) {
		if (mode != ConversionMode.STRICT) {
			return jp.isNumber();
		}

		return jp.isNumber() && jp.getAsDouble() % 1 == 0;
	}

	private static boolean isLong(JsonPrimitive jp, ConversionMode mode) {
		if (mode != ConversionMode.STRICT) {
			return jp.isNumber();
		}

		double value = jp.getAsDouble();

		return value % 1 == 0 && value >= Long.MIN_VALUE && value <= Long.MAX_VALUE;
	}

	private static boolean isFloat(JsonPrimitive jp, ConversionMode mode) {
		if (mode != ConversionMode.STRICT) {
			return jp.isNumber();
		}

		double value = jp.getAsDouble();

		return value >= -Float.MAX_VALUE && value <= Float.MAX_VALUE;
	}

	private static JsonElement getDefaultValue(Schema schema) {
		try {
			Number value = schema.getDefaultValue().getAsNumber();
			return new JsonPrimitive(value);
		} catch (Exception e) {
			return JsonNull.INSTANCE;
		}
	}

	private NumberConvertor() {
	}
}

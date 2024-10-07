package com.fincity.nocode.kirun.engine.util.json;

import static com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator.path;

import java.util.List;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.convertor.enums.ConversionMode;
import com.fincity.nocode.kirun.engine.json.schema.convertor.exception.SchemaConversionException;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public class ConvertorUtil {

	public static JsonElement handleUnConvertibleValue(List<Schema> parents, ConversionMode mode, JsonElement element,
			String errorMessage) {
		return handleUnConvertibleValue(parents, mode, element, JsonNull.INSTANCE, errorMessage);
	}

	public static JsonElement handleUnConvertibleValue(List<Schema> parents, ConversionMode mode, JsonElement element,
			JsonElement defaultValue, String errorMessage) {

		if (mode == null)
			mode = ConversionMode.STRICT;

		return switch (mode) {
			case STRICT -> throw new SchemaConversionException(path(parents), element, mode, errorMessage);
			case LENIENT -> JsonNull.INSTANCE;
			case USE_DEFAULT -> defaultValue;
			case SKIP -> element;
		};
	}

	private ConvertorUtil() {
	}
}

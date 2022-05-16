package com.fincity.nocode.kirun.engine.json.schema.validator;

import static com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator.path;

import java.util.List;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanValidator {

	public static void validate(List<String> parents, Schema schema, JsonElement element) {

		if (element == null || element.isJsonNull())
			throw new SchemaValidationException(path(parents, schema.getName()), "Expected a boolean but found null");

		if (!element.isJsonPrimitive() || !((JsonPrimitive) element).isBoolean())
			throw new SchemaValidationException(path(parents, schema.getName()),
			        element.toString() + " is not a boolean");
	}

	private BooleanValidator() {
	}
}

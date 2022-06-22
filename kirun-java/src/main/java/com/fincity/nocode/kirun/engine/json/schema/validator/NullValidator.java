package com.fincity.nocode.kirun.engine.json.schema.validator;

import static com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator.path;

import java.util.List;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.google.gson.JsonElement;

public class NullValidator {

	public static JsonElement validate(List<String> parents, Schema schema, JsonElement element) {

		if (element != null && !element.isJsonNull())
			throw new SchemaValidationException(path(parents, schema.getName()),
			        "Expected a null but found " + element);
		
		return element;
	}

	private NullValidator() {
	}
}

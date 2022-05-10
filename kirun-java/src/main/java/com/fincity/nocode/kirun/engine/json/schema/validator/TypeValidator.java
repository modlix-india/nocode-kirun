package com.fincity.nocode.kirun.engine.json.schema.validator;

import static com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator.path;

import java.util.List;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.google.gson.JsonElement;

public class TypeValidator {

	public static void validate(List<String> parents, SchemaType type, Schema schema, Repository<Schema> repository,
			JsonElement element) {

		if (type == SchemaType.STRING) {
			StringValidator.validate(parents, schema, element);
		} else if (type == SchemaType.LONG || type == SchemaType.INTEGER || type == SchemaType.DOUBLE
				|| type == SchemaType.FLOAT) {
			NumberValidator.validate(type, parents, schema, element);
		} else if (type == SchemaType.BOOLEAN) {
			BooleanValidator.validate(parents, schema, element);
		} else if (type == SchemaType.OBJECT) {
			ObjectValidator.validate(parents, schema, repository, element);
		} else if (type == SchemaType.ARRAY) {
			ArrayValidator.validate(parents, schema, repository, element);
		} else if (type == SchemaType.NULL) {
			NullValidator.validate(parents, schema, element);
		} else {

			throw new SchemaValidationException(path(parents, schema.getName()), type + " is not a valid type.");
		}
	}

	private TypeValidator() {
	}
}

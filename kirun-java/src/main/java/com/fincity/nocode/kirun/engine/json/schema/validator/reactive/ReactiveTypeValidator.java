package com.fincity.nocode.kirun.engine.json.schema.validator.reactive;

import static com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator.path;

import java.util.List;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.validator.BooleanValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.NullValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.NumberValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.StringValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import reactor.core.publisher.Mono;

public class ReactiveTypeValidator {

	public static Mono<JsonElement> validate(List<Schema> parents, SchemaType type, Schema schema,
	        ReactiveRepository<Schema> repository, JsonElement element) {

		if (type == SchemaType.OBJECT) {

			return ReactiveObjectValidator.validate(parents, schema, repository, element);
		} else if (type == SchemaType.ARRAY) {

			return ReactiveArrayValidator.validate(parents, schema, repository, element);
		}

		if (type == SchemaType.STRING) {
			try {

				StringValidator.validate(parents, schema, element);
				return Mono.just(element);
			} catch (SchemaValidationException sve) {

				return Mono.error(sve);
			}
		} else if (type == SchemaType.LONG || type == SchemaType.INTEGER || type == SchemaType.DOUBLE
		        || type == SchemaType.FLOAT) {
			try {

				NumberValidator.validate(type, parents, schema, element);
				return Mono.just(element);
			} catch (SchemaValidationException sve) {

				return Mono.error(sve);
			}
		} else if (type == SchemaType.BOOLEAN) {
			try {

				BooleanValidator.validate(parents, schema, element);
				return Mono.just(element);
			} catch (SchemaValidationException sve) {

				return Mono.error(sve);
			}
		} else if (type == SchemaType.NULL) {
			try {

				NullValidator.validate(parents, schema, element);
				return Mono.just(JsonNull.INSTANCE);
			} catch (SchemaValidationException sve) {

				return Mono.error(sve);
			}
		}

		return Mono.error(() -> new SchemaValidationException(path(parents), type + " is not a valid type."));
	}

	private ReactiveTypeValidator() {
	}
}

package com.fincity.nocode.kirun.engine.json.schema.validator.reactive;

import static com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator.path;

import java.util.List;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.convertor.BooleanConvertor;
import com.fincity.nocode.kirun.engine.json.schema.convertor.NullConvertor;
import com.fincity.nocode.kirun.engine.json.schema.convertor.NumberConvertor;
import com.fincity.nocode.kirun.engine.json.schema.convertor.StringConvertor;
import com.fincity.nocode.kirun.engine.json.schema.convertor.enums.ConversionMode;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.validator.BooleanValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.NullValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.NumberValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.StringValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.util.json.ConvertorUtil;
import com.fincity.nocode.kirun.engine.util.json.ValidatorUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import reactor.core.publisher.Mono;

public class ReactiveTypeValidator {

	public static Mono<JsonElement> validate(List<Schema> parents, SchemaType type, Schema schema,
			ReactiveRepository<Schema> repository, JsonElement element) {

		return validateType(parents, type, schema, repository, element, false, null);
	}

	public static Mono<JsonElement> validate(List<Schema> parents, SchemaType type, Schema schema,
			ReactiveRepository<Schema> repository, JsonElement element, boolean convert, ConversionMode mode) {

		return validateType(parents, type, schema, repository, element, convert, mode);
	}

	private static Mono<JsonElement> validateType(List<Schema> parents, SchemaType type, Schema schema,
			ReactiveRepository<Schema> repository, JsonElement element, boolean convert, ConversionMode mode) {

		if (type == SchemaType.OBJECT) {
			return ReactiveObjectValidator.validate(parents, schema, repository, element, convert, mode);
		} else if (type == SchemaType.ARRAY) {
			return ReactiveArrayValidator.validate(parents, schema, repository, element, convert, mode);
		}

		return handleTypeValidationAndConversion(parents, type, schema, element, convert, mode);
	}

	private static Mono<JsonElement> handleTypeValidationAndConversion(List<Schema> parents, SchemaType type,
			Schema schema, JsonElement element, boolean convert, ConversionMode mode) {

		JsonElement cElement = convert ? convertElement(parents, type, schema, element, mode) : element;

		return Mono.fromCallable(() -> validateElement(parents, type, schema, cElement, mode))
				.onErrorMap(e -> e instanceof SchemaValidationException ? e
						: new SchemaValidationException(path(parents), e));
	}

	private static JsonElement convertElement(List<Schema> parents, SchemaType type, Schema schema,
			JsonElement element, ConversionMode mode) {

		if (type == null)
			return ConvertorUtil.handleUnConvertibleValue(parents, mode, element,
					schema.getDefaultValue() != null ? schema.getDefaultValue() : element,
					type + " is not a valid type for conversion.");

		if (mode == null)
			mode = ConversionMode.STRICT;

		return switch (type) {
			case STRING -> StringConvertor.convert(parents, schema, mode, element);
			case LONG, INTEGER, DOUBLE, FLOAT -> NumberConvertor.convert(parents, type, schema, mode, element);
			case BOOLEAN -> BooleanConvertor.convert(parents, schema, mode, element);
			case NULL -> NullConvertor.convert(parents, schema, mode, element);
			default -> ConvertorUtil.handleUnConvertibleValue(parents, mode, element,
					schema.getDefaultValue() != null ? schema.getDefaultValue() : element,
					type + " is not a valid type for conversion.");
		};
	}

	private static JsonElement validateElement(List<Schema> parents, SchemaType type, Schema schema,
			JsonElement element, ConversionMode mode) {

		if (type == null)
			return ValidatorUtil.handleValidationError(parents, mode, element,
					schema.getDefaultValue() != null ? schema.getDefaultValue() : JsonNull.INSTANCE,
					type + " is not a valid type.");

		return switch (type) {
			case STRING -> StringValidator.validate(parents, schema, element);
			case LONG, INTEGER, DOUBLE, FLOAT -> NumberValidator.validate(type, parents, schema, element);
			case BOOLEAN -> BooleanValidator.validate(parents, schema, element);
			case NULL -> NullValidator.validate(parents, schema, element);
			default -> ValidatorUtil.handleValidationError(parents, mode, element,
					schema.getDefaultValue() != null ? schema.getDefaultValue() : JsonNull.INSTANCE,
					type + " is not a valid type.");
		};
	}

	private ReactiveTypeValidator() {
	}
}

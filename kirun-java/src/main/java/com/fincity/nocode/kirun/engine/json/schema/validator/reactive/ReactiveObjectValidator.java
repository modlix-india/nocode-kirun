package com.fincity.nocode.kirun.engine.json.schema.validator.reactive;

import static com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator.path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.convertor.enums.ConversionMode;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.reactive.ReactiveSchemaUtil;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class ReactiveObjectValidator {

	public static Mono<JsonElement> validate(List<Schema> parents, Schema schema, ReactiveRepository<Schema> repository,
			JsonElement element) {

		return validateObject(parents, schema, repository, element, false, null);
	}

	public static Mono<JsonElement> validate(List<Schema> parents, Schema schema, ReactiveRepository<Schema> repository,
			JsonElement element, boolean convert, ConversionMode mode) {

		return validateObject(parents, schema, repository, element, convert, mode);
	}

	private static Mono<JsonElement> validateObject(List<Schema> parents, Schema schema,
			ReactiveRepository<Schema> repository,
			JsonElement element, boolean convert, ConversionMode mode) {

		if (element == null || element.isJsonNull())
			return Mono.error(() -> new SchemaValidationException(path(parents), "Expected an object but found null"));

		if (!element.isJsonObject())
			return Mono.error(
					() -> new SchemaValidationException(path(parents), element + " is not an Object"));

		final JsonObject jsonObject = (JsonObject) element;
		Set<String> keys = new HashSet<>(jsonObject.keySet());

		try {
			checkMinMaxProperties(parents, schema, keys);
		} catch (SchemaValidationException sve) {
			return Mono.error(sve);
		}

		if (schema.getRequired() != null) {
			try {
				checkRequired(parents, schema, jsonObject);
			} catch (SchemaValidationException sve) {
				return Mono.error(sve);
			}
		}

		return checkPropertyNameSchema(parents, schema, repository, keys, jsonObject)
				.flatMap(e -> checkProperties(parents, schema, repository, e, convert, mode))
				.flatMap(tuple -> checkPatternProperties(parents, schema, repository, tuple))
				.flatMap(tuple -> checkAdditionalProperties(parents, schema, repository, tuple))
				.map(Tuple2::getT1);
	}

	private static Mono<JsonObject> checkPropertyNameSchema(List<Schema> parents, Schema schema,
			ReactiveRepository<Schema> repository, Set<String> keys, JsonObject element) {

		if (schema.getPropertyNames() == null)
			return Mono.just(element);

		return Flux.fromIterable(keys)
				.flatMap(key -> ReactiveSchemaValidator
						.validate(parents, schema.getPropertyNames(), repository, new JsonPrimitive(key))
						.onErrorMap(e -> new SchemaValidationException(path(parents),
								"Property name '" + key + "' does not fit the property schema")))
				.collectList()
				.map(x -> element);

	}

	private static Mono<Tuple2<JsonObject, Set<String>>> checkAdditionalProperties(List<Schema> parents, Schema schema,
			ReactiveRepository<Schema> repository, Tuple2<JsonObject, Set<String>> inTup) {

		if (schema.getAdditionalProperties() == null) {
			return Mono.just(inTup);
		}

		AdditionalType apt = schema.getAdditionalProperties();

		if (apt.getBooleanValue() != null) {

			if (!apt.getBooleanValue()
					.booleanValue()
					&& !inTup.getT2()
							.isEmpty()) {
				return Mono.error(() -> new SchemaValidationException(path(parents), inTup.getT2()
						+ " are additional properties which are not allowed."));
			}
		} else if (apt.getSchemaValue() != null) {

			JsonObject job = inTup.getT1();

			return Flux.fromIterable(inTup.getT2())
					.flatMap(key -> {

						List<Schema> newParents = new ArrayList<>(parents == null ? List.of() : parents);
						return ReactiveSchemaValidator
								.validate(newParents, apt.getSchemaValue(), repository, job.get(key))
								.map(v -> Tuples.of(key, v));
					})
					.collectList()
					.map(lst -> adjustRemaining(inTup, lst));
		}

		return Mono.just(inTup);
	}

	private static Mono<Tuple2<JsonObject, Set<String>>> checkPatternProperties(List<Schema> parents, Schema schema,
			ReactiveRepository<Schema> repository, Tuple2<JsonObject, Set<String>> inTup) {

		if (schema.getPatternProperties() == null || schema.getPatternProperties()
				.isEmpty()) {
			return Mono.just(inTup);
		}

		return Flux.fromIterable(schema.getPatternProperties()
				.keySet())
				.map(e -> Tuples.of(e, Pattern.compile(e)))
				.collectMap(Tuple2::getT1, Tuple2::getT2)
				.flatMap(compiledPatterns -> Flux.fromIterable(inTup.getT2())
						.flatMap(key -> {

							List<Schema> newParents = new ArrayList<>(parents == null ? List.of() : parents);

							return Flux.fromIterable(compiledPatterns.entrySet())
									.filter(e -> e.getValue()
											.matcher(key)
											.matches())
									.flatMap(e -> ReactiveSchemaValidator
											.validate(newParents, schema.getPatternProperties()
													.get(e.getKey()), repository,
													inTup.getT1()
															.get(key))
											.map(v -> Tuples.of(key, v)))
									.take(1);
						})
						.collectList()
						.map(lst -> adjustRemaining(inTup, lst)));
	}

	private static Tuple2<JsonObject, Set<String>> adjustRemaining(Tuple2<JsonObject, Set<String>> inTup,
			List<Tuple2<String, JsonElement>> lst) {
		Set<String> existingKeys = new HashSet<>(inTup.getT2());
		JsonObject job = inTup.getT1();

		for (var each : lst) {
			existingKeys.remove(each.getT1());
			job.add(each.getT1(), each.getT2());
		}

		return Tuples.of(job, existingKeys);
	}

	private static Mono<Tuple2<JsonObject, Set<String>>> checkProperties(List<Schema> parents, Schema schema,
			ReactiveRepository<Schema> repository, JsonObject jsonObject, boolean convert, ConversionMode mode) {

		if (schema.getProperties() == null) {
			return Mono.just(Tuples.of(jsonObject, jsonObject.keySet()));
		}

		return Flux.fromIterable(schema.getProperties()
				.entrySet())
				.flatMap(each -> {

					JsonElement value = jsonObject.get(each.getKey());

					List<Schema> newParents = new ArrayList<>(parents == null ? List.of() : parents);

					if (!jsonObject.has(each.getKey()) && (value == null || value.isJsonNull())) {

						return ReactiveSchemaUtil.getDefaultValue(each.getValue(), repository)
								.flatMap(defValue -> {
									if (defValue == null || defValue.isJsonNull())
										return Mono.empty();

									return ReactiveSchemaValidator
											.validate(newParents, each.getValue(), repository, value, convert, mode)
											.map(v -> Tuples.of(each.getKey(), Optional.of(v)));
								})
								.switchIfEmpty(Mono.just(Tuples.of(each.getKey(), Optional.<JsonElement>empty())));
					}

					return ReactiveSchemaValidator
							.validate(newParents, each.getValue(), repository, value, convert, mode)
							.map(v -> Tuples.of(each.getKey(), Optional.of(v)));
				})
				.collectList()
				.map(tupList -> adjustRemainingWithOptional(jsonObject, tupList));

	}

	private static Tuple2<JsonObject, Set<String>> adjustRemainingWithOptional(JsonObject jsonObject,
			List<Tuple2<String, Optional<JsonElement>>> tupList) {
		Set<String> keys = new HashSet<>(jsonObject.keySet());

		for (var tup : tupList) {
			var op = tup.getT2();
			if (op.isEmpty())
				continue;
			jsonObject.add(tup.getT1(), op.get());
			keys.remove(tup.getT1());
		}

		return Tuples.of(jsonObject, keys);
	}

	public static void checkRequired(List<Schema> parents, Schema schema, JsonObject jsonObject) {
		for (String key : schema.getRequired()) {
			if (jsonObject.get(key) == null || jsonObject.get(key).isJsonNull()) {
				throw new SchemaValidationException(path(parents), key + " is mandatory");
			}
		}
	}

	public static void checkMinMaxProperties(List<Schema> parents, Schema schema, Set<String> keys) {
		if (schema.getMinProperties() != null && keys.size() < schema.getMinProperties()) {
			throw new SchemaValidationException(path(parents),
					"Object should have minimum of " + schema.getMinProperties() + " properties");
		}

		if (schema.getMaxProperties() != null && keys.size() > schema.getMaxProperties()) {
			throw new SchemaValidationException(path(parents),
					"Object can have maximum of " + schema.getMaxProperties() + " properties");
		}
	}

	private ReactiveObjectValidator() {
	}

}

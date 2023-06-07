package com.fincity.nocode.kirun.engine.json.schema.validator.reactive;

import static com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.reactive.ReactiveSchemaUtil;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class ReactiveSchemaValidator {

	public static Mono<JsonElement> validate(List<Schema> oldParents, Schema schema,
	        ReactiveRepository<Schema> repository, JsonElement element) {

		if (schema == null) {
			return Mono.error(() -> new SchemaValidationException(path(oldParents), "No schema found to validate"));
		}

		final List<Schema> parents = oldParents == null ? new ArrayList<>() : oldParents;
		parents.add(schema);

		if ((element == null || element.isJsonNull()) && schema.getDefaultValue() != null) {
			return Mono.just(schema.getDefaultValue()
			        .deepCopy());
		}

		if (schema.getConstant() != null) {
			return Mono.fromCallable(() -> SchemaValidator.constantValidation(parents, schema, element));
		}

		if (schema.getEnums() != null && !schema.getEnums()
		        .isEmpty()) {
			return Mono.fromCallable(() -> SchemaValidator.enumCheck(parents, schema, element));
		}

		if (schema.getFormat() != null && schema.getType() == null) {
			return Mono.error(() -> new SchemaValidationException(path(parents),
			        "Type is missing in schema for declared " + schema.getFormat()
			                .toString() + " format."));
		}

		return typeValidation(parents, schema, repository, element).flatMap(typValiElement -> {

			if (schema.getRef() != null && !schema.getRef()
			        .isBlank()) {
				return ReactiveSchemaUtil.getSchemaFromRef(parents.get(0), repository, schema.getRef())
				        .flatMap(refSchema -> validate(parents, refSchema, repository, typValiElement));
			}

			return ReactiveAnyOfAllOfOneOfValidator.validate(parents, schema, repository, typValiElement)
			        .flatMap(aoaoElement -> notValidation(parents, schema, repository, aoaoElement));
		});

	}

	public static Mono<JsonElement> notValidation(List<Schema> parents, Schema schema,
	        ReactiveRepository<Schema> repository, JsonElement element) {

		if (schema.getNot() == null)
			return Mono.just(element);

		return validate(parents, schema.getNot(), repository, element)

		        .map(e -> Optional.<Throwable>empty())

		        .onErrorResume(t -> Mono.just(Optional.of(t)))

		        .flatMap(op -> op.isPresent() ?

		                Mono.just(element) :

		                Mono.error(new SchemaValidationException(path(parents),
		                        "Schema validated value in not condition.")));
	}

	public static Mono<JsonElement> typeValidation(List<Schema> parents, Schema schema,
	        ReactiveRepository<Schema> repository, JsonElement element) {

		if (schema.getType() == null)
			return Mono.just(element);

		return Flux.fromIterable(schema.getType()
		        .getAllowedSchemaTypes())
		        .flatMap(type -> Mono.just(element)
		                .flatMap(el -> ReactiveTypeValidator.validate(parents, type, schema, repository, el)
		                        .map(e -> Tuples.of(el, Optional.<Throwable>empty()))
		                        .onErrorResume(sve -> Mono
		                                .just(Tuples.of(el == null ? JsonNull.INSTANCE : el, Optional.of(sve))))
		                        .onErrorStop()))

		        .takeUntil(e -> e.getT2()
		                .isEmpty())

		        .collectList()

		        .flatMap(lst ->
				{

			        if (lst.isEmpty())
				        return Mono.empty();

			        Tuple2<JsonElement, Optional<Throwable>> last = lst.get(lst.size() - 1);

			        if (last.getT2()
			                .isEmpty())
				        return Mono.just(last.getT1());

			        String parentPath = path(parents);

			        return Mono.error(() -> new SchemaValidationException(parentPath,
			                "Value " + element + " is not of valid type(s)", lst.stream()
			                        .map(Tuple2::getT2)
			                        .filter(Optional::isPresent)
			                        .map(Optional::get)
			                        .map(e -> e instanceof SchemaValidationException sve ? sve
			                                : new SchemaValidationException(parentPath, e))
			                        .toList()));
		        });
	}

	private ReactiveSchemaValidator() {
	}
}

package com.fincity.nocode.kirun.engine.json.schema.validator.reactive;

import static com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator.path;

import java.util.List;
import java.util.Optional;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class ReactiveAnyOfAllOfOneOfValidator {

	public static Mono<JsonElement> validate(List<Schema> parents, Schema schema, ReactiveRepository<Schema> repository,
	        JsonElement element) {

		if (schema.getOneOf() != null && !schema.getOneOf()
		        .isEmpty()) {
			return oneOf(parents, schema, repository, element);
		} else if (schema.getAllOf() != null && !schema.getAllOf()
		        .isEmpty()) {
			return allOf(parents, schema, repository, element);
		} else if (schema.getAnyOf() != null && !schema.getAnyOf()
		        .isEmpty()) {
			return anyOf(parents, schema, repository, element);
		}

		return Mono.just(element);
	}

	private static Mono<JsonElement> anyOf(List<Schema> parents, Schema schema, ReactiveRepository<Schema> repository,
	        JsonElement element) {

		return Flux.fromIterable(schema.getAnyOf())
				.flatMap(s -> Mono.just(s)
		                .flatMap(sch -> ReactiveSchemaValidator.validate(parents, sch, repository, element))
		                .map(e -> Tuples.of(e, Optional.<Throwable>empty()))
		                .onErrorResume(sve -> Mono.just(Tuples.of(element, Optional.of(sve)))))

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
			                "The value don't satisfy any of the schemas.", lst.stream()
			                        .map(Tuple2::getT2)
			                        .filter(Optional::isPresent)
			                        .map(Optional::get)
			                        .map(e -> e instanceof SchemaValidationException sve ? sve
			                                : new SchemaValidationException(parentPath, e))
			                        .toList()));
		        });
	}

	private static Mono<JsonElement> allOf(List<Schema> parents, Schema schema, ReactiveRepository<Schema> repository,
	        JsonElement element) {

		return Flux.fromIterable(schema.getAllOf())
				.flatMap(s -> Mono.just(s)
		                .flatMap(sch -> ReactiveSchemaValidator.validate(parents, sch, repository, element))
		                .map(e -> Tuples.of(e, Optional.<Throwable>empty()))
		                .onErrorResume(sve -> Mono.just(Tuples.of(element, Optional.of(sve)))))

		        .collectList()

		        .flatMap(lst ->
				{

			        if (lst.isEmpty())
				        return Mono.empty();

			        long count = lst.stream()
			                .filter(e -> e.getT2()
			                        .isEmpty())
			                .count();

			        if (count == schema.getAllOf()
			                .size())
				        return Mono.just(element);

			        String parentPath = path(parents);

			        throw new SchemaValidationException(parentPath, "The value don't satisfy some of the schemas.",
			                lst.stream()
			                        .map(Tuple2::getT2)
			                        .filter(Optional::isPresent)
			                        .map(Optional::get)
			                        .map(e -> e instanceof SchemaValidationException sve ? sve
			                                : new SchemaValidationException(parentPath, e))
			                        .toList());
		        });
	}

	private static Mono<JsonElement> oneOf(List<Schema> parents, Schema schema, ReactiveRepository<Schema> repository,
	        JsonElement element) {

		return Flux.fromIterable(schema.getOneOf())
		        .flatMap(s -> Mono.just(s)
		                .flatMap(sch -> ReactiveSchemaValidator.validate(parents, sch, repository, element))
		                .map(e -> Tuples.of(e, Optional.<Throwable>empty()))
		                .onErrorResume(sve -> Mono.just(Tuples.of(element, Optional.of(sve)))))
		        .collectList()

		        .flatMap(lst ->
				{

			        if (lst.isEmpty())
				        return Mono.empty();

			        long count = lst.stream()
			                .filter(e -> e.getT2()
			                        .isEmpty())
			                .count();

			        if (count == 1l)
				        return Mono.just(element);

			        String parentPath = path(parents);

			        throw new SchemaValidationException(parentPath,
			                (count == 0 ? "The value does not satisfy any schema"
			                        : "The value satisfy more than one schema"),
			                lst.stream()
			                        .map(Tuple2::getT2)
			                        .filter(Optional::isPresent)
			                        .map(Optional::get)
			                        .map(e -> e instanceof SchemaValidationException sve ? sve
			                                : new SchemaValidationException(parentPath, e))
			                        .toList());
		        });
	}

	private ReactiveAnyOfAllOfOneOfValidator() {
	}
}

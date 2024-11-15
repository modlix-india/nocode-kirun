package com.fincity.nocode.kirun.engine.function.system.math;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RandomRepository implements ReactiveRepository<ReactiveFunction> {

	private static final Map<String, ReactiveFunction> RANDOM_REPO = Map.ofEntries(
			Map.entry("Random", new Random()),
			Map.entry("RandomInt", new AbstractRandom("RandomInt", SchemaType.INTEGER)),
			Map.entry("RandomLong", new AbstractRandom("RandomLong", SchemaType.LONG)),
			Map.entry("RandomFloat", new AbstractRandom("RandomFloat", SchemaType.FLOAT)),
			Map.entry("RandomDouble", new AbstractRandom("RandomDouble", SchemaType.DOUBLE)));

	private static final List<String> FILTERABLE_NAMES = RANDOM_REPO.values()
			.stream()
			.map(ReactiveFunction::getSignature)
			.map(FunctionSignature::getFullName)
			.toList();

	@Override
	public Mono<ReactiveFunction> find(String namespace, String name) {
		if (!namespace.equals(Namespaces.MATH))
			return Mono.empty();
		return Mono.just(RANDOM_REPO.get(name));
	}

	@Override
	public Flux<String> filter(String name) {
		return Flux.fromIterable(FILTERABLE_NAMES)
				.filter(e -> e.toLowerCase()
						.indexOf(name.toLowerCase()) != -1);
	}
}

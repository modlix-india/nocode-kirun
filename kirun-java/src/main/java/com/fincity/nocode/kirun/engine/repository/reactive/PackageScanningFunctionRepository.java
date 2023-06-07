package com.fincity.nocode.kirun.engine.repository.reactive;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class PackageScanningFunctionRepository implements ReactiveRepository<ReactiveFunction> {

	private final Map<String, Map<String, ReactiveFunction>> map;
	private final List<String> filterable;

	public PackageScanningFunctionRepository(String packageName) {

		Reflections reflections = new Reflections(packageName, Scanners.SubTypes);
		map = reflections.getSubTypesOf(AbstractReactiveFunction.class)
		        .stream()
		        .map(e ->
				{
			        try {
				        return e.getConstructor()
				                .newInstance();
			        } catch (Exception ex) {
				        return null;
			        }
		        })
		        .filter(Objects::nonNull)
		        .map(ReactiveFunction.class::cast)
		        .collect(Collectors.groupingBy(e -> e.getSignature()
		                .getNamespace()))
		        .entrySet()
		        .stream()
		        .map(e -> Tuples.of(e.getKey(), e.getValue()
		                .stream()
		                .collect(Collectors.toMap(f -> f.getSignature()
		                        .getName(), java.util.function.Function.identity()))))
		        .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2));

		filterable = map.values()
		        .stream()
		        .map(Map::values)
		        .flatMap(Collection::stream)
		        .map(ReactiveFunction::getSignature)
		        .map(FunctionSignature::getFullName)
		        .toList();
	}

	@Override
	public Mono<ReactiveFunction> find(String namespace, String name) {

		if (!map.containsKey(namespace))
			return Mono.empty();

		return Mono.justOrEmpty(map.get(namespace)
		        .get(name));
	}

	@Override
	public Flux<String> filter(String name) {

		String lowerCaseName = name.toLowerCase();

		return Flux.fromIterable(this.filterable)
		        .filter(e -> e.toLowerCase()
		                .contains(lowerCaseName));
	}
}

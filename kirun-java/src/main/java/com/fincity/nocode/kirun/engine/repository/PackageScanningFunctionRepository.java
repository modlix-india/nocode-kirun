package com.fincity.nocode.kirun.engine.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class PackageScanningFunctionRepository implements Repository<Function> {

	private final Map<String, Map<String, Function>> map;
	private final List<String> filterable;

	public PackageScanningFunctionRepository(String packageName) {

		Reflections reflections = new Reflections(packageName, Scanners.SubTypes);
		map = reflections.getSubTypesOf(AbstractFunction.class)
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
		        .map(Function.class::cast)
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
		        .map(Function::getSignature)
		        .map(FunctionSignature::getFullName)
		        .toList();
	}

	@Override
	public Function find(String namespace, String name) {

		if (!map.containsKey(namespace))
			return null;

		return map.get(namespace)
		        .get(name);
	}

	@Override
	public List<String> filter(String name) {

		String lowerCaseName = name.toLowerCase();

		return this.filterable.stream()
		        .filter(e -> e.toLowerCase()
		                .contains(lowerCaseName))
		        .toList();
	}
}

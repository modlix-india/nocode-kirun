package com.fincity.nocode.kirun.engine.repository;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.function.Function;

public class PackageScanningFunctionRepository implements Repository<Function> {

	private final Map<String, Function> map;

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
		        .collect(Collectors.toMap(e -> e.getSignature()
		                .getFullName(), java.util.function.Function.identity()));
	}

	@Override
	public Function find(String name) {

		return map.get(name);
	}
}

package com.fincity.nocode.kirun.engine.function.system.math;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

public class RandomRepository implements Repository<Function> {

	private static final Map<String, AbstractRandom> RANDOM_REPO = Map.ofEntries(
			Map.entry("RandomInteger", new AbstractRandom("RandomInteger", SchemaType.INTEGER)),
			Map.entry("RandomLong", new AbstractRandom("RandomLong", SchemaType.LONG)),
			Map.entry("RandomFloat", new AbstractRandom("RandomFloat", SchemaType.FLOAT)),
			Map.entry("RandomDouble", new AbstractRandom("RandomDouble", SchemaType.DOUBLE)));
	
	private static final List<String> FILTERABLE_NAMES = RANDOM_REPO.values()
	        .stream()
	        .map(Function::getSignature)
	        .map(FunctionSignature::getFullName)
	        .toList();

	@Override
	public AbstractRandom find(String namespace, String name) {
		if (!namespace.equals(Namespaces.MATH))
			return null;
		return RANDOM_REPO.get(name);
	}

	@Override
	public List<String> filter(String name) {
		return FILTERABLE_NAMES.stream()
		        .filter(e -> e.toLowerCase()
		                .indexOf(name.toLowerCase()) != -1)
		        .toList();
	}
}

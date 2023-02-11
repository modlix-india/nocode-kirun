package com.fincity.nocode.kirun.engine.function.system.math;

import java.util.Map;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

public class RandomRepository implements Repository<Function> {

	private static final Map<String, AbstractRandom> RANDOM_REPO = Map.ofEntries(
			Map.entry("RandomInteger", new AbstractRandom("RandomInteger", SchemaType.INTEGER)),
			Map.entry("RandomLong", new AbstractRandom("RandomLong", SchemaType.LONG)),
			Map.entry("RandomFloat", new AbstractRandom("RandomFloat", SchemaType.FLOAT)),
			Map.entry("RandomDouble", new AbstractRandom("RandomDouble", SchemaType.DOUBLE)));

	@Override
	public AbstractRandom find(String namespace, String name) {
		if (!namespace.equals(Namespaces.MATH))
			return null;
		return RANDOM_REPO.get(name);
	}

}

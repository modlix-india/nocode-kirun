package com.fincity.nocode.kirun.engine.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

public class KIRunSchemaRepository implements Repository<Schema> {

	private static final Schema ANY = Schema.ofAny("any")
	        .setNamespace(Namespaces.SYSTEM);
	private static final Schema BOOLEAN = Schema.ofBoolean("boolean")
	        .setNamespace(Namespaces.SYSTEM);
	private static final Schema DOUBLE = Schema.ofDouble("double")
	        .setNamespace(Namespaces.SYSTEM);
	private static final Schema FLOAT = Schema.ofFloat("float")
	        .setNamespace(Namespaces.SYSTEM);
	private static final Schema INTEGER = Schema.ofInteger("integer")
	        .setNamespace(Namespaces.SYSTEM);
	private static final Schema LONG = Schema.ofLong("long")
	        .setNamespace(Namespaces.SYSTEM);
	private static final Schema NUMBER = Schema.ofNumber("number")
	        .setNamespace(Namespaces.SYSTEM);
	private static final Schema STRING = Schema.ofString("string")
	        .setNamespace(Namespaces.SYSTEM);

	private Map<String, Schema> map = new HashMap<>();

	private List<String> filterable;

	public KIRunSchemaRepository() {

		map.put(ANY.getName(), ANY);
		map.put(BOOLEAN.getName(), BOOLEAN);
		map.put(DOUBLE.getName(), DOUBLE);
		map.put(FLOAT.getName(), FLOAT);
		map.put(INTEGER.getName(), INTEGER);
		map.put(LONG.getName(), LONG);
		map.put(NUMBER.getName(), NUMBER);
		map.put(STRING.getName(), STRING);
		map.put(Parameter.EXPRESSION.getName(), Parameter.EXPRESSION);
		map.put(Schema.NULL.getName(), Schema.NULL);
		map.put(Schema.SCHEMA.getName(), Schema.SCHEMA);

		filterable = map.values()
		        .stream()
		        .map(Schema::getFullName)
		        .toList();
	}

	@Override
	public Schema find(String namespace, String name) {

		if (!Namespaces.SYSTEM.equals(namespace))
			return null;

		return map.get(name);
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

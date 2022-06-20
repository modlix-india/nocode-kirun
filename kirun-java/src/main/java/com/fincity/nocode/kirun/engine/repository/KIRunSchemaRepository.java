package com.fincity.nocode.kirun.engine.repository;

import java.util.HashMap;
import java.util.Map;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

public class KIRunSchemaRepository implements Repository<Schema> {

	private Map<String, Schema> map = new HashMap<>();

	public KIRunSchemaRepository() {

		map.put(Schema.ANY.getName(), Schema.ANY);
		map.put(Schema.BOOLEAN.getName(), Schema.BOOLEAN);
		map.put(Schema.DOUBLE.getName(), Schema.DOUBLE);
		map.put(Schema.FLOAT.getName(), Schema.FLOAT);
		map.put(Schema.INTEGER.getName(), Schema.INTEGER);
		map.put(Schema.LONG.getName(), Schema.LONG);
		map.put(Schema.NULL.getName(), Schema.NULL);
		map.put(Schema.NUMBER.getName(), Schema.NUMBER);
		map.put(Schema.SCHEMA.getName(), Schema.SCHEMA);
		map.put(Schema.STRING.getName(), Schema.STRING);
	}

	@Override
	public Schema find(String namespace, String name) {

		if (!Namespaces.SYSTEM.equals(namespace))
			return null;

		return map.get(name);
	}
}

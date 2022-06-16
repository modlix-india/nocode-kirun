package com.fincity.nocode.kirun.engine.repository;

import java.util.HashMap;
import java.util.Map;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.json.schema.Schema;

public class KIRunSchemaRepository implements Repository<Schema> {

	private Map<String, Schema> map = new HashMap<>();

	public KIRunSchemaRepository() {

		map.put(Schema.ANY.getFullName(), Schema.ANY);
		map.put(Schema.BOOLEAN.getFullName(), Schema.BOOLEAN);
		map.put(Schema.DOUBLE.getFullName(), Schema.DOUBLE);
		map.put(Schema.FLOAT.getFullName(), Schema.FLOAT);
		map.put(Schema.INTEGER.getFullName(), Schema.INTEGER);
		map.put(Schema.LONG.getFullName(), Schema.LONG);
		map.put(Schema.NULL.getFullName(), Schema.NULL);
		map.put(Schema.NUMBER.getFullName(), Schema.NUMBER);
		map.put(Schema.SCHEMA.getFullName(), Schema.SCHEMA);
		map.put(Schema.STRING.getFullName(), Schema.STRING);
	}

	@Override
	public Schema find(String name) {
		return map.get(name);
	}
}

package com.fincity.nocode.kirun.engine.repository.reactive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class KIRunReactiveSchemaRepository implements ReactiveRepository<Schema> {

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
    private static final Schema TIMESTAMP = Schema.ofString("timeStamp")
            .setNamespace(Namespaces.DATE);

	private Map<String, Schema> map = new HashMap<>();

	private List<String> filterable;

	public KIRunReactiveSchemaRepository() {

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
        map.put("Date." + TIMESTAMP.getName(), TIMESTAMP);


		filterable = map.values()
		        .stream()
		        .map(Schema::getFullName)
		        .toList();
	}

	@Override
	public Mono<Schema> find(String namespace, String name) {

		if (!Namespaces.SYSTEM.equals(namespace))
			return Mono.empty();

		return Mono.just(map.get(name));
	}

	@Override
	public Flux<String> filter(String name) {

		String lowerCaseName = name.toLowerCase();

		return Flux.fromIterable(this.filterable)
		        .filter(e -> e.toLowerCase()
		                .contains(lowerCaseName));
	}

}

package com.fincity.nocode.kirun.engine.repository.reactive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.google.gson.JsonPrimitive;

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
	private static final Schema TIMESTAMP = Schema.ofString("Timestamp")
			.setNamespace(Namespaces.DATE);
	private static final Schema TIMEUNIT = Schema.ofString("Timeunit")
			.setNamespace(Namespaces.DATE).setEnums(List.of(
					new JsonPrimitive("YEARS"),
					new JsonPrimitive("QUARTERS"),
					new JsonPrimitive("MONTHS"),
					new JsonPrimitive("WEEKS"),
					new JsonPrimitive("DAYS"),
					new JsonPrimitive("HOURS"),
					new JsonPrimitive("MINUTES"),
					new JsonPrimitive("SECONDS"),
					new JsonPrimitive("MILLISECONDS")));

	private static final Schema TIMEOBJECT = Schema.ofObject("TimeObject")
			.setNamespace(Namespaces.DATE)
			.setProperties(Map.of(
					"year", Schema.ofInteger("year"),
					"month", Schema.ofInteger("month"),
					"day", Schema.ofInteger("day"),
					"hour", Schema.ofInteger("hour"),
					"minute", Schema.ofInteger("minute"),
					"second", Schema.ofLong("second"),
					"millisecond", Schema.ofLong("millisecond")));

	private static final Schema DURATION = Schema.ofObject("Duration")
			.setNamespace(Namespaces.DATE)
			.setProperties(Map.of(
					"years", Schema.ofInteger("years"),
					"quarters", Schema.ofInteger("quarters"),
					"months", Schema.ofInteger("months"),
					"weeks", Schema.ofInteger("weeks"),
					"days", Schema.ofInteger("days"),
					"hours", Schema.ofInteger("hours"),
					"minutes", Schema.ofInteger("minutes"),
					"seconds", Schema.ofLong("seconds"),
					"milliseconds", Schema.ofLong("milliseconds")));

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
		map.put(TIMESTAMP.getName(), TIMESTAMP);
		map.put(TIMEUNIT.getName(), TIMEUNIT);
		map.put(TIMEOBJECT.getName(), TIMEOBJECT);
		map.put(DURATION.getName(), DURATION);

		filterable = map.values()
				.stream()
				.map(Schema::getFullName)
				.toList();
	}

	@Override
	public Mono<Schema> find(String namespace, String name) {

		if (!Namespaces.SYSTEM.equals(namespace) && !Namespaces.DATE.equals(namespace))
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

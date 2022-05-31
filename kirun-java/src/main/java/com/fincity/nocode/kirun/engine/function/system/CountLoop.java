package com.fincity.nocode.kirun.engine.function.system;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM;

import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.ContextElement;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CountLoop extends AbstractFunction {

	static final String COUNT = "count";

	static final String VALUE = "value";

	static final String INDEX = "index";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("CountLoop")
	        .setNamespace(SYSTEM)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(COUNT, Schema.of(COUNT, SchemaType.INTEGER)
	                .setDefaultValue(new JsonPrimitive(1)))))
	        .setEvents(Map.ofEntries(
	                Event.eventMapEntry(Event.ITERATION,
	                        Map.of(INDEX,
	                                Schema.of(INDEX, SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT,
	                                        SchemaType.DOUBLE))),
	                Event.outputEventMapEntry(Map.of(VALUE, Schema.of(VALUE, SchemaType.INTEGER, SchemaType.LONG,
	                        SchemaType.FLOAT, SchemaType.DOUBLE)))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, ContextElement> context,
	        Map<String, Mono<JsonElement>> args) {

		var count = args.get(COUNT);

		Flux<JsonPrimitive> fluxrange = count
		        .flatMapMany(jsonElementCount -> integerSeries(jsonElementCount.getAsInt() + 1));

		return Flux.merge(fluxrange.map(e -> EventResult.of(Event.ITERATION, Map.of(INDEX, e))),
		        Flux.from(count.map(v -> EventResult.outputOf(Map.of(VALUE, v)))));
	}

	private Flux<JsonPrimitive> integerSeries(final Integer t) {
		return Flux.generate(() -> 1, (state, sink) ->
			{
				int v = state;

				if (v >= t)
					sink.complete();
				else
					sink.next(Integer.valueOf(v));

				return v + 1;
			})
		        .map(e -> new JsonPrimitive((Number) e));
	}
}

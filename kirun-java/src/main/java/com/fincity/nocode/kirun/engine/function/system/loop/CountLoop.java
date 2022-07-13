package com.fincity.nocode.kirun.engine.function.system.loop;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM_LOOP;

import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;

public class CountLoop extends AbstractFunction {

	static final String COUNT = "count";

	static final String VALUE = "value";

	static final String INDEX = "index";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("CountLoop")
	        .setNamespace(SYSTEM_LOOP)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(COUNT, Schema.of(COUNT, SchemaType.INTEGER)
	                .setDefaultValue(new JsonPrimitive(1)))))
	        .setEvents(Map.ofEntries(
	                Event.eventMapEntry(Event.ITERATION,
	                        Map.of(INDEX,
	                                Schema.of(INDEX, SchemaType.INTEGER))),
	                Event.outputEventMapEntry(Map.of(VALUE, Schema.of(VALUE, SchemaType.INTEGER)))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		var count = context.getArguments()
		        .get(COUNT);

		Flux<JsonPrimitive> fluxrange = integerSeries(count.getAsInt() + 1);

		return new FunctionOutput(Flux
		        .merge(fluxrange.map(e -> EventResult.of(Event.ITERATION, Map.of(INDEX, e))),
		                Flux.just(EventResult.outputOf(Map.of(VALUE, count))))
		        .collectList()
		        .block());
	}

	private Flux<JsonPrimitive> integerSeries(final Integer t) {
		return Flux.generate(() -> 1, (state, sink) -> {
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

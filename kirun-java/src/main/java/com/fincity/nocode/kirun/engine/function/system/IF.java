package com.fincity.nocode.kirun.engine.function.system;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM;

import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class IF extends AbstractFunction {

	static final String CONDITION = "condition";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("IF")
	        .setNamespace(SYSTEM)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(CONDITION, Schema.of(CONDITION, SchemaType.BOOLEAN))))
	        .setEvents(Map.ofEntries(Event.eventMapEntry(Event.TRUE, Map.of()),
	                Event.eventMapEntry(Event.FALSE, Map.of()), Event.outputEventMapEntry(Map.of())));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, Mono<JsonElement>> context,
	        Map<String, Mono<JsonElement>> args) {

		var condition = args.get(CONDITION);

		Flux<EventResult> fluxrange = condition
		        .flatMapMany(je -> Flux.just(EventResult.of(je.getAsBoolean() ? Event.TRUE : Event.FALSE, Map.of())));

		return Flux.merge(fluxrange, Flux.just(EventResult.outputOf(Map.of())));
	}
}

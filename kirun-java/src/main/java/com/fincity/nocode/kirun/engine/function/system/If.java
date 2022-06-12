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
import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class If extends AbstractFunction {

	static final String CONDITION = "condition";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("If")
	        .setNamespace(SYSTEM)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(CONDITION, Schema.of(CONDITION, SchemaType.BOOLEAN))))
	        .setEvents(Map.ofEntries(Event.eventMapEntry(Event.TRUE, Map.of()),
	                Event.eventMapEntry(Event.FALSE, Map.of()), Event.outputEventMapEntry(Map.of())));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, ContextElement> context,
	        Map<String, JsonElement> args) {

		var condition = Mono.just(args.get(CONDITION));

		Flux<EventResult> fluxrange = condition
		        .flatMapMany(je -> Flux.just(EventResult.of(je.getAsBoolean() ? Event.TRUE : Event.FALSE, Map.of())));

		return Flux.merge(fluxrange, Flux.just(EventResult.outputOf(Map.of())));
	}
}

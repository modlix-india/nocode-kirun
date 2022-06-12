package com.fincity.nocode.kirun.engine.function.system;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM;

import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;

public class GenerateEvent extends AbstractFunction {

	static final String EVENT_NAME = "eventName";

	static final String RESULT = "result";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("GenerateEvent")
	        .setNamespace(SYSTEM)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(EVENT_NAME, Schema.STRING),
	                Parameter.ofEntry(RESULT, Schema.ofObject(RESULT))))
	        .setEvents(Map.ofEntries(
	                Event.outputEventMapEntry(Map.of(EVENT_NAME, Schema.STRING, RESULT, Schema.ofObject(RESULT)))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, ContextElement> context, Map<String, JsonElement> args) {

		return Flux.just(EventResult.outputOf(Map.of(EVENT_NAME, args.get(EVENT_NAME), RESULT, args.get(RESULT))));
	}

}

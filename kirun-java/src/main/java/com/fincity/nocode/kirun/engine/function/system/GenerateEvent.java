package com.fincity.nocode.kirun.engine.function.system;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;

public class GenerateEvent extends AbstractFunction {

	static final String EVENT_NAME = "eventName";

	static final String RESULT = "result";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("GenerateEvent")
	        .setNamespace(SYSTEM)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(EVENT_NAME, Schema.STRING),
	                Parameter.ofEntry(RESULT, Schema.ofObject(RESULT))))
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of())));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Flux<EventResult> internalExecute(FunctionExecutionParameters context) {

		Map<String, List<Map<String, JsonElement>>> events = context.getEvents();
		Map<String, JsonElement> arguments = context.getArguments();

		String eventName = arguments.get(EVENT_NAME)
		        .getAsString();

		Map<String, JsonElement> map = context.getArguments()
		        .get(RESULT)
		        .getAsJsonObject()
		        .entrySet()
		        .stream()
		        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		events.computeIfAbsent(eventName, k -> new ArrayList<>())
		        .add(map);

		return Flux.just(EventResult.outputOf(Map.of()));
	}

}

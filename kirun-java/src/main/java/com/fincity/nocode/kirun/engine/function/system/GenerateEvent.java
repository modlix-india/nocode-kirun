package com.fincity.nocode.kirun.engine.function.system;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class GenerateEvent extends AbstractFunction {

	private static final String VALUE = "value";

	static final String EVENT_NAME = "eventName";

	static final String RESULTS = "results";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("GenerateEvent")
	        .setNamespace(SYSTEM)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(EVENT_NAME, Schema.ofString(EVENT_NAME)),
	                Parameter.ofEntry(RESULTS, Schema.ofObject(RESULTS)
	                        .setProperties(Map.of("name", Schema.ofString("name"), VALUE, Schema.ofAny(VALUE))), true)))
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of())));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected List<EventResult> internalExecute(FunctionExecutionParameters context) {

		Map<String, List<Map<String, JsonElement>>> events = context.getEvents();
		Map<String, JsonElement> arguments = context.getArguments();

		String eventName = arguments.get(EVENT_NAME)
		        .getAsString();

		Map<String, JsonElement> map = StreamSupport.stream(context.getArguments()
		        .get(RESULTS)
		        .getAsJsonArray()
		        .spliterator(), false)
		        .map(JsonObject.class::cast)
		        .map(e -> Tuples.of(e.get("name")
		                .getAsString(), e.get(VALUE)))
		        .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2));

		events.computeIfAbsent(eventName, k -> new ArrayList<>())
		        .add(map);

		return List.of(EventResult.outputOf(Map.of()));
	}

}

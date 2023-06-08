package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class ToString extends AbstractReactiveFunction {

	protected static final String PARAMETER_INPUT_ANYTYPE_NAME = "anytype";

	protected static final String EVENT_RESULT_NAME = "result";

	protected static final Parameter PARAMETER_INPUT_ANYTYPE = new Parameter()
			.setParameterName(PARAMETER_INPUT_ANYTYPE_NAME)
			.setSchema(Schema.ofAny(PARAMETER_INPUT_ANYTYPE_NAME));

	protected static final Event EVENT_STRING = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofString(EVENT_RESULT_NAME)));

	private final FunctionSignature signature = new FunctionSignature().setName("ToString")
			.setNamespace(Namespaces.STRING)
			.setParameters(Map.of(PARAMETER_INPUT_ANYTYPE.getParameterName(), PARAMETER_INPUT_ANYTYPE))
			.setEvents(Map.of(EVENT_STRING.getName(), EVENT_STRING));

	@Override
	public FunctionSignature getSignature() {
		return signature;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonElement input = context.getArguments()
		        .get(PARAMETER_INPUT_ANYTYPE_NAME);

		GsonBuilder gb = new GsonBuilder();
		gb.setPrettyPrinting();
		Gson gson = gb.create();

		return Mono.just(new FunctionOutput(
		        List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(gson.toJson(input)))))));

	}

}

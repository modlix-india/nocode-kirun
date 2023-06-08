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
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class TrimTo extends AbstractReactiveFunction {

	protected static final String PARAMETER_STRING_NAME = "string";

	protected static final String PARAMETER_LENGTH_NAME = "length";

	protected static final String EVENT_RESULT_NAME = "result";

	protected static final Parameter PARAMETER_STRING = new Parameter().setParameterName(PARAMETER_STRING_NAME)
			.setSchema(Schema.ofString(PARAMETER_STRING_NAME));

	protected static final Parameter PARAMETER_LENGTH = new Parameter().setParameterName(PARAMETER_LENGTH_NAME)
			.setSchema(Schema.ofInteger(PARAMETER_LENGTH_NAME));

	protected static final Event EVENT_STRING = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofString(EVENT_RESULT_NAME)));

	private final FunctionSignature signature = new FunctionSignature().setName("TrimTo")
			.setNamespace(Namespaces.STRING)
			.setParameters(Map.of(PARAMETER_STRING.getParameterName(), PARAMETER_STRING,
					PARAMETER_LENGTH.getParameterName(), PARAMETER_LENGTH))
			.setEvents(Map.of(EVENT_STRING.getName(), EVENT_STRING));

	@Override
	public FunctionSignature getSignature() {

		return signature;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		String inputString = context.getArguments().get(PARAMETER_STRING_NAME).getAsJsonPrimitive().getAsString();
		Integer length = context.getArguments().get(PARAMETER_LENGTH_NAME).getAsJsonPrimitive().getAsInt();

		return Mono.just(new FunctionOutput(List.of(EventResult.of(EVENT_RESULT_NAME,
				Map.of(EVENT_RESULT_NAME, new JsonPrimitive(inputString.substring(0, length)))))));
	}

}

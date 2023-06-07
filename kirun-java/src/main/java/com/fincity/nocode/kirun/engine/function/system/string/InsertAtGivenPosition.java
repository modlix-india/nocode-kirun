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

public class InsertAtGivenPosition extends AbstractReactiveFunction {

	protected static final String PARAMETER_STRING_NAME = "string";

	protected static final String PARAMETER_AT_POSITION_NAME = "position";

	protected static final String PARAMETER_INSERT_STRING_NAME = "insertString";

	protected static final String EVENT_RESULT_NAME = "result";

	protected static final Parameter PARAMETER_STRING = new Parameter().setParameterName(PARAMETER_STRING_NAME)
			.setSchema(Schema.ofString(PARAMETER_STRING_NAME));

	protected static final Parameter PARAMETER_AT_POSITION = new Parameter()
			.setParameterName(PARAMETER_AT_POSITION_NAME).setSchema(Schema.ofInteger(PARAMETER_AT_POSITION_NAME));

	protected static final Parameter PARAMETER_INSERT_STRING = new Parameter()
			.setParameterName(PARAMETER_INSERT_STRING_NAME).setSchema(Schema.ofString(PARAMETER_INSERT_STRING_NAME));

	protected static final Event EVENT_STRING = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofString(EVENT_RESULT_NAME)));

	private final FunctionSignature signature = new FunctionSignature().setName("InsertAtGivenPosition")
			.setNamespace(Namespaces.STRING)
			.setParameters(Map.of(PARAMETER_STRING.getParameterName(), PARAMETER_STRING,
					PARAMETER_AT_POSITION.getParameterName(), PARAMETER_AT_POSITION,
					PARAMETER_INSERT_STRING.getParameterName(), PARAMETER_INSERT_STRING))
			.setEvents(Map.of(EVENT_STRING.getName(), EVENT_STRING));

	@Override
	public FunctionSignature getSignature() {

		return signature;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		String inputString = context.getArguments().get(PARAMETER_STRING_NAME).getAsJsonPrimitive().getAsString();
		Integer at = context.getArguments().get(PARAMETER_AT_POSITION_NAME).getAsJsonPrimitive().getAsInt();
		String insertString = context.getArguments().get(PARAMETER_INSERT_STRING_NAME).getAsJsonPrimitive()
				.getAsString();

		StringBuilder outputString = new StringBuilder();

		outputString.append(inputString.substring(0, at));
		outputString.append(insertString);
		outputString.append(inputString.substring(at));

		return Mono.just(new FunctionOutput(List.of(EventResult.of(EVENT_RESULT_NAME,
				Map.of(EVENT_RESULT_NAME, new JsonPrimitive(outputString.toString()))))));
	}

}

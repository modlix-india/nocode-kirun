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

public class DeleteForGivenLength extends AbstractReactiveFunction {

	protected static final String PARAMETER_STRING_NAME = "string";

	protected static final String PARAMETER_AT_START_NAME = "startPosition";

	protected static final String PARAMETER_AT_END_NAME = "endPosition";

	protected static final String EVENT_RESULT_NAME = "result";

	protected static final Parameter PARAMETER_STRING = new Parameter().setParameterName(PARAMETER_STRING_NAME)
	        .setSchema(Schema.ofString(PARAMETER_STRING_NAME));

	protected static final Parameter PARAMETER_AT_START = new Parameter().setParameterName(PARAMETER_AT_START_NAME)
	        .setSchema(Schema.ofInteger(PARAMETER_AT_START_NAME));

	protected static final Parameter PARAMETER_AT_END = new Parameter().setParameterName(PARAMETER_AT_END_NAME)
	        .setSchema(Schema.ofInteger(PARAMETER_AT_END_NAME));

	protected static final Event EVENT_STRING = new Event().setName(Event.OUTPUT)
	        .setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofString(EVENT_RESULT_NAME)));

	private final FunctionSignature signature = new FunctionSignature().setName("DeleteForGivenLength")
	        .setNamespace(Namespaces.STRING)
	        .setParameters(
	                Map.of(PARAMETER_STRING.getParameterName(), PARAMETER_STRING, PARAMETER_AT_START.getParameterName(),
	                        PARAMETER_AT_START, PARAMETER_AT_END.getParameterName(), PARAMETER_AT_END))
	        .setEvents(Map.of(EVENT_STRING.getName(), EVENT_STRING));

	@Override
	public FunctionSignature getSignature() {

		return signature;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		String inputString = context.getArguments()
		        .get(PARAMETER_STRING_NAME)
		        .getAsString();
		Integer startPosition = context.getArguments()
		        .get(PARAMETER_AT_START_NAME)
		        .getAsInt();
		Integer endPosition = context.getArguments()
		        .get(PARAMETER_AT_END_NAME)
		        .getAsInt();
		Integer inputStringLength = inputString.length();

		if (endPosition >= startPosition) {
			StringBuilder outputString = new StringBuilder(inputStringLength - (endPosition - startPosition) + 1);
			outputString.append(inputString.substring(0, startPosition));
			outputString.append(inputString.substring(endPosition));
			return Mono.just(new FunctionOutput(List
			        .of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(outputString.toString()))))));
		}

		return Mono.just(new FunctionOutput(
		        List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(inputString))))));
	}

}

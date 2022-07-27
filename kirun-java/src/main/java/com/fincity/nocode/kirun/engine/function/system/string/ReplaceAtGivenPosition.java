package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

public class ReplaceAtGivenPosition extends AbstractFunction {

	protected static final String PARAMETER_STRING_NAME = "string";

	protected static final String PARAMETER_AT_START_NAME = "startPosition";

	protected static final String PARAMETER_AT_LENGTH_NAME = "lengthPosition";

	protected static final String PARAMETER_REPLACE_STRING_NAME = "replaceString";

	protected static final String EVENT_RESULT_NAME = "result";

	protected static final Parameter PARAMETER_STRING = new Parameter().setParameterName(PARAMETER_STRING_NAME)
			.setSchema(Schema.ofString(PARAMETER_STRING_NAME));

	protected static final Parameter PARAMETER_AT_START = new Parameter().setParameterName(PARAMETER_AT_START_NAME)
			.setSchema(Schema.ofInteger(PARAMETER_AT_START_NAME));

	protected static final Parameter PARAMETER_AT_LENGTH = new Parameter().setParameterName(PARAMETER_AT_LENGTH_NAME)
			.setSchema(Schema.ofInteger(PARAMETER_AT_LENGTH_NAME));

	protected static final Parameter PARAMETER_REPLACE_STRING = new Parameter()
			.setParameterName(PARAMETER_REPLACE_STRING_NAME).setSchema(Schema.ofString(PARAMETER_REPLACE_STRING_NAME));

	protected static final Event EVENT_STRING = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofString(EVENT_RESULT_NAME)));

	private final FunctionSignature signature = new FunctionSignature().setName("ReplaceAtGivenPosition")
			.setNamespace(Namespaces.STRING)
			.setParameters(Map.of(PARAMETER_STRING.getParameterName(), PARAMETER_STRING,
					PARAMETER_AT_START.getParameterName(), PARAMETER_AT_START, PARAMETER_AT_LENGTH.getParameterName(),
					PARAMETER_AT_LENGTH, PARAMETER_REPLACE_STRING.getParameterName(), PARAMETER_REPLACE_STRING))
			.setEvents(Map.of(EVENT_STRING.getName(), EVENT_STRING));

	@Override
	public FunctionSignature getSignature() {
		return signature;
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		String inputString = context.getArguments().get(PARAMETER_STRING_NAME).getAsString();
		Integer startPosition = context.getArguments().get(PARAMETER_AT_START_NAME).getAsInt();
		Integer length = context.getArguments().get(PARAMETER_AT_LENGTH_NAME).getAsInt();
		String replaceString = context.getArguments().get(PARAMETER_REPLACE_STRING_NAME).getAsString();
		Integer inputStringLength = inputString.length();

		if (startPosition < length) {
			StringBuilder outputString = new StringBuilder(inputStringLength - (length - startPosition) + 1);
			outputString.append(inputString.substring(0, startPosition));
			outputString.append(replaceString);
			outputString.append(inputString.substring(startPosition + length));
			return new FunctionOutput(List
					.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(outputString.toString())))));
		}
		return new FunctionOutput(
				List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(inputString)))));
	}

}

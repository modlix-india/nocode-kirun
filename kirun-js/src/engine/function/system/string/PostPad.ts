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

public class PostPad extends AbstractFunction {

	protected static final String PARAMETER_STRING_NAME = "string";

	protected static final String PARAMETER_POSTPAD_STRING_NAME = "postpadString";

	protected static final String PARAMETER_LENGTH_NAME = "length";

	protected static final String EVENT_RESULT_NAME = "result";

	protected static final Parameter PARAMETER_STRING = new Parameter().setParameterName(PARAMETER_STRING_NAME)
			.setSchema(Schema.ofString(PARAMETER_STRING_NAME));

	protected static final Parameter PARAMETER_POSTPAD_STRING = new Parameter()
			.setParameterName(PARAMETER_POSTPAD_STRING_NAME).setSchema(Schema.ofString(PARAMETER_POSTPAD_STRING_NAME));

	protected static final Parameter PARAMETER_LENGTH = new Parameter().setParameterName(PARAMETER_LENGTH_NAME)
			.setSchema(Schema.ofInteger(PARAMETER_LENGTH_NAME));

	protected static final Event EVENT_STRING = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofString(EVENT_RESULT_NAME)));

	private final FunctionSignature signature = new FunctionSignature().setName("PostPad")
			.setNamespace(Namespaces.STRING)
			.setParameters(Map.of(PARAMETER_STRING.getParameterName(), PARAMETER_STRING,
					PARAMETER_POSTPAD_STRING.getParameterName(), PARAMETER_POSTPAD_STRING,
					PARAMETER_LENGTH.getParameterName(), PARAMETER_LENGTH))
			.setEvents(Map.of(EVENT_STRING.getName(), EVENT_STRING));

	@Override
	public FunctionSignature getSignature() {
		return signature;
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {
		String inputString = context.getArguments().get(PARAMETER_STRING_NAME).getAsJsonPrimitive().getAsString();
		String postpadString = context.getArguments().get(PARAMETER_POSTPAD_STRING_NAME).getAsJsonPrimitive()
				.getAsString();
		Integer length = context.getArguments().get(PARAMETER_LENGTH_NAME).getAsJsonPrimitive().getAsInt();
		StringBuilder outputString = new StringBuilder(inputString.length() + length);
		Integer prepadStringLength = postpadString.length();

		outputString.append(inputString);

		while (prepadStringLength <= length) {
			outputString.append(postpadString);
			prepadStringLength += postpadString.length();
		}

		if (outputString.length() - inputString.length() < length) {
			outputString.append(postpadString.substring(0, length - (outputString.length() - inputString.length())));
		}

		return new FunctionOutput(List.of(EventResult.of(EVENT_RESULT_NAME,
				Map.of(EVENT_RESULT_NAME, new JsonPrimitive(outputString.toString())))));
	}
}

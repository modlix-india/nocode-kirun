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

public class Frequency extends AbstractFunction {

	protected static final String PARAMETER_STRING_NAME = "string";

	protected static final String PARAMETER_SEARCH_STRING_NAME = "searchString";

	protected static final String EVENT_RESULT_NAME = "result";

	protected static final Parameter PARAMETER_STRING = new Parameter().setParameterName(PARAMETER_STRING_NAME)
			.setSchema(Schema.ofString(PARAMETER_STRING_NAME));

	protected static final Parameter PARAMETER_SEARCH_STRING = new Parameter()
			.setParameterName(PARAMETER_SEARCH_STRING_NAME).setSchema(Schema.ofString(PARAMETER_SEARCH_STRING_NAME));

	protected static final Event EVENT_INT = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofInteger(EVENT_RESULT_NAME)));

	private final FunctionSignature signature = new FunctionSignature().setName("Frequency")
			.setNamespace(Namespaces.STRING)
			.setParameters(Map.of(PARAMETER_STRING.getParameterName(), PARAMETER_STRING,
					PARAMETER_SEARCH_STRING.getParameterName(), PARAMETER_SEARCH_STRING))
			.setEvents(Map.of(EVENT_INT.getName(), EVENT_INT));

	@Override
	public FunctionSignature getSignature() {
		return signature;
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		JsonPrimitive inputString = context.getArguments().get(PARAMETER_STRING_NAME).getAsJsonPrimitive();
		JsonPrimitive searchString = context.getArguments().get(PARAMETER_SEARCH_STRING_NAME).getAsJsonPrimitive();

		return new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME,
				new JsonPrimitive(this.stringFunction(inputString.getAsString(), searchString.getAsString()))))));

	}

	private Integer stringFunction(String input, String search) {

		Integer inputLength = input.length();
		Integer searchLength = search.length();
		Integer frequency = 0;

		for (int i = 0; i < inputLength - searchLength + 1; i++) {
			if (input.charAt(i) == search.charAt(0)) {
				boolean flag = true;
				for (int j = 0; j < searchLength; j++) {
					if (input.charAt(i + j) != search.charAt(j)) {
						flag = false;
						break;
					}
				}
				if (flag)
					frequency++;
			}
		}

		return frequency;
	}

}

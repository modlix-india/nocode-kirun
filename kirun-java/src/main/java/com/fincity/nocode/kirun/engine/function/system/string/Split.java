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

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

public class Split extends AbstractFunction {

	protected static final String PARAMETER_STRING_NAME = "string";

	protected static final String PARAMETER_SPLIT_STRING_NAME = "searchString";

	protected static final String EVENT_RESULT_NAME = "result";

	protected static final Parameter PARAMETER_STRING = new Parameter().setParameterName(PARAMETER_STRING_NAME)
			.setSchema(Schema.ofString(PARAMETER_STRING_NAME));

	protected static final Parameter PARAMETER_SPLIT_STRING = new Parameter()
			.setParameterName(PARAMETER_SPLIT_STRING_NAME).setSchema(Schema.ofString(PARAMETER_SPLIT_STRING_NAME));

	protected static final Event EVENT_ARRAY = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofArray(EVENT_RESULT_NAME)));

	@Override
	public FunctionSignature getSignature() {

		return new FunctionSignature()
				.setName("Split").setNamespace(Namespaces.STRING).setParameters(Map.of(PARAMETER_STRING_NAME,
						PARAMETER_STRING, PARAMETER_SPLIT_STRING_NAME, PARAMETER_SPLIT_STRING))
				.setEvents(Map.of(EVENT_ARRAY.getName(), EVENT_ARRAY));
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		JsonPrimitive s1 = context.getArguments().get(PARAMETER_STRING_NAME).getAsJsonPrimitive();
		JsonPrimitive s2 = context.getArguments().get(PARAMETER_SPLIT_STRING_NAME).getAsJsonPrimitive();

		String str = s1.getAsString();

		JsonArray stringSplitedJsonArray = new JsonArray();
		String[] splitedString = str.split(s2.getAsString());

		int start = 0;
		while (start <= splitedString.length - 1) {
			stringSplitedJsonArray.add(splitedString[start]);
			start++;
		}

		return new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_ARRAY.getName(), stringSplitedJsonArray))));
	}

}

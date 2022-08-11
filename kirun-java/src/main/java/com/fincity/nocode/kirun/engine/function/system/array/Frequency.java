package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

public class Frequency extends AbstractArrayFunction {

	public Frequency() {
		super("Frequency",
				List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_ANY, PARAMETER_INT_SOURCE_FROM, PARAMETER_INT_LENGTH),
				EVENT_RESULT_INTEGER);
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		JsonArray source = context.getArguments().get(PARAMETER_ARRAY_SOURCE.getParameterName()).getAsJsonArray();

		var find = context.getArguments().get(PARAMETER_ANY.getParameterName());

		JsonPrimitive from = context.getArguments().get(PARAMETER_INT_SOURCE_FROM.getParameterName())
				.getAsJsonPrimitive();

		int length = context.getArguments().get(PARAMETER_INT_LENGTH.getParameterName()).getAsJsonPrimitive()
				.getAsInt();

		if (source.size() == 0)
			return new FunctionOutput(
					List.of(EventResult.outputOf(Map.of(EVENT_RESULT_INTEGER.getName(), new JsonPrimitive(0)))));

		int end = length > 0 ? length : source.size();

		if (length == -1)
			end = source.size() - from.getAsInt();

		int start = from.getAsInt() < 0  ? 0 : from.getAsInt();

		Integer frequency = 0;

		for (int i = start; i < end && i < source.size(); i++) {

			if (find.isJsonPrimitive() && source.get(i).isJsonPrimitive()
					&& source.get(i).equals(find.getAsJsonPrimitive()) ||

					find.isJsonArray() && source.get(i).isJsonArray() && source.get(i).equals(find.getAsJsonArray()) ||

					find.isJsonObject() && source.get(i).isJsonObject() && source.get(i).equals(find.getAsJsonObject()))

				frequency++;

		}

		return new FunctionOutput(
				List.of(EventResult.outputOf(Map.of(EVENT_RESULT_INTEGER.getName(), new JsonPrimitive(frequency)))));
	}

}

package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

public class IndexOfArray extends AbstractArrayFunction {

	public IndexOfArray() {
		super("IndexOfArray", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_ARRAY_SECOND_SOURCE, PARAMETER_INT_FIND_FROM),
				EVENT_RESULT_INTEGER);
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		JsonArray source = context.getArguments().get(PARAMETER_ARRAY_SOURCE.getParameterName()).getAsJsonArray();

		JsonArray secondSource = context.getArguments().get(PARAMETER_ARRAY_SECOND_SOURCE.getParameterName())
				.getAsJsonArray();

		JsonPrimitive from = context.getArguments().get(PARAMETER_INT_FIND_FROM.getParameterName())
				.getAsJsonPrimitive();

		int len = from.getAsInt() >= source.size() ? 0 : from.getAsInt();
		int secondSourceSize = secondSource.size();
		int index = -1;

		for (int i = len; i < source.size(); i++) {
			int j = 0;
			if (!source.get(i).isJsonNull() && !secondSource.get(j).isJsonNull()
					&& source.get(i).equals(secondSource.get(j))) {
				while (j < secondSourceSize) {
					if (source.get(i).isJsonNull() || secondSource.get(j).isJsonNull()
							|| !source.get(i + j).equals(secondSource.get(j))) {
						break;
					}
					j++;
				}
				if (j == secondSourceSize) {
					index = i;
					break;
				}
			}
		}

		return new FunctionOutput(
				List.of(EventResult.outputOf(Map.of(EVENT_RESULT_INTEGER.getName(), new JsonPrimitive(index)))));
	}
}

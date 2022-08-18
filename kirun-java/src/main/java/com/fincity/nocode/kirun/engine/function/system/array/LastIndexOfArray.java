package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.primitive.PrimitiveUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

public class LastIndexOfArray extends AbstractArrayFunction {

	public LastIndexOfArray() {
		super("LastIndexOfArray",
				List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_ARRAY_SECOND_SOURCE, PARAMETER_INT_FIND_FROM),
				EVENT_RESULT_INTEGER);
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		JsonArray source = context.getArguments().get(PARAMETER_ARRAY_SOURCE.getParameterName()).getAsJsonArray();

		JsonArray secondSource = context.getArguments().get(PARAMETER_ARRAY_SECOND_SOURCE.getParameterName())
				.getAsJsonArray();

		int from = context.getArguments().get(PARAMETER_INT_FIND_FROM.getParameterName()).getAsJsonPrimitive()
				.getAsInt();

		if (source.isEmpty())

			return new FunctionOutput(
					List.of(EventResult.outputOf(Map.of(EVENT_RESULT_ARRAY.getName(), new JsonPrimitive(-1)))));

		if (from < 0 || from > source.size() || secondSource.size() > source.size())
			throw new KIRuntimeException("Given from index is more than the size of the source array");

		int secondSourceSize = secondSource.size();
		int index = -1;

		for (int i = from; i < source.size(); i++) {
			int j = 0;
			if (PrimitiveUtil.compare(source.get(i), secondSource.get(j)) == 0) {
				while (j < secondSourceSize) {
					if (PrimitiveUtil.compare(source.get(i + j), secondSource.get(j)) != 0) {
						break;
					}
					j++;
				}
				if (j == secondSourceSize) {
					index = i;
				}
			}
		}

		return new FunctionOutput(
				List.of(EventResult.outputOf(Map.of(EVENT_RESULT_ARRAY.getName(), new JsonPrimitive(index)))));
	}
}

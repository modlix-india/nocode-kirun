package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

public class SubArray extends AbstractArrayFunction {

	public SubArray() {
		super("SubArray", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_INT_FIND_FROM, PARAMETER_INT_LENGTH),
				EVENT_RESULT_EMPTY);
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		JsonArray source = context.getArguments().get(PARAMETER_ARRAY_SOURCE.getParameterName()).getAsJsonArray();

		JsonPrimitive from = context.getArguments().get(PARAMETER_INT_FIND_FROM.getParameterName())
				.getAsJsonPrimitive();

		JsonPrimitive length = context.getArguments().get(PARAMETER_INT_LENGTH.getParameterName()).getAsJsonPrimitive();

		int len = length.getAsInt();

		if (len == -1)
			len = source.size() - from.getAsInt();

		if (len <= 0)
			return new FunctionOutput(List.of(EventResult.outputOf(Map.of())));

		int start = from.getAsInt();

		if (!(start >= 0 && start < source.size()) || start + len > source.size())

			throw new KIRuntimeException(
					"Given find from point is more than the source size array or the Requested length for the subarray was more than the source size");

		while (start != 0) {
			source.remove(0);
			start--;
		}

		while (source.size() > len)
			source.remove(source.size() - 1);

		return new FunctionOutput(List.of(EventResult.outputOf(Map.of())));
	}

}

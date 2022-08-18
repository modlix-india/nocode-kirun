package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

public class DeleteFrom extends AbstractArrayFunction {

	public DeleteFrom() {
		super("DeleteFrom", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_INT_SOURCE_FROM, PARAMETER_INT_LENGTH),
				EVENT_RESULT_EMPTY);
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		JsonArray source = context.getArguments().get(PARAMETER_ARRAY_SOURCE.getParameterName()).getAsJsonArray();

		JsonPrimitive from = context.getArguments().get(PARAMETER_INT_SOURCE_FROM.getParameterName())
				.getAsJsonPrimitive();

		JsonPrimitive length = context.getArguments().get(PARAMETER_INT_LENGTH.getParameterName()).getAsJsonPrimitive();

		if (source.size() == 0)
			throw new KIRuntimeException("There are no elements to be deleted");

		int start = from.getAsInt() < 0 ? 0 : from.getAsInt();

		int len = length.getAsInt();

		if (len == -1)
			len = source.size() - start;

		if (start + len > source.size())
			throw new KIRuntimeException("Requested length to be deleted is more than the size of array ");

		while (len > 0) {
			source.remove(start);
			len--;
		}

		return new FunctionOutput(List.of(EventResult.outputOf(Map.of())));
	}

}
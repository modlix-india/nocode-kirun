package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.primitive.PrimitiveUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

		JsonElement find = context.getArguments().get(PARAMETER_ANY.getParameterName());

		int start = context.getArguments().get(PARAMETER_INT_SOURCE_FROM.getParameterName()).getAsJsonPrimitive()
				.getAsInt();

		int length = context.getArguments().get(PARAMETER_INT_LENGTH.getParameterName()).getAsJsonPrimitive()
				.getAsInt();

		if (source.size() == 0)
			return new FunctionOutput(
					List.of(EventResult.outputOf(Map.of(EVENT_RESULT_INTEGER.getName(), new JsonPrimitive(0)))));

		if (start > source.size())
			throw new KIRuntimeException("Given start point is more than the size of source");

		if (find == null || find.isJsonNull())
			throw new KIRuntimeException("Given find was null. Hence cannot be found in the array");

		int end = start + length;

		if (length == -1)
			end = source.size() - start;

		if (end > source.size())
			throw new KIRuntimeException("Given length is more than the size of source");

		int frequency = 0;

		for (int i = start; i < end; i++) {

			if (PrimitiveUtil.compare(source.get(i), find) == 0)
				frequency++;
		}

		return new FunctionOutput(
				List.of(EventResult.outputOf(Map.of(EVENT_RESULT_INTEGER.getName(), new JsonPrimitive(frequency)))));
	}

}

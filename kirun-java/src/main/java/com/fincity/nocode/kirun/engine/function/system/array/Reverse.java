package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class Reverse extends AbstractArrayFunction {

	public Reverse() {
		super("Reverse", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_INT_SOURCE_FROM, PARAMETER_INT_LENGTH),
				EVENT_RESULT_EMPTY);
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		JsonArray source = context.getArguments().get(PARAMETER_ARRAY_SOURCE.getParameterName()).getAsJsonArray();

		JsonPrimitive start = context.getArguments().get(PARAMETER_INT_SOURCE_FROM.getParameterName())
				.getAsJsonPrimitive();

		JsonPrimitive end = context.getArguments().get(PARAMETER_INT_LENGTH.getParameterName()).getAsJsonPrimitive();

		if (source.isEmpty() || end.getAsInt() > source.size() - 1 || start.getAsInt() < 0)
			return new FunctionOutput(List.of(EventResult.outputOf(Map.of())));

		int st = start.getAsInt();
		int ed = end.getAsInt();

		if (end.equals(new JsonPrimitive(-1)))
			ed = source.size() - st;

		while (st < ed) {
			JsonElement first = source.get(st);
			JsonElement last = source.get(ed);
			source.set(st++, last);
			source.set(ed--, first);
		}

		return new FunctionOutput(List.of(EventResult.outputOf(Map.of())));
	}
}

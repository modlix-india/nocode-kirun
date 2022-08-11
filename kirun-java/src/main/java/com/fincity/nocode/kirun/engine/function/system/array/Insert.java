package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class Insert extends AbstractArrayFunction {

	public Insert() {
		super("Insert", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_INT_OFFSET, PARAMETER_ANY), EVENT_RESULT_EMPTY);
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		JsonArray source = context.getArguments().get(PARAMETER_ARRAY_SOURCE.getParameterName()).getAsJsonArray();

		int offset = context.getArguments().get(PARAMETER_INT_OFFSET.getParameterName()).getAsJsonPrimitive()
				.getAsInt();

		var output = context.getArguments().get(PARAMETER_ANY.getParameterName());

		if (source.size() == 0) {
			if (offset == 0)
				source.add(output);
			return new FunctionOutput(List.of(EventResult.outputOf(Map.of())));
		}

		source.add(output);
		int len = source.size() - 1;
		offset++; // to insert at that point

		while (len >= offset) {
			JsonElement temp = source.get(len - 1);
			source.set(len - 1, source.get(len));
			source.set(len, temp);
			len--;
		}

		return new FunctionOutput(List.of(EventResult.outputOf(Map.of())));

	}

}

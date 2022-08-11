package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class Shuffle extends AbstractArrayFunction {

	public Shuffle() {
		super("Shuffle", List.of(PARAMETER_ARRAY_SOURCE), EVENT_RESULT_EMPTY);
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		JsonArray source = context.getArguments().get(PARAMETER_ARRAY_SOURCE.getParameterName()).getAsJsonArray();

		if (source.size() <= 1)
			return new FunctionOutput(List.of(EventResult.outputOf(Map.of())));

		int x = 0;
		int size = source.size();

		Random rand = new Random();

		for (int i = 0; i < size; i++) {
			int y = rand.nextInt(0, size) % size;
			JsonElement temp = source.get(x);
			source.set(x, source.get(y));
			source.set(y, temp);
			x = y;
		}

		return new FunctionOutput(List.of(EventResult.outputOf(Map.of())));
	}
}

package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import reactor.core.publisher.Mono;

public class Shuffle extends AbstractArrayFunction {

	private static final Random rand = new Random();

	public Shuffle() {
		super("Shuffle", List.of(PARAMETER_ARRAY_SOURCE), EVENT_RESULT_ARRAY);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray source = context.getArguments().get(PARAMETER_ARRAY_SOURCE.getParameterName()).getAsJsonArray();

		if (source.size() <= 1)
			return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, source)))));

		int x = 0;
		int size = source.size();
		source = duplicateArray(source);

		for (int i = 0; i < size; i++) {
			int y = rand.nextInt(0, size) % size;
			JsonElement temp = source.get(x);
			source.set(x, source.get(y));
			source.set(y, temp);
			x = y;
		}

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, source)))));
	}
}

package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import reactor.core.publisher.Mono;

public class AddFirst extends AbstractArrayFunction {

	public AddFirst() {
		super("AddFirst", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_ANY_ELEMENT), EVENT_RESULT_ARRAY);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray source = context.getArguments()
				.get(PARAMETER_ARRAY_SOURCE.getParameterName())
				.getAsJsonArray();

		var input = context.getArguments()
				.get(PARAMETER_ANY_ELEMENT.getParameterName());

		source = duplicateArray(source);

		if (source.isEmpty()) {
			source.add(input);
			return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, source)))));
		}

		source.add(input);

		int len = source.size() - 1;

		while (len > 0) {
			JsonElement temp = source.get(len - 1);
			source.set(len - 1, source.get(len));
			source.set(len, temp);
			len--;
		}

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, source)))));
	}

}

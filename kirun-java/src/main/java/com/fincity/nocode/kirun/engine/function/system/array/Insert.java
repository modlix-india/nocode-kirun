package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import reactor.core.publisher.Mono;

public class Insert extends AbstractArrayFunction {

	public Insert() {
		super("Insert", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_INT_OFFSET, PARAMETER_ANY_ELEMENT),
				EVENT_RESULT_ARRAY);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray source = context.getArguments()
				.get(PARAMETER_ARRAY_SOURCE.getParameterName())
				.getAsJsonArray();

		int offset = context.getArguments()
				.get(PARAMETER_INT_OFFSET.getParameterName())
				.getAsJsonPrimitive()
				.getAsInt();

		var output = context.getArguments()
				.get(PARAMETER_ANY_ELEMENT.getParameterName());

		source = duplicateArray(source);
		if (source.size() == 0) {
			if (offset == 0)
				source.add(output);
			return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, source)))));
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

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, source)))));

	}

}

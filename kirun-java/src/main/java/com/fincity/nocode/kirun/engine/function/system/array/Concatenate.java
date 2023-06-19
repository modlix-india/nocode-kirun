package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;

import reactor.core.publisher.Mono;

public class Concatenate extends AbstractArrayFunction {

	public Concatenate() {
		super("Concatenate", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_ARRAY_SECOND_SOURCE), EVENT_RESULT_ARRAY);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray source = context.getArguments()
				.get(PARAMETER_ARRAY_SOURCE.getParameterName())
				.getAsJsonArray();

		JsonArray secondSource = context.getArguments()
				.get(PARAMETER_ARRAY_SECOND_SOURCE.getParameterName())
				.getAsJsonArray();

		if (secondSource.isJsonNull() || secondSource.isEmpty())
			return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, source)))));

		source = duplicateArray(source);
		source.addAll(secondSource);

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, source)))));
	}
}

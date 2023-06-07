package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;

import reactor.core.publisher.Mono;

public class Add extends AbstractArrayFunction {

	public Add() {
		super("Add", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_ARRAY_SECOND_SOURCE), EVENT_RESULT_EMPTY);
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
			return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of()))));

		source.addAll(secondSource);

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of()))));
	}
}

package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class Join extends AbstractArrayFunction {

	public Join() {
		super("Join", List.of(PARAMETER_ARRAY_SOURCE_PRIMITIVE, PARAMETER_DELIMITER), EVENT_RESULT_STRING);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray source = context.getArguments().get(PARAMETER_ARRAY_SOURCE.getParameterName()).getAsJsonArray();

		var delimiter = context.getArguments().get(PARAMETER_DELIMITER.getParameterName()).getAsString();
		
		if (source.isEmpty())
			return Mono.just(new FunctionOutput(
					List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(""))))));

		var result = StreamSupport.stream(source.spliterator(), false).filter(e -> !e.isJsonNull()).map(e -> e.getAsString())
				.collect(Collectors.joining(delimiter));
		System.out.println(result);
		return Mono.just(new FunctionOutput(
				List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(result))))));
	}
}

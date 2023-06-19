package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import reactor.core.publisher.Mono;

public class Reverse extends AbstractArrayFunction {

	public Reverse() {
		super("Reverse", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_INT_SOURCE_FROM, PARAMETER_INT_LENGTH),
				EVENT_RESULT_ARRAY);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray source = context.getArguments().get(PARAMETER_ARRAY_SOURCE.getParameterName()).getAsJsonArray();

		int st = context.getArguments().get(PARAMETER_INT_SOURCE_FROM.getParameterName()).getAsJsonPrimitive()
				.getAsInt();

		int length = context.getArguments().get(PARAMETER_INT_LENGTH.getParameterName()).getAsJsonPrimitive()
				.getAsInt();

		if (length == -1)
			length = source.size() - st;

		if (length >= source.size() || length < 0 || st < 0)
			throw new KIRuntimeException(
					"Please provide start point between the start and end indexes or provide the length which was less than the source size ");

		int endPoint = st + length - 1;
		source = duplicateArray(source);

		while (st < endPoint) {
			JsonElement first = source.get(st);
			JsonElement last = source.get(endPoint);
			source.set(st++, last);
			source.set(endPoint--, first);
		}

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, source)))));
	}
}

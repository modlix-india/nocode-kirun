package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class SubArray extends AbstractArrayFunction {

	public SubArray() {
		super("SubArray", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_INT_FIND_FROM, PARAMETER_INT_LENGTH),
				EVENT_RESULT_ARRAY);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray source = context.getArguments()
				.get(PARAMETER_ARRAY_SOURCE.getParameterName())
				.getAsJsonArray();

		JsonPrimitive from = context.getArguments()
				.get(PARAMETER_INT_FIND_FROM.getParameterName())
				.getAsJsonPrimitive();

		JsonPrimitive length = context.getArguments()
				.get(PARAMETER_INT_LENGTH.getParameterName())
				.getAsJsonPrimitive();

		int len = length.getAsInt();

		int start = from.getAsInt();

		if (len == -1)
			len = source.size() - start;

		if (len <= 0)
			return Mono.just(
					new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonArray())))));

		if (!(start >= 0 && start < source.size()) || start + len > source.size())

			throw new KIRuntimeException(
					"Given find from point is more than the source size array or the Requested length for the subarray was more than the source size");

		JsonArray subArr = new JsonArray();

		for (int i = 0; i < len; i++) {
			subArr.add(source.get(i + start));
		}

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, subArr)))));
	}

}

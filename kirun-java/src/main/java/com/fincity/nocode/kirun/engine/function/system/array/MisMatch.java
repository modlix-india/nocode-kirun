package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.stream.ArrayUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class MisMatch extends AbstractArrayFunction {

	public MisMatch() {
		super("MisMatch", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_INT_FIND_FROM, PARAMETER_ARRAY_SECOND_SOURCE,
				PARAMETER_INT_SECOND_SOURCE_FROM, PARAMETER_INT_LENGTH), EVENT_RESULT_INTEGER);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray firstSource = context.getArguments().get(PARAMETER_ARRAY_SOURCE.getParameterName()).getAsJsonArray();

		JsonPrimitive firstFind = context.getArguments().get(PARAMETER_INT_FIND_FROM.getParameterName())
				.getAsJsonPrimitive();

		JsonArray secondSource = context.getArguments().get(PARAMETER_ARRAY_SECOND_SOURCE.getParameterName())
				.getAsJsonArray();

		JsonPrimitive secondFind = context.getArguments().get(PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName())
				.getAsJsonPrimitive();

		JsonPrimitive length = context.getArguments().get(PARAMETER_INT_LENGTH.getParameterName()).getAsJsonPrimitive();

		// write check conditions

		int first = firstFind.getAsInt() < firstSource.size() && firstFind.getAsInt() > 0 ? firstFind.getAsInt() : 0;
		int second = secondFind.getAsInt() < secondSource.size() && secondFind.getAsInt() > 0 ? secondFind.getAsInt()
				: 0;

		if (first + length.getAsInt() >= firstSource.size() || second + length.getAsInt() > secondSource.size())
			throw new KIRuntimeException(
					"The size of the array for first and second which was being requested is more than size of the given array");

		return Mono.just(new FunctionOutput(
				List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME,
						new JsonPrimitive(Arrays.mismatch(ArrayUtil.jsonArrayToArray(firstSource), first,
								first + length.getAsInt(), ArrayUtil.jsonArrayToArray(secondSource), second,
								second + length.getAsInt())))))));
	}

}

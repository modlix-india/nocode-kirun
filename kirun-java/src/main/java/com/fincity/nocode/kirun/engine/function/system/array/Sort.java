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

public class Sort extends Max {

	protected Sort() {
		super("Sort", List.of(PARAMETER_ARRAY_SOURCE_PRIMITIVE, PARAMETER_INT_FIND_FROM, PARAMETER_INT_LENGTH,
		        PARAMETER_BOOLEAN_ASCENDING), EVENT_RESULT_EMPTY);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray source = context.getArguments()
		        .get(PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName())
		        .getAsJsonArray();

		JsonPrimitive startPosition = context.getArguments()
		        .get(PARAMETER_INT_FIND_FROM.getParameterName())
		        .getAsJsonPrimitive();

		JsonPrimitive length = context.getArguments()
		        .get(PARAMETER_INT_LENGTH.getParameterName())
		        .getAsJsonPrimitive();

		boolean ascending = context.getArguments()
		        .get(PARAMETER_BOOLEAN_ASCENDING.getParameterName())
		        .getAsBoolean();

		if (source.isJsonNull() || source.isEmpty())
			throw new KIRuntimeException("Expected a source of an array but not found any");

		int start = startPosition.getAsInt();

		int len = length.getAsInt();

		if (len == -1)
			len = source.size() - start;

		if (start < 0 || start >= source.size() || start + len > source.size())
			throw new KIRuntimeException(
			        "Given start point is more than the size of the array or not available at that point");

		JsonPrimitive[] elements = ArrayUtil.jsonArrayToPrimitive(source);

		Arrays.sort(elements, start, start + len, ascending ? this::compareTo : (o1, o2) -> -this.compareTo(o1, o2));

		for (int i = start; i < start + len; i++) {
			source.set(i, elements[i]);
		}

		return Mono
		        .just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_EMPTY.getName(), source)))));
	}

}

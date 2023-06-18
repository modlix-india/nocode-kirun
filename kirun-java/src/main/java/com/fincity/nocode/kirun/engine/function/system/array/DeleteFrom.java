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

public class DeleteFrom extends AbstractArrayFunction {

	public DeleteFrom() {
		super("DeleteFrom", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_INT_SOURCE_FROM, PARAMETER_INT_LENGTH),
				EVENT_RESULT_ARRAY);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray source = context.getArguments()
				.get(PARAMETER_ARRAY_SOURCE.getParameterName())
				.getAsJsonArray();

		JsonPrimitive from = context.getArguments()
				.get(PARAMETER_INT_SOURCE_FROM.getParameterName())
				.getAsJsonPrimitive();

		JsonPrimitive length = context.getArguments()
				.get(PARAMETER_INT_LENGTH.getParameterName())
				.getAsJsonPrimitive();

		if (source.size() == 0)
			throw new KIRuntimeException("There are no elements to be deleted");

		int start = from.getAsInt();

		if (start >= source.size() || start < 0)
			throw new KIRuntimeException(
					"The int source for the array should be in between 0 and length of the array ");

		int len = length.getAsInt();

		if (len == -1)
			len = source.size() - start;

		if (start + len > source.size())
			throw new KIRuntimeException("Requested length to be deleted is more than the size of array ");
		source = duplicateArray(source);
		while (len > 0) {
			source.remove(start);
			len--;
		}

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, source)))));
	}

}

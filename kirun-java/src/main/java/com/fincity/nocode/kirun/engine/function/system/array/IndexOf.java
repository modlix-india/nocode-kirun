package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.primitive.PrimitiveUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class IndexOf extends AbstractArrayFunction {

	public IndexOf() {
		super("IndexOf", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_ANY_NOT_NULL, PARAMETER_INT_FIND_FROM),
		        EVENT_RESULT_INTEGER);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray source = context.getArguments()
		        .get(PARAMETER_ARRAY_SOURCE.getParameterName())
		        .getAsJsonArray();

		var find = context.getArguments()
		        .get(PARAMETER_ANY_NOT_NULL.getParameterName());

		JsonPrimitive srcFrom = context.getArguments()
		        .get(PARAMETER_INT_FIND_FROM.getParameterName())
		        .getAsJsonPrimitive();

		if (source.isEmpty())
			return Mono.just(new FunctionOutput(
			        List.of(EventResult.outputOf(Map.of(EVENT_RESULT_INTEGER.getName(), new JsonPrimitive(-1))))));

		int len = srcFrom.getAsInt();

		if (len < 0 || len > source.size())
			throw new KIRuntimeException(
			        "The size of the search index of the array is greater than the size of the array");

		int index = -1;

		for (int i = len; i < source.size(); i++) {

			if (PrimitiveUtil.compare(source.get(i), find) == 0) {
				index = i;
				break;
			}

		}

		return Mono.just(new FunctionOutput(
		        List.of(EventResult.outputOf(Map.of(EVENT_RESULT_INTEGER.getName(), new JsonPrimitive(index))))));
	}
}

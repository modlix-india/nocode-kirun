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

public class IndexOfArray extends AbstractArrayFunction {

	public IndexOfArray() {
		super("IndexOfArray", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_ARRAY_SECOND_SOURCE, PARAMETER_INT_FIND_FROM),
		        EVENT_RESULT_INTEGER);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray source = context.getArguments()
		        .get(PARAMETER_ARRAY_SOURCE.getParameterName())
		        .getAsJsonArray();

		JsonArray secondSource = context.getArguments()
		        .get(PARAMETER_ARRAY_SECOND_SOURCE.getParameterName())
		        .getAsJsonArray();

		JsonPrimitive from = context.getArguments()
		        .get(PARAMETER_INT_FIND_FROM.getParameterName())
		        .getAsJsonPrimitive();

		if (source.isEmpty() || secondSource.isEmpty())
			return Mono.just(new FunctionOutput(
			        List.of(EventResult.outputOf(Map.of(EVENT_RESULT_INTEGER.getName(), new JsonPrimitive(-1))))));

		if (from.getAsInt() < 0 || from.getAsInt() > source.size() || source.size() < secondSource.size())
			throw new KIRuntimeException("Given from second source is more than the size of the source array");

		int len = from.getAsInt();
		int secondSourceSize = secondSource.size();
		int index = -1;

		for (int i = len; i < source.size(); i++) {
			int j = 0;

			if (PrimitiveUtil.compare(source.get(i), secondSource.get(j)) == 0) {
				while (j < secondSourceSize) {
					if (PrimitiveUtil.compare(source.get(i + j), secondSource.get(j)) != 0) {
						break;
					}
					j++;
				}
				if (j == secondSourceSize) {
					index = i;
					break;
				}
			}
		}

		return Mono.just(new FunctionOutput(
		        List.of(EventResult.outputOf(Map.of(EVENT_RESULT_INTEGER.getName(), new JsonPrimitive(index))))));
	}
}

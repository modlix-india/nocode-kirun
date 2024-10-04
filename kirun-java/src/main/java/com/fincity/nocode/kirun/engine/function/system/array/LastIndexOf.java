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

public class LastIndexOf extends AbstractArrayFunction {

	public LastIndexOf() {
		super("LastIndexOf", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_ANY_ELEMENT_OBJECT, PARAMETER_INT_FIND_FROM),
				EVENT_RESULT_INTEGER);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray source = context.getArguments()
				.get(PARAMETER_ARRAY_SOURCE.getParameterName())
				.getAsJsonArray();

		var find = context.getArguments()
				.get(PARAMETER_ANY_ELEMENT_OBJECT.getParameterName());

		JsonPrimitive length = context.getArguments()
				.get(PARAMETER_INT_FIND_FROM.getParameterName())
				.getAsJsonPrimitive();

		if (source.size() == 0)
			return Mono.just(new FunctionOutput(
					List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(-1))))));

		int len = length.getAsInt();

		if (len < 0 || len > source.size())
			throw new KIRuntimeException(
					"The value of length shouldn't the exceed the size of the array or shouldn't be in terms");

		int index = -1;

		if (find.isJsonNull())
			throw new KIRuntimeException("Please provide the valid find object or primitive in order to verify");

		for (int i = source.size() - 1; i >= len; i--) {
			if (PrimitiveUtil.compare(source.get(i), find) == 0) {
				index = i;
				break;
			}
		}

		return Mono.just(new FunctionOutput(
				List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(index))))));
	}
}

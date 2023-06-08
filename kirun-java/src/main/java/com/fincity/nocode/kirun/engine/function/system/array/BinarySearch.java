package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.primitive.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.util.stream.ArrayUtil;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class BinarySearch extends AbstractArrayFunction {

	public BinarySearch() {
		super("BinarySearch", List.of(PARAMETER_ARRAY_SOURCE_PRIMITIVE, PARAMETER_INT_SOURCE_FROM,
		        PARAMETER_FIND_PRIMITIVE, PARAMETER_INT_LENGTH), EVENT_INDEX);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		var source = ArrayUtil.jsonArrayToArray(context.getArguments()
		        .get(PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName())
		        .getAsJsonArray());
		int start = context.getArguments()
		        .get(PARAMETER_INT_SOURCE_FROM.getParameterName())
		        .getAsInt();
		var find = context.getArguments()
		        .get(PARAMETER_FIND_PRIMITIVE.getParameterName());
		int end = context.getArguments()
		        .get(PARAMETER_INT_LENGTH.getParameterName())
		        .getAsInt();

		if (source.length == 0 || start < 0 || start > source.length) {
			throw new KIRuntimeException("Search source array cannot be empty");
		}

		if (end == -1)
			end = source.length - start;

		end = start + end;

		if (end > source.length)
			throw new KIRuntimeException("End point for array cannot be more than the size of the source array");

		int index = -1;

		while (start <= end) {
			int mid = (start + end) / 2;
			if (PrimitiveUtil.compare(source[mid], find) == 0) {
				index = mid;
				break;
			} else if (PrimitiveUtil.compare(source[mid], find) > 0)
				end = mid - 1;
			else
				start = mid + 1;
		}

		return Mono.just(
		        new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_INDEX_NAME, new JsonPrimitive(index))))));
	}

}

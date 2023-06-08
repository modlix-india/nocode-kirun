package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.stream.ArrayUtil;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonArray;

import reactor.core.publisher.Mono;

public class Copy extends AbstractArrayFunction {

	public Copy() {
		super("Copy", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_INT_SOURCE_FROM, PARAMETER_INT_LENGTH,
		        PARAMETER_BOOLEAN_DEEP_COPY), EVENT_RESULT_ARRAY);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		var source = ArrayUtil.jsonArrayToArray(context.getArguments()
		        .get(PARAMETER_ARRAY_SOURCE.getParameterName())
		        .getAsJsonArray());
		var srcfrom = context.getArguments()
		        .get(PARAMETER_INT_SOURCE_FROM.getParameterName())
		        .getAsInt();
		var length = context.getArguments()
		        .get(PARAMETER_INT_LENGTH.getParameterName())
		        .getAsInt();

		if (length == -1)
			length = source.length - srcfrom;

		if (srcfrom + length > source.length)
			throw new KIRuntimeException(
			        StringFormatter.format("Array has no elements from $ to $ as the array size is $", srcfrom,
			                srcfrom + length, source.length));

		var deep = context.getArguments()
		        .get(PARAMETER_BOOLEAN_DEEP_COPY.getParameterName())
		        .getAsBoolean();

		JsonArray ja = new JsonArray(length);

		for (int i = srcfrom; i < srcfrom + length; i++) {
			ja.add(deep ? source[i].deepCopy() : source[i]);
		}

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, ja)))));
	}

}

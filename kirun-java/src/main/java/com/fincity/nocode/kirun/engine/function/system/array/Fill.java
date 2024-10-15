package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonNull;

import reactor.core.publisher.Mono;

public class Fill extends AbstractArrayFunction {

	public Fill() {
		super("Fill",
				List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_INT_SOURCE_FROM, PARAMETER_INT_LENGTH,
						PARAMETER_ANY_ELEMENT),
				EVENT_RESULT_ARRAY);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		var source = context.getArguments()
				.get(PARAMETER_ARRAY_SOURCE.getParameterName())
				.getAsJsonArray();
		var srcfrom = context.getArguments()
				.get(PARAMETER_INT_SOURCE_FROM.getParameterName())
				.getAsInt();
		var length = context.getArguments()
				.get(PARAMETER_INT_LENGTH.getParameterName())
				.getAsInt();
		var element = context.getArguments()
				.get(PARAMETER_ANY_ELEMENT.getParameterName());

		if (length == -1)
			length = source.size() - srcfrom;

		int add = (srcfrom + length) - source.size();

		source = duplicateArray(source);
		if (add > 0) {
			for (int i = 0; i < add; i++)
				source.add(JsonNull.INSTANCE);
		}

		for (int i = srcfrom; i < (srcfrom + length); i++) {
			source.set(i, element.deepCopy());
		}

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, source)))));
	}

}

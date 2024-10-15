package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;

import reactor.core.publisher.Mono;

public class InsertLast extends AbstractArrayFunction {

	public InsertLast() {
		super("InsertLast", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_ANY_ELEMENT), EVENT_RESULT_ARRAY);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray source = context.getArguments()
				.get(PARAMETER_ARRAY_SOURCE.getParameterName())
				.getAsJsonArray();

		var output = context.getArguments()
				.get(PARAMETER_ANY_ELEMENT_OBJECT.getParameterName());

		source = duplicateArray(source);
		source.add(output);

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, source)))));

	}

}

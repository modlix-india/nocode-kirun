package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;

import reactor.core.publisher.Mono;

public class DeleteLast extends AbstractArrayFunction {

	public DeleteLast() {
		super("DeleteLast", List.of(PARAMETER_ARRAY_SOURCE), EVENT_RESULT_EMPTY);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray source = context.getArguments()
		        .get(PARAMETER_ARRAY_SOURCE.getParameterName())
		        .getAsJsonArray();

		if (source.isEmpty())
			throw new KIRuntimeException("Given source array is empty");

		source.remove(source.size() - 1);

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of()))));
	}

}

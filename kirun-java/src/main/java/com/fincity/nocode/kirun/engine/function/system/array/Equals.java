package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class Equals extends AbstractArrayFunction {

	public Equals() {
		super("Equals", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_INT_SOURCE_FROM, PARAMETER_ARRAY_FIND,
		        PARAMETER_INT_FIND_FROM, PARAMETER_INT_LENGTH), EVENT_RESULT_BOOLEAN);
	}

	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		Compare compare = new Compare();

		return compare.execute(context)
		        .map(fo ->
				{
			        Map<String, JsonElement> resultMap = fo.allResults()
			                .get(0)
			                .getResult();

			        int v = resultMap.get(EVENT_RESULT_NAME)
			                .getAsJsonPrimitive()
			                .getAsInt();

			        return new FunctionOutput(
			                List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(v == 0)))));
		        });
	}

}

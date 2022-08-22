package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.primitive.PrimitiveUtil;
import com.google.gson.JsonArray;

public class Delete extends AbstractArrayFunction {

	public Delete() {
		super("Delete", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_ANY_VAR_ARGS), EVENT_RESULT_EMPTY);
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		JsonArray source = context.getArguments().get(PARAMETER_ARRAY_SOURCE.getParameterName()).getAsJsonArray();

		var receivedArgs = context.getArguments().get(PARAMETER_ANY_VAR_ARGS.getParameterName());

		if (receivedArgs == null || receivedArgs.isJsonNull())
			throw new KIRuntimeException("The deletable var args are empty. So cannot be proceeded further.");

		JsonArray deletable = receivedArgs.getAsJsonArray();

		if (source.isEmpty() || deletable.isEmpty())
			throw new KIRuntimeException("Expected a source or deletable for an array but not found any");

		Set<Integer> indexes = new HashSet<>();
		JsonArray duplicateSource = new JsonArray();
		duplicateSource.addAll(source); // to have reference as the size of the source is continuously changing after
										// removing any index or element

		for (int i = source.size() - 1; i >= 0; i--) {
			for (int j = 0; j < deletable.size(); j++) {
				if (!indexes.contains(i) && (PrimitiveUtil.compare(source.get(i), deletable.get(j)) == 0))
					indexes.add(i);
			}
		}

		indexes.stream().forEach(index -> source.remove(duplicateSource.get(index)));

		return new FunctionOutput(List.of(EventResult.outputOf(Map.of())));
	}

}

package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;

import com.google.gson.JsonArray;

public class Delete extends AbstractArrayFunction {

	public Delete() {
		super("Delete", List.of(PARAMETER_ARRAY_SOURCE_PRIMITIVE, PARAMETER_ARRAY_SECOND_SOURCE), EVENT_RESULT_EMPTY);
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		JsonArray source = context.getArguments().get(PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName())
				.getAsJsonArray();

		JsonArray deletable = context.getArguments().get(PARAMETER_ARRAY_SECOND_SOURCE.getParameterName())
				.getAsJsonArray();

		if (source.isEmpty() || deletable.isEmpty() || deletable.size() > source.size())
			throw new KIRuntimeException(
					"Expected a source or deletable for an array but not found any or the deletable size of the array is more than the source array");

		int deletableSize = deletable.size();

		int index = -1;

		for (int i = 0; i < source.size(); i++) {
			int j = 0;
			if (!source.get(i).isJsonNull() && !deletable.get(j).isJsonNull()
					&& source.get(i).equals(deletable.get(j))) {
				while (j < deletableSize) {
					if (source.get(i).isJsonNull() || deletable.get(j).isJsonNull()
							|| !source.get(i + j).equals(deletable.get(j))) {
						break;
					}
					j++;
				}
				if (j == deletableSize) {
					index = i;
					break;
				}
			}
		}

		if (index != -1) {
			for (int i = index; i <= deletableSize; i++) {
				source.remove(index);
			}
		}

		return new FunctionOutput(List.of(EventResult.outputOf(Map.of())));
	}

}

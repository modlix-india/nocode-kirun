package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.stream.ArrayUtil;
import com.google.gson.JsonPrimitive;

public class BinarySearch extends AbstractArrayFunction {

	public BinarySearch() {
		super("BinarySearch",
		        List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_INT_SOURCE_FROM, PARAMETER_ARRAY_FIND, PARAMETER_INT_LENGTH),
		        EVENT_INDEX);
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {
		var source = ArrayUtil.jsonArrayToArray(context.getArguments()
		        .get(PARAMETER_ARRAY_SOURCE.getParameterName())
		        .getAsJsonArray());
		var from = context.getArguments()
		        .get(PARAMETER_INT_SOURCE_FROM.getParameterName())
		        .getAsInt();
		var find = ArrayUtil.jsonArrayToArray(context.getArguments()
		        .get(PARAMETER_ARRAY_FIND.getParameterName())
		        .getAsJsonArray());
		var length = context.getArguments()
		        .get(PARAMETER_INT_LENGTH.getParameterName())
		        .getAsInt();

		if (source.length == 0) {
			throw new KIRuntimeException("Search source array cannot be empty");
		}

		if (find.length == 0) {
			throw new KIRuntimeException("Find array cannot be empty");
		}

		if (length == -1)
			length = source.length;

		if (find.length > (length - from))
			throw new KIRuntimeException("Find array is larger than the source array");

		return new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_INDEX_NAME, new JsonPrimitive(
		        Arrays.binarySearch(source, from, length + from, find.length == 1 ? find[0] : find))))));
	}

}

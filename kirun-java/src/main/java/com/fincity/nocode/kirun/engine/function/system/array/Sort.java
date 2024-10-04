package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.ObjectValueSetterExtractor;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.stream.ArrayUtil;
import com.fincity.nocode.kirun.engine.util.string.StringUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class Sort extends Max {

	private ObjectValueSetterExtractor ove;
	private static String DATA = "Data.";

	public Sort() {
		super("Sort", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_INT_FIND_FROM, PARAMETER_INT_LENGTH,
				PARAMETER_BOOLEAN_ASCENDING, PARAMETER_KEY_PATH), EVENT_RESULT_ARRAY);
		this.ove = new ObjectValueSetterExtractor(new JsonObject(), "Data.");
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray source = context.getArguments()
				.get(PARAMETER_ARRAY_SOURCE.getParameterName())
				.getAsJsonArray();

		JsonPrimitive startPosition = context.getArguments()
				.get(PARAMETER_INT_FIND_FROM.getParameterName())
				.getAsJsonPrimitive();

		JsonPrimitive length = context.getArguments()
				.get(PARAMETER_INT_LENGTH.getParameterName())
				.getAsJsonPrimitive();

		boolean ascending = context.getArguments()
				.get(PARAMETER_BOOLEAN_ASCENDING.getParameterName())
				.getAsBoolean();

		String keyPath = context.getArguments()
				.get(PARAMETER_KEY_PATH.getParameterName())
				.getAsString();

		if (source.isJsonNull() || source.isEmpty())
			throw new KIRuntimeException("Expected a source of an array but not found any");

		int start = startPosition.getAsInt();

		int len = length.getAsInt();

		if (len == -1)
			len = source.size() - start;

		if (start < 0 || start >= source.size() || start + len > source.size())
			throw new KIRuntimeException(
					"Given start point is more than the size of the array or not available at that point");

		JsonElement[] elements = ArrayUtil.jsonArrayToArray(source);
		Arrays.sort(elements, start, start + len, (o1, o2) -> this.compareFunction(o1, o2, ascending, keyPath));
		source = new JsonArray(elements.length);

		for (int i = 0; i < elements.length; i++) {
			source.add(elements[i]);
		}

		return Mono
				.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, source)))));
	}

	private int compareFunction(JsonElement a, JsonElement b, boolean ascending, String keyPath) {
		ObjectValueSetterExtractor ove = new ObjectValueSetterExtractor(new JsonObject(), "Data.");
		if (a.isJsonObject() && b.isJsonObject() && !StringUtil.isNullOrBlank(keyPath)) {
			JsonObject current = new JsonObject();
			current.add("a", a);
			current.add("b", b);
			ove.setStore(current);
			var aVal = ove.getValue(DATA + "a." + keyPath);
			var bVal = ove.getValue(DATA + "b." + keyPath);
			JsonPrimitive aValue = aVal.isJsonPrimitive() ? aVal.getAsJsonPrimitive() : null;
			JsonPrimitive bValue = bVal.isJsonPrimitive() ? bVal.getAsJsonPrimitive() : null;
			return ascending ? compareTo(aValue, bValue) : -compareTo(aValue, bValue);
		}

		return ascending
				? compareTo(a.isJsonPrimitive() ? a.getAsJsonPrimitive() : null,
						b.isJsonPrimitive() ? b.getAsJsonPrimitive() : null)
				: -compareTo(a.isJsonPrimitive() ? a.getAsJsonPrimitive() : null,
						b.isJsonPrimitive() ? b.getAsJsonPrimitive() : null);
	}

}

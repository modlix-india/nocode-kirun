package com.fincity.nocode.kirun.engine.runtime.expression.operators.ternary;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.runtime.expression.Operation;
import com.fincity.nocode.kirun.engine.util.stream.TriFunction;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;

@FunctionalInterface
public interface TernaryOperator extends TriFunction<JsonElement, JsonElement, JsonElement, JsonElement> {

	public default void nullCheck(JsonElement e1, JsonElement e2, JsonElement e3, Operation op) {
		if (e1 == null || e1.isJsonNull() || e2 == null || e2.isJsonNull() || e3 == null || e3.isJsonNull())
			throw new ExecutionException(
			        StringFormatter.format("$ cannot be applied to a null value", op.getOperatorName()));
	}
}

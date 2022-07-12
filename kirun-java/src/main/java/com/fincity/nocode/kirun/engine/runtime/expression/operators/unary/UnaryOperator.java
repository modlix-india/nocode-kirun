package com.fincity.nocode.kirun.engine.runtime.expression.operators.unary;

import java.util.function.Function;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.runtime.expression.Operation;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;

@FunctionalInterface
public interface UnaryOperator extends Function<JsonElement, JsonElement> {

	public default void nullCheck(JsonElement element, Operation op) {
		if (element == null || element.isJsonNull())
			throw new ExecutionException(
			        StringFormatter.format("$ cannot be applied to a null value", op.getOperatorName()));
	}
}

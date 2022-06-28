package com.fincity.nocode.kirun.engine.runtime.expression.operators.binary;

import java.util.function.BiFunction;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.runtime.expression.Operation;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;

@FunctionalInterface
public interface BinaryOperator extends BiFunction<JsonElement, JsonElement, JsonElement> {

	public default void nullCheck(JsonElement e1, JsonElement e2, Operation unaryPlus) {
		if (e1 == null || e1.isJsonNull() || e2 == null || e2.isJsonNull())
			throw new ExecutionException(
			        StringFormatter.format("$ cannot be applied to a null value", unaryPlus.getOperatorName()));
	}
}

package com.fincity.nocode.kirun.engine.runtime.expression.operators.ternary;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ConditionalTernaryOperator implements TernaryOperator {

	@Override
	public JsonElement apply(JsonElement a, JsonElement b, JsonElement c) {

		if (a == null || a.isJsonNull())
			return c;

		if (!a.isJsonPrimitive()) {
			return b;
		}

		JsonPrimitive pa = a.getAsJsonPrimitive();

		if (pa.isBoolean())
			return pa.getAsBoolean() ? b : c;

		if (pa.isString())
			return pa.getAsString()
			        .length() != 0 ? b : c;

		if (pa.isNumber()) {

			return pa.getAsDouble() != 0.0d ? b : c;
		}

		return c;
	}
}

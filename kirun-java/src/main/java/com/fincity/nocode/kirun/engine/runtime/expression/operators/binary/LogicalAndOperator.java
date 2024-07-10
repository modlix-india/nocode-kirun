package com.fincity.nocode.kirun.engine.runtime.expression.operators.binary;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class LogicalAndOperator implements BinaryOperator {

	@Override
	public JsonElement apply(JsonElement t, JsonElement u) {

		boolean tBoolean = !(t == null || t.isJsonNull());
		if (tBoolean && t.isJsonPrimitive()) {
			JsonPrimitive jp = t.getAsJsonPrimitive();
			if (jp.isBoolean())
				tBoolean = jp.getAsBoolean();
			else if (jp.isNumber())
				tBoolean = jp.getAsDouble() != 0.0d;
		}

		if (!tBoolean)
			return new JsonPrimitive(false);

		boolean uBoolean = !(u == null || u.isJsonNull());
		if (uBoolean && u.isJsonPrimitive()) {
			JsonPrimitive jp = u.getAsJsonPrimitive();
			if (jp.isBoolean())
				uBoolean = jp.getAsBoolean();
			else if (jp.isNumber())
				uBoolean = jp.getAsDouble() != 0.0d;
		}

		return new JsonPrimitive(tBoolean && uBoolean);
	}
}

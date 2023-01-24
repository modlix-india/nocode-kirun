package com.fincity.nocode.kirun.engine.runtime.expression.operators.unary;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class LogicalNotOperator implements UnaryOperator {

	@Override
	public JsonElement apply(JsonElement t) {

		if (t == null || t.isJsonNull())
			return new JsonPrimitive(true);
		
		if (!t.isJsonPrimitive())
			return new JsonPrimitive(false);
		
		JsonPrimitive jp = t.getAsJsonPrimitive();
		
		if (jp.isBoolean()) return new JsonPrimitive(!jp.getAsBoolean());
		
		if (jp.isString()) return new JsonPrimitive(false);
		
		return new JsonPrimitive(jp.getAsDouble() == 0.0d);
	}
}

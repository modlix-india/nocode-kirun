package com.fincity.nocode.kirun.engine.runtime.expression.operators.binary;

import com.google.gson.JsonElement;

public class LogicalNullishCoalescingOperator implements BinaryOperator {

	@Override
	public JsonElement apply(JsonElement t, JsonElement u) {

		if (t == null || t.isJsonNull()) return u;
		
		return t;
	}
}

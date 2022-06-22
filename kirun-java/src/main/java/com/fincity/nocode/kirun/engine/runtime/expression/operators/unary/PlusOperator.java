package com.fincity.nocode.kirun.engine.runtime.expression.operators.unary;

import com.fincity.nocode.kirun.engine.function.util.PrimitiveUtil;
import com.google.gson.JsonElement;

public class PlusOperator implements UnaryOperator {

	@Override
	public JsonElement apply(JsonElement t) {

		PrimitiveUtil.findPrimitiveNumberType(t.getAsJsonPrimitive());
		return t;
	}
}

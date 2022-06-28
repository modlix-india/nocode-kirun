package com.fincity.nocode.kirun.engine.runtime.expression.operators.unary;

import com.fincity.nocode.kirun.engine.function.util.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.runtime.expression.Operation;
import com.google.gson.JsonElement;

public class ArithmeticUnaryPlusOperator implements UnaryOperator {

	@Override
	public JsonElement apply(JsonElement t) {
		
		this.nullCheck(t, Operation.UNARY_PLUS);

		PrimitiveUtil.findPrimitiveNumberType(t.getAsJsonPrimitive());
		return t;
	}
}

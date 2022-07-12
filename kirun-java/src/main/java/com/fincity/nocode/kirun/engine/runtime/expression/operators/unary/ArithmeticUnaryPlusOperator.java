package com.fincity.nocode.kirun.engine.runtime.expression.operators.unary;

import com.fincity.nocode.kirun.engine.runtime.expression.Operation;
import com.fincity.nocode.kirun.engine.util.primitive.PrimitiveUtil;
import com.google.gson.JsonElement;

public class ArithmeticUnaryPlusOperator implements UnaryOperator {

	@Override
	public JsonElement apply(JsonElement t) {
		
		this.nullCheck(t, Operation.UNARY_PLUS);

		PrimitiveUtil.findPrimitiveNumberType(t.getAsJsonPrimitive());
		return t;
	}
}

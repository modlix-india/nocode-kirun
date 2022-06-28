package com.fincity.nocode.kirun.engine.runtime.expression.operators.unary;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.function.util.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.runtime.expression.Operation;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuple2;

public class BitwiseComplementOperator implements UnaryOperator {

	@Override
	public JsonElement apply(JsonElement t) {
		
		this.nullCheck(t, Operation.UNARY_BITWISE_COMPLEMENT);

		Tuple2<SchemaType, Number> primitive = PrimitiveUtil.findPrimitiveNumberType(t.getAsJsonPrimitive());
		
		switch (primitive.getT1()) {
		case LONG:
			return new JsonPrimitive(~((Long) primitive.getT2()));
		case INTEGER:
			return new JsonPrimitive(~((Integer) primitive.getT2()));
		default:
			throw new ExecutionException(StringFormatter.format("Unable to apply bitwise operator on $", t));
		}
	}
}

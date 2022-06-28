package com.fincity.nocode.kirun.engine.runtime.expression.operators.unary;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.function.util.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.runtime.expression.Operation;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuple2;

public class LogicalNotOperator implements UnaryOperator {

	@Override
	public JsonElement apply(JsonElement t) {

		this.nullCheck(t, Operation.UNARY_LOGICAL_NOT);
		
		Tuple2<SchemaType, Object> primitive = PrimitiveUtil.findPrimitiveNullAsBoolean(t.getAsJsonPrimitive());

		if (primitive.getT1() == SchemaType.BOOLEAN)
			return new JsonPrimitive(!((Boolean) primitive.getT2()));

		throw new ExecutionException(StringFormatter.format("Unable to apply not operator on $", t));
	}
}

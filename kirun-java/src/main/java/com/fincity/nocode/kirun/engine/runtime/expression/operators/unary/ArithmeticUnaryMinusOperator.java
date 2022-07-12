package com.fincity.nocode.kirun.engine.runtime.expression.operators.unary;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.runtime.expression.Operation;
import com.fincity.nocode.kirun.engine.util.primitive.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuple2;

public class ArithmeticUnaryMinusOperator implements UnaryOperator {

	@Override
	public JsonElement apply(JsonElement t) {

		this.nullCheck(t, Operation.UNARY_MINUS);

		Tuple2<SchemaType, Number> primitiveTypeTuple = PrimitiveUtil.findPrimitiveNumberType(t.getAsJsonPrimitive());
		switch (primitiveTypeTuple.getT1()) {
		case DOUBLE:
			return new JsonPrimitive(((Double) primitiveTypeTuple.getT2()) * -1);
		case LONG:
			return new JsonPrimitive(((Long) primitiveTypeTuple.getT2()) * -1);
		case FLOAT:
			return new JsonPrimitive(((Float) primitiveTypeTuple.getT2()) * -1);
		case INTEGER:
			return new JsonPrimitive(((Integer) primitiveTypeTuple.getT2()) * -1);
		default:
			throw new ExecutionException(StringFormatter.format("Unable to apply minus operator on $", t));
		}
	}
}

package com.fincity.nocode.kirun.engine.runtime.expression.operators.unary;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.function.util.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class MinusOperator implements UnaryOperator {

	@Override
	public JsonElement apply(JsonElement t) {

		SchemaType type = PrimitiveUtil.findPrimitiveNumberType(t.getAsJsonPrimitive());
		switch (type) {
		case DOUBLE:
			return new JsonPrimitive(t.getAsDouble() * -1);
		case LONG:
			return new JsonPrimitive(t.getAsLong() * -1);
		case FLOAT:
			return new JsonPrimitive(t.getAsFloat() * -1);
		case INTEGER:
			return new JsonPrimitive(t.getAsInt() * -1);
		default:
			throw new ExecutionException(StringFormatter.format("Unable to apply minus operator on $", t));
		}
	}
}

package com.fincity.nocode.kirun.engine.runtime.expression.operators.binary;

import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.BOOLEAN;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.function.util.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuple2;

public class LogicalOrOperator implements BinaryOperator {

	@Override
	public JsonElement apply(JsonElement t, JsonElement u) {

		Tuple2<SchemaType, Object> tType = PrimitiveUtil.findPrimitiveNullAsBoolean(t);
		Tuple2<SchemaType, Object> uType = PrimitiveUtil.findPrimitiveNullAsBoolean(u);
		
		if (tType.getT1() != BOOLEAN)
			throw new ExecutionException(StringFormatter.format("Boolean value expected but found $", tType.getT2()));
		
		if (uType.getT1() != BOOLEAN)
			throw new ExecutionException(StringFormatter.format("Boolean value expected but found $", uType.getT2()));

		return new JsonPrimitive(((Boolean) tType.getT2()) || ((Boolean) uType.getT2()));
	}
}

package com.fincity.nocode.kirun.engine.runtime.expression.operators.binary;

import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.INTEGER;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.LONG;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.runtime.expression.Operation;
import com.fincity.nocode.kirun.engine.util.primitive.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuple2;

public class BitwiseLeftShiftOperator implements BinaryOperator {

	@Override
	public JsonElement apply(JsonElement t, JsonElement u) {

		this.nullCheck(t, u, Operation.BITWISE_LEFT_SHIFT);
		
		Tuple2<SchemaType, Number> tType = PrimitiveUtil.findPrimitiveNumberType(t);
		Tuple2<SchemaType, Number> uType = PrimitiveUtil.findPrimitiveNumberType(u);

		if ((tType.getT1() != LONG && tType.getT1() != INTEGER) || (uType.getT1() != LONG && uType.getT1() != INTEGER))
			throw new ExecutionException(StringFormatter.format("$ and $ has to be either integer or long type",
			        tType.getT2(), uType.getT2()));

		if (tType.getT1() == LONG || uType.getT1() == LONG)
			return new JsonPrimitive(tType.getT2()
			        .longValue() << uType.getT2()
			                .longValue());

		return new JsonPrimitive(tType.getT2()
		        .intValue() << uType.getT2()
		                .intValue());
	}
}

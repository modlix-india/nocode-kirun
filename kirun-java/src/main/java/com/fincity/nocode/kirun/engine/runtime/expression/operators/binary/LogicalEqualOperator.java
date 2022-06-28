package com.fincity.nocode.kirun.engine.runtime.expression.operators.binary;

import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.BOOLEAN;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.DOUBLE;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.FLOAT;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.LONG;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.STRING;

import com.fincity.nocode.kirun.engine.function.util.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuple2;

public class LogicalEqualOperator implements BinaryOperator {

	@Override
	public JsonElement apply(JsonElement t, JsonElement u) {

		Tuple2<SchemaType, Object> tType = PrimitiveUtil.findPrimitiveNullAsBoolean(t);
		Tuple2<SchemaType, Object> uType = PrimitiveUtil.findPrimitiveNullAsBoolean(u);

		if (tType.getT1() == STRING || uType.getT1() == STRING)
			return new JsonPrimitive(tType.getT2()
			        .toString()
			        .equals(uType.getT2()
			                .toString()));

		if (tType.getT1() == BOOLEAN || uType.getT1() == BOOLEAN)
			return new JsonPrimitive(
			        ((JsonPrimitive) tType.getT2()).getAsBoolean() == ((JsonPrimitive) uType.getT2()).getAsBoolean());

		Number tNumber = (Number) tType.getT2();
		Number uNumber = (Number) uType.getT2();

		if (tType.getT1() == DOUBLE || uType.getT1() == DOUBLE)
			return new JsonPrimitive(tNumber.doubleValue() == uNumber.doubleValue());

		if (tType.getT1() == FLOAT || uType.getT1() == FLOAT)
			return new JsonPrimitive(tNumber.floatValue() == uNumber.floatValue());

		if (tType.getT1() == LONG || uType.getT1() == LONG)
			return new JsonPrimitive(tNumber.longValue() == uNumber.longValue());

		return new JsonPrimitive(tNumber.intValue() == uNumber.intValue());
	}
}

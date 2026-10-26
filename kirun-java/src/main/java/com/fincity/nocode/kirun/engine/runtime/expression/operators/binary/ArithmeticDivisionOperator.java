package com.fincity.nocode.kirun.engine.runtime.expression.operators.binary;

import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.DOUBLE;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.FLOAT;

import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.runtime.expression.Operation;
import com.fincity.nocode.kirun.engine.util.primitive.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.util.primitive.WholeNumberArithmetic;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuple2;

public class ArithmeticDivisionOperator implements BinaryOperator {

	@Override
	public JsonElement apply(JsonElement t, JsonElement u) {
		
		this.nullCheck(t, u, Operation.DIVISION);

		Tuple2<SchemaType, Number> tType = PrimitiveUtil.findPrimitiveNumberType(t);
		Tuple2<SchemaType, Number> uType = PrimitiveUtil.findPrimitiveNumberType(u);

		Number tNumber = tType.getT2();
		Number uNumber = uType.getT2();

		if (tType.getT1() == DOUBLE || uType.getT1() == DOUBLE)
			return new JsonPrimitive(tNumber.doubleValue() / uNumber.doubleValue());

		if (tType.getT1() == FLOAT || uType.getT1() == FLOAT)
			return new JsonPrimitive(tNumber.floatValue() / uNumber.floatValue());

		// int and long: a remainder makes a double division (7 / 2 is 3.5, 1 / 3 is
		// 0.3333333333333333 as in kirun-js); an exact one stays whole.
		return WholeNumberArithmetic.divide(tNumber, uNumber);
	}
}

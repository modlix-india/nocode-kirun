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

public class ArithmeticSubtractionOperator implements BinaryOperator {

	@Override
	public JsonElement apply(JsonElement t, JsonElement u) {

		this.nullCheck(t, u, Operation.SUBTRACTION);
		
		Tuple2<SchemaType, Number> tType = PrimitiveUtil.findPrimitiveNumberType(t);
		Tuple2<SchemaType, Number> uType = PrimitiveUtil.findPrimitiveNumberType(u);

		Number tNumber = tType.getT2();
		Number uNumber = uType.getT2();

		if (tType.getT1() == DOUBLE || uType.getT1() == DOUBLE)
			return new JsonPrimitive(tNumber.doubleValue() - uNumber.doubleValue());

		if (tType.getT1() == FLOAT || uType.getT1() == FLOAT)
			return new JsonPrimitive(tNumber.floatValue() - uNumber.floatValue());

		// int and long: widen instead of wrapping (kirun-js numbers never wrap).
		return WholeNumberArithmetic.subtract(tNumber, uNumber);
	}
}

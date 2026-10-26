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

/**
 * The {@code //} operator as kirun-js has it: {@code Math.floor(t / u)} (7 // 2 is 3, -7 // 2 is
 * -4). A whole zero divisor fails with "/ by zero", as {@code /} does.
 */
public class ArithmeticIntegerDivisionOperator implements BinaryOperator {

	@Override
	public JsonElement apply(JsonElement t, JsonElement u) {

		this.nullCheck(t, u, Operation.INTEGER_DIVISION);

		Tuple2<SchemaType, Number> tType = PrimitiveUtil.findPrimitiveNumberType(t);
		Tuple2<SchemaType, Number> uType = PrimitiveUtil.findPrimitiveNumberType(u);

		Number tNumber = tType.getT2();
		Number uNumber = uType.getT2();

		if (tType.getT1() == DOUBLE || uType.getT1() == DOUBLE || tType.getT1() == FLOAT
		        || uType.getT1() == FLOAT) {
			double r = Math.floor(tNumber.doubleValue() / uNumber.doubleValue());
			if (Double.isFinite(r) && Math.abs(r) < 0x1p63)
				return new JsonPrimitive(WholeNumberArithmetic.whole((long) r));
			return new JsonPrimitive(r);
		}

		long x = tNumber.longValue();
		long y = uNumber.longValue();
		if (y == 0L)
			throw new ArithmeticException("/ by zero");
		if (x == Long.MIN_VALUE && y == -1L)
			return new JsonPrimitive(-(double) x);
		return new JsonPrimitive(WholeNumberArithmetic.whole(Math.floorDiv(x, y)));
	}
}

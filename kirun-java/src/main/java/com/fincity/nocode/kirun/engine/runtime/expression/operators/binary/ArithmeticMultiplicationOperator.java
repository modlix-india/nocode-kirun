package com.fincity.nocode.kirun.engine.runtime.expression.operators.binary;

import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.BOOLEAN;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.DOUBLE;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.FLOAT;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.LONG;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.STRING;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.runtime.expression.Operation;
import com.fincity.nocode.kirun.engine.util.primitive.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuple2;

public class ArithmeticMultiplicationOperator implements BinaryOperator {

	@Override
	public JsonElement apply(JsonElement t, JsonElement u) {

		this.nullCheck(t, u, Operation.MULTIPLICATION);

		Tuple2<SchemaType, Object> tType = PrimitiveUtil.findPrimitive(t);
		Tuple2<SchemaType, Object> uType = PrimitiveUtil.findPrimitive(u);

		if (tType.getT1() == BOOLEAN || uType.getT1() == BOOLEAN)
			throw new ExecutionException(
			        StringFormatter.format("Cannot multiply the values $ and $", tType.getT2(), uType.getT2()));

		if (tType.getT1() == STRING && uType.getT1() == STRING)
			throw new ExecutionException(
			        StringFormatter.format("Cannot multiply strings $ and $", tType.getT2(), uType.getT2()));

		if (tType.getT1() == STRING || uType.getT1() == STRING) {

			String str;
			Number num;
			if (tType.getT1() == STRING) {
				str = tType.getT2()
				        .toString();
				num = (Number) uType.getT2();
			} else {
				str = uType.getT2()
				        .toString();
				num = (Number) tType.getT2();
			}

			StringBuilder sb = new StringBuilder();

			long times = num.longValue();
			boolean reverse = false;

			if (num.doubleValue() < 0d) {
				reverse = true;
				times = -times;
			}

			while (times > 0) {
				sb.append(str);
				times--;
			}

			long chrs = (long) Math.floor(str.length() * (num.doubleValue() - num.longValue()));
			if (chrs < 0)
				chrs = -chrs;

			if (chrs != 0)
				sb.append(str.substring(0, (int) chrs));

			if (reverse)
				sb = sb.reverse();

			return new JsonPrimitive(sb.toString());
		}

		Number tNumber = (Number) tType.getT2();
		Number uNumber = (Number) uType.getT2();

		if (tType.getT1() == DOUBLE || uType.getT1() == DOUBLE)
			return new JsonPrimitive(tNumber.doubleValue() * uNumber.doubleValue());

		if (tType.getT1() == FLOAT || uType.getT1() == FLOAT)
			return new JsonPrimitive(tNumber.floatValue() * uNumber.floatValue());

		if (tType.getT1() == LONG || uType.getT1() == LONG)
			return new JsonPrimitive(tNumber.longValue() * uNumber.longValue());

		return new JsonPrimitive(tNumber.intValue() * uNumber.intValue());
	}
}

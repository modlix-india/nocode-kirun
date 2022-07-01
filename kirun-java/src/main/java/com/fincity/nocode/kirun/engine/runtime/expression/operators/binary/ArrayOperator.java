package com.fincity.nocode.kirun.engine.runtime.expression.operators.binary;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.function.util.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import reactor.util.function.Tuple2;

public class ArrayOperator implements BinaryOperator {

	@Override
	public JsonElement apply(JsonElement t, JsonElement u) {

		if (t == null || t.isJsonNull()) {
			throw new ExecutionException("Cannot apply array operator on a null value");
		}

		if (u == null || u.isJsonNull()) {
			throw new ExecutionException("Cannot retrive null index value");
		}

		if (t.isJsonPrimitive()) {
			throw new ExecutionException(StringFormatter.format("Cannot retrieve value from a primitive value $", t));
		}

		if (t.isJsonArray()) {

			Tuple2<SchemaType, Number> uNumber = PrimitiveUtil.findPrimitiveNumberType(u);

			if (uNumber.getT1() == SchemaType.FLOAT || uNumber.getT1() == SchemaType.DOUBLE) {
				throw new ExecutionException(StringFormatter.format("Cannot retrieve $ from the array", u));
			}

			int index = uNumber.getT2()
			        .intValue();

			JsonArray arr = t.getAsJsonArray();
			if (index >= arr.size())
				throw new ExecutionException(StringFormatter
				        .format("Cannot retrieve index $ from the array of length $", index, arr.size()));

			return arr.get(index);
		}

		JsonObject obj = t.getAsJsonObject();
		return obj.get(u.getAsString());
	}

}

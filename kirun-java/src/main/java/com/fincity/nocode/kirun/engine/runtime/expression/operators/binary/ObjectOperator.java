package com.fincity.nocode.kirun.engine.runtime.expression.operators.binary;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.function.util.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuple2;

public class ObjectOperator implements BinaryOperator {

	@Override
	public JsonElement apply(JsonElement t, JsonElement u) {

		if (t == null || t.isJsonNull()) {
			throw new ExecutionException("Cannot apply object operator on a null value");
		}

		if (u == null || u.isJsonNull()) {
			throw new ExecutionException("Cannot retrive null property value");
		}

		if (t.isJsonPrimitive()) {
			if (u.isJsonPrimitive()) {
				Tuple2<SchemaType, Number> number = PrimitiveUtil
				        .findPrimitiveNumberType(new JsonPrimitive(t.getAsString() + "." + u.getAsString()));

				if (number.getT1() == SchemaType.DOUBLE)
					return new JsonPrimitive(number.getT2()
					        .doubleValue());
				else if (number.getT1() == SchemaType.FLOAT)
					return new JsonPrimitive(number.getT2()
					        .floatValue());
				else
					throw new ExecutionException(StringFormatter.format("Unknown type $ found with value $",
					        number.getT1(), number.getT2()));
			}

			throw new ExecutionException(StringFormatter.format("Cannot retrieve the value $ from the object $", u, t));
		}

		if (t.isJsonArray()) {
			if ("length".equals(u.getAsString()))
				return new JsonPrimitive(t.getAsJsonArray()
				        .size());
			throw new ExecutionException("Cannot apply object operator on an array");
		}

		JsonObject obj = t.getAsJsonObject();
		return obj.get(u.getAsString());
	}
}

package com.fincity.nocode.kirun.engine.function.util;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LazilyParsedNumber;

public class PrimitiveUtil {

	public static SchemaType findPrimitiveNumberType(JsonPrimitive value) {

		if (value == null || !value.isNumber())
			throw new ExecutionException(StringFormatter.format("Unable to convert $ to a number.", value));

		try {
			Number number = value.getAsNumber();

			if (!(number instanceof LazilyParsedNumber))
				return baseNumberType(number);

			int ind = value.getAsString()
			        .indexOf('.');
			if (ind == -1) {

				Long num = number.longValue();
				int intNum = num.intValue();
				if (num == intNum)
					return SchemaType.INTEGER;
				return SchemaType.LONG;
			} else {

				Double d = number.doubleValue();
				Float f = d.floatValue();

				if (d == 0.0d)
					return SchemaType.FLOAT;

				return f != 0.0f && f != Float.POSITIVE_INFINITY && f != Float.NEGATIVE_INFINITY ? SchemaType.FLOAT
				        : SchemaType.DOUBLE;
			}
		} catch (Exception ex) {

			throw new ExecutionException(StringFormatter.format("Unable to convert $ to a number.", value), ex);
		}
	}

	private static SchemaType baseNumberType(Number number) {
		
		if (number instanceof Integer)
			return SchemaType.INTEGER;
		if (number instanceof Float)
			return SchemaType.FLOAT;
		if (number instanceof Long)
			return SchemaType.LONG;
		if (number instanceof Double)
			return SchemaType.DOUBLE;
		
		throw new ExecutionException(StringFormatter.format("Unable to identified the Number type of $", number));
	}

	private PrimitiveUtil() {
	}

	public static JsonPrimitive toPrimitiveType(Object e) {

		if (e instanceof Integer i)
			return new JsonPrimitive(i);
		if (e instanceof Float f)
			return new JsonPrimitive(f);
		if (e instanceof Long l)
			return new JsonPrimitive(l);
		if (e instanceof Double d)
			return new JsonPrimitive(d);

		throw new ExecutionException("Parameter is not a primitive type " + e);
	}
}

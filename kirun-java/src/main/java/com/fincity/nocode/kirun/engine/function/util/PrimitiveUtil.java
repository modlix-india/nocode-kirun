package com.fincity.nocode.kirun.engine.function.util;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.google.gson.JsonPrimitive;

public class PrimitiveUtil {

	public static SchemaType findPrimitiveType(JsonPrimitive value) {

		try {
			Double d = value.getAsDouble();
			Long l = d.longValue();
			if (d.doubleValue() == l.doubleValue()) {

				Integer i = l.intValue();

				if (l.longValue() == i.longValue())
					return SchemaType.INTEGER;
				else
					return SchemaType.LONG;
			} else {
				Float f = d.floatValue();
				if (f.doubleValue() == d.doubleValue())
					return SchemaType.FLOAT;
				else
					return SchemaType.DOUBLE;
			}
		} catch (Exception ex) {

			throw new ExecutionException("Unable to convert the number.", ex);
		}
	}

	private PrimitiveUtil() {
	}
}

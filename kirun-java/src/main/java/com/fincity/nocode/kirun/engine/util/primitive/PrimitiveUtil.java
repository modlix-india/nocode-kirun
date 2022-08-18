package com.fincity.nocode.kirun.engine.util.primitive;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LazilyParsedNumber;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class PrimitiveUtil {

	public static Tuple2<SchemaType, Object> findPrimitiveNullAsBoolean(JsonElement element) {

		if (element == null || element.isJsonNull())
			return Tuples.of(SchemaType.BOOLEAN, new JsonPrimitive(false));

		if (!element.isJsonPrimitive())
			throw new ExecutionException(StringFormatter.format("$ is not a primitive type", element));

		JsonPrimitive value = element.getAsJsonPrimitive();

		if (value.isBoolean())
			return Tuples.of(SchemaType.BOOLEAN, value.getAsBoolean());

		if (value.isString())
			return Tuples.of(SchemaType.STRING, value.getAsString());

		return findPrimitiveNumberType(value).mapT2(Number.class::cast);
	}

	public static Tuple2<SchemaType, Object> findPrimitive(JsonElement element) {

		if (element == null || element.isJsonNull())
			return Tuples.of(SchemaType.NULL, JsonNull.INSTANCE);

		if (!element.isJsonPrimitive())
			throw new ExecutionException(StringFormatter.format("$ is not a primitive type", element));

		JsonPrimitive value = element.getAsJsonPrimitive();

		if (value.isBoolean())
			return Tuples.of(SchemaType.BOOLEAN, value.getAsBoolean());

		if (value.isString())
			return Tuples.of(SchemaType.STRING, value.getAsString());

		return findPrimitiveNumberType(value).mapT2(Number.class::cast);
	}

	public static Tuple2<SchemaType, Number> findPrimitiveNumberType(JsonElement element) {

		if (element == null || !element.isJsonPrimitive())
			throw new ExecutionException(StringFormatter.format("Unable to convert $ to a number.", element));

		JsonPrimitive value = element.getAsJsonPrimitive();

		try {
			Number number = value.getAsNumber();

			if (!(number instanceof LazilyParsedNumber))
				return Tuples.of(baseNumberType(number), number);

			int ind = value.getAsString().indexOf('.');
			if (ind == -1) {

				Long num = number.longValue();
				int intNum = num.intValue();
				if (num == intNum)
					return Tuples.of(SchemaType.INTEGER, intNum);
				return Tuples.of(SchemaType.LONG, num);
			} else {

				Double d = number.doubleValue();
				Float f = d.floatValue();

				if (d == 0.0d)
					return Tuples.of(SchemaType.FLOAT, f);

				return f != 0.0f && f != Float.POSITIVE_INFINITY && f != Float.NEGATIVE_INFINITY
						? Tuples.of(SchemaType.FLOAT, f)
						: Tuples.of(SchemaType.DOUBLE, d);
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

	public static int comparePrimitive(JsonPrimitive pa, JsonPrimitive pb) {

		if (pa == pb)
			return 0;

		if (pa.isString() || pb.isString())
			return pa.getAsString().compareTo(pb.getAsString());

		if (pa.isBoolean() || pb.isBoolean())
			return pa.getAsBoolean() ? -1 : 1;

		Number a = pa.getAsNumber();
		Number b = pb.getAsNumber();

		if (a instanceof Double || b instanceof Double)
			return Double.compare(a.doubleValue(), b.doubleValue());
		else if (a instanceof Float || b instanceof Float)
			return Float.compare(a.floatValue(), b.floatValue());
		else if (a instanceof Long || b instanceof Long)
			return Long.compare(a.longValue(), b.longValue());

		return Integer.compare(a.intValue(), b.intValue());
	}

	public static int compare(JsonElement a, JsonElement b) {

		if (a == b)
			return 0;

		if (a == null || b == null)
			return a == null ? -1 : 1;

		if (a.isJsonNull() || b.isJsonNull()) {

			if (a.isJsonNull() && b.isJsonNull())
				return 0;

			return a.isJsonNull() ? -1 : 1;

		}

		if (a.isJsonPrimitive() || b.isJsonPrimitive()) {

			if (a.isJsonPrimitive() && b.isJsonPrimitive())
				return comparePrimitive(a.getAsJsonPrimitive(), b.getAsJsonPrimitive());

			return a.isJsonPrimitive() ? -1 : 1;

		}

		if (a.isJsonArray() || b.isJsonArray()) {

			if (a.isJsonArray() && b.isJsonArray()) {
				JsonArray ja = a.getAsJsonArray();

				JsonArray jb = b.getAsJsonArray();

				if (ja.size() != jb.size())
					return ja.size() - jb.size();

				for (int i = 0; i < ja.size(); i++) {

					int cmp = compare(ja.get(i), jb.get(i));

					if (cmp != 0)
						return cmp;

				}
				return 0;

			}

			return a.isJsonArray() ? -1 : 1;

		}

		JsonObject ja = a.getAsJsonObject();

		JsonObject jb = b.getAsJsonObject();

		if (ja.size() != jb.size())
			return ja.size() - jb.size();

		for (String k : ja.keySet()) {

			int cmp = compare(ja.get(k), jb.get(k));

			if (cmp != 0)
				return cmp;

		}
		return 0;
	}

	private PrimitiveUtil() {
	}
}

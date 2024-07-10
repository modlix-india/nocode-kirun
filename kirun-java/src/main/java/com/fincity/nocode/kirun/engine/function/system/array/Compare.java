package com.fincity.nocode.kirun.engine.function.system.array;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.stream.ArrayUtil;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class Compare extends AbstractArrayFunction {

	public Compare() {
		super("Compare", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_INT_SOURCE_FROM, PARAMETER_ARRAY_FIND,
				PARAMETER_INT_FIND_FROM, PARAMETER_INT_LENGTH), EVENT_RESULT_INTEGER);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
		var source = ArrayUtil.jsonArrayToArray(context.getArguments()
				.get(PARAMETER_ARRAY_SOURCE.getParameterName())
				.getAsJsonArray());
		var srcfrom = context.getArguments()
				.get(PARAMETER_INT_SOURCE_FROM.getParameterName())
				.getAsInt();
		var find = ArrayUtil.jsonArrayToArray(context.getArguments()
				.get(PARAMETER_ARRAY_FIND.getParameterName())
				.getAsJsonArray());
		var findfrom = context.getArguments()
				.get(PARAMETER_INT_FIND_FROM.getParameterName())
				.getAsInt();
		var length = context.getArguments()
				.get(PARAMETER_INT_LENGTH.getParameterName())
				.getAsInt();

		if (source.length == 0) {
			throw new KIRuntimeException("Compare source array cannot be empty");
		}

		if (find.length == 0) {
			throw new KIRuntimeException("Compare find array cannot be empty");
		}

		if (length == -1)
			length = source.length - srcfrom;

		if (srcfrom + length > source.length)
			throw new KIRuntimeException(StringFormatter.format("Source array size $ is less than comparing size $",
					source.length, srcfrom + length));

		if (findfrom + length > find.length)
			throw new KIRuntimeException(StringFormatter.format("Find array size $ is less than comparing size $",
					find.length, findfrom + length));

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME,
				new JsonPrimitive(compare(source, srcfrom, srcfrom + length, find, findfrom, findfrom + length)))))));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected int compare(JsonElement[] source, int srcfrom, int srcto, JsonElement[] find, int findfrom, int findto) {

		if (srcto < srcfrom) {
			int x = srcfrom;
			srcfrom = srcto;
			srcto = x;
		}

		if (findto < findfrom) {
			int x = findfrom;
			findfrom = findto;
			findto = x;
		}

		if ((srcto - srcfrom) != (findto - findfrom)) {
			throw new KIRuntimeException(StringFormatter.format(
					"Cannot compare uneven arrays from $ to $ in source array with $ to $ in find array", srcto,
					srcfrom, findto, findfrom));
		}

		for (int i = srcfrom, j = findfrom; i < srcto; i++, j++) {

			int x = 1;

			if (source[i] == null || find[j] == null || source[i] == JsonNull.INSTANCE
					|| find[j] == JsonNull.INSTANCE) {

				boolean s = source[i] == null || source[i] == JsonNull.INSTANCE;
				boolean f = find[j] == null || find[j] == JsonNull.INSTANCE;

				if (s == f)
					x = 0;
				else if (s)
					x = -1;
			} else if (source[i].isJsonPrimitive() && find[j].isJsonPrimitive()) {

				JsonPrimitive s = source[i].getAsJsonPrimitive();
				JsonPrimitive f = find[j].getAsJsonPrimitive();

				if (s.isJsonNull() && !f.isJsonNull()) {
					x = 1;
				} else if (!s.isJsonNull() && f.isJsonNull()) {
					x = -1;
				} else if (s.isString() || f.isString()) {
					x = s.getAsString()
							.compareTo(f.getAsString());
				} else if (s.isNumber() && f.isNumber()) {
					x = compareTo(s.getAsNumber(), f.getAsNumber());
				} else if (s.isBoolean() && f.isBoolean()) {
					x = Boolean.compare(s.getAsBoolean(), f.getAsBoolean());
				}
			} else if (source[i] instanceof Comparable<?> && find[j] instanceof Comparable<?>) {
				x = ((Comparable) source[i]).compareTo(find[i]);
			}

			if (x != 0)
				return x;
		}

		return 0;
	}

	protected int compareTo(Number a, Number b) {

		if (a instanceof Double || b instanceof Double)
			return Double.compare(a.doubleValue(), b.doubleValue());
		else if (a instanceof Float || b instanceof Float)
			return Float.compare(a.floatValue(), b.floatValue());
		else if (a instanceof Long || b instanceof Long)
			return Long.compare(a.longValue(), b.longValue());
		else if (a instanceof BigInteger || b instanceof BigInteger)
			return BigInteger.valueOf(a.longValue())
					.compareTo(BigInteger.valueOf(b.longValue()));
		else if (a instanceof BigDecimal || b instanceof BigDecimal)
			return BigDecimal.valueOf(a.doubleValue())
					.compareTo(BigDecimal.valueOf(b.doubleValue()));

		return Integer.compare(a.intValue(), b.intValue());
	}

}

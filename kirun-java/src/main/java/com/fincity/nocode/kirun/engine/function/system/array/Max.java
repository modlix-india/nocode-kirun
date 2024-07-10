package com.fincity.nocode.kirun.engine.function.system.array;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class Max extends AbstractArrayFunction {

	public Max() {
		this("Max", List.of(PARAMETER_ARRAY_SOURCE_PRIMITIVE), EVENT_RESULT_ANY);
	}

	protected Max(String functionName, List<Parameter> parameters, Event event) {
		super(functionName, parameters, event);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray source = context.getArguments()
				.get(PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName())
				.getAsJsonArray();

		if (source.size() == 0)
			throw new KIRuntimeException("Search source array cannot be empty");

		JsonPrimitive max = source.get(0)
				.getAsJsonPrimitive();

		for (int i = 1; i < source.size(); i++) {
			JsonPrimitive y = source.get(i)
					.getAsJsonPrimitive();
			if (functionSpecificComparator(max, y))
				continue;
			max = y;
		}

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, max)))));
	}

	protected boolean functionSpecificComparator(JsonPrimitive max, JsonPrimitive y) {
		return compareTo(max, y) >= 0;
	}

	protected int compareTo(JsonPrimitive oa, JsonPrimitive ob) {

		if (oa != null && oa.isJsonNull())
			oa = null;

		if (ob != null && ob.isJsonNull())
			ob = null;

		if (oa == null || ob == null) {
			if (oa == null && ob == null)
				return 0;
			if (oa == null)
				return -1;
			return 1;
		}

		if (oa.isString() || ob.isString())
			return oa.getAsString()
					.compareTo(ob.getAsString());

		Number a = oa.getAsNumber();
		Number b = ob.getAsNumber();

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

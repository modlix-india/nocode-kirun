package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class Disjoint extends AbstractArrayFunction {

	public Disjoint() {
		super("Disjoint", List.of(PARAMETER_ARRAY_SOURCE, PARAMETER_INT_SOURCE_FROM, PARAMETER_ARRAY_SECOND_SOURCE,
		        PARAMETER_INT_SECOND_SOURCE_FROM, PARAMETER_INT_LENGTH), EVENT_RESULT_ARRAY);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonArray firstSource = context.getArguments()
		        .get(PARAMETER_ARRAY_SOURCE.getParameterName())
		        .getAsJsonArray();

		JsonPrimitive firstFrom = context.getArguments()
		        .get(PARAMETER_INT_SOURCE_FROM.getParameterName())
		        .getAsJsonPrimitive();

		JsonArray secondSource = context.getArguments()
		        .get(PARAMETER_ARRAY_SECOND_SOURCE.getParameterName())
		        .getAsJsonArray();

		JsonPrimitive secondFrom = context.getArguments()
		        .get(PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName())
		        .getAsJsonPrimitive();

		JsonPrimitive length = context.getArguments()
		        .get(PARAMETER_INT_LENGTH.getParameterName())
		        .getAsJsonPrimitive();

		int first = firstFrom.getAsInt();

		int second = secondFrom.getAsInt();

		int len = length.getAsInt();

		if (len == -1)
			len = firstSource.size() <= secondSource.size() ? firstSource.size() - first : secondSource.size() - second;

		if (len > firstSource.size() || len > secondSource.size() || first + len > firstSource.size()
		        || second + len > secondSource.size())

			throw new KIRuntimeException(
			        "The length which was being requested is more than than the size either source array or second source array");

		Set<Object> set1 = new HashSet<>();
		Set<Object> set2 = new HashSet<>();

		for (int i = first; i < first + len; i++)
			set1.add(firstSource.get(i));

		for (int i = second; i < second + len; i++)
			set2.add(secondSource.get(i));

		Set<Object> set3 = new HashSet<>();

		Iterator<Object> itr1 = set1.iterator();

		while (itr1.hasNext()) {
			Object check = itr1.next();
			if (set2.contains(check))
				set2.remove(check);
			else
				set3.add(check);

		}

		Iterator<Object> itr2 = set2.iterator();

		while (itr2.hasNext()) {
			Object check = itr2.next();
			if (!set1.contains(check))
				set3.add(check);
		}

		JsonArray arr = new JsonArray();

		Iterator<Object> itr3 = set3.iterator();

		while (itr3.hasNext())
			arr.add((JsonElement) itr3.next());

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_ARRAY.getName(), arr)))));
	}
}

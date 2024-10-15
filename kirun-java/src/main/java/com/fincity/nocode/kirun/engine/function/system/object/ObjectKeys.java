package com.fincity.nocode.kirun.engine.function.system.object;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class ObjectKeys extends AbstractObjectFunction {

	private static final String SOURCE = "source";

	private static final String VALUE = "value";

	public ObjectKeys() {
		super("ObjectKeys", Schema.ofArray(VALUE, Schema.ofString(VALUE)));
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		var source = context.getArguments()
				.get(SOURCE);

		JsonArray arr = new JsonArray();

		if (source == null || source.isJsonNull() || (source.isJsonPrimitive() && !((JsonPrimitive) source).isString()))
			return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, new JsonArray())))));

		else if (source.isJsonPrimitive()) {

			JsonPrimitive prim = source.getAsJsonPrimitive();
			String str = prim.getAsString();
			for (int i = 0; i < str.length(); i++) {
				arr.add("" + i);
			}

		} else if (source.isJsonArray()) {

			JsonArray inputArray = source.getAsJsonArray();
			for (int i = 0; i < inputArray.size(); i++) {
				arr.add("" + i);
			}

		} else if (source.isJsonObject()) {

			source.getAsJsonObject()
					.keySet()
					.stream()
					.sorted(String::compareTo)
					.forEach(arr::add);
		}

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, arr)))));
	}
}

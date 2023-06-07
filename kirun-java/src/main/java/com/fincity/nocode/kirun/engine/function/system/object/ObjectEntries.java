package com.fincity.nocode.kirun.engine.function.system.object;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class ObjectEntries extends AbstractObjectFunction {

	private static final String SOURCE = "source";

	private static final String VALUE = "value";

	public ObjectEntries() {
		super("ObjectEntries",
		        Schema.ofArray(VALUE, Schema.ofArray("tuple", Schema.ofString("key"), Schema.ofAny(VALUE))));
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		var source = context.getArguments()
		        .get(SOURCE);

		JsonArray arr = new JsonArray();

		if (source == null || source.isJsonNull() || (source.isJsonPrimitive() && !((JsonPrimitive) source).isString()))
			return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, new JsonArray())))));

		else if (source.isJsonPrimitive()) {

			String[] outputString = source.getAsString()
			        .split("");
			for (int i = 0; i < outputString.length; i++) {
				JsonArray tempArr = new JsonArray();
				tempArr.add(String.valueOf(i));
				tempArr.add(outputString[i]);
				arr.add(tempArr);
			}

		} else if (source.isJsonArray()) {

			JsonArray inputArray = source.getAsJsonArray(); // taking input as array
			for (int i = 0; i < inputArray.size(); i++) {
				JsonArray tempArr = new JsonArray();
				tempArr.add(String.valueOf(i));
				tempArr.add(inputArray.get(i)
				        .deepCopy());
				arr.add(tempArr);
			}

		} else if (source.isJsonObject()) {

			JsonObject jsonObject = source.getAsJsonObject()
			        .deepCopy();

			jsonObject.entrySet()
			        .stream()
			        .sorted((a, b) -> a.getKey()
			                .compareTo(b.getKey()))
			        .map(e ->
					{
				        JsonArray tempArr = new JsonArray();
				        tempArr.add(e.getKey());
				        tempArr.add(e.getValue());

				        return tempArr;
			        })
			        .forEach(arr::add);
		}

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, arr)))));
	}

}

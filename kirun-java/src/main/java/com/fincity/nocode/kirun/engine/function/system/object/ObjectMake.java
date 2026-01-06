package com.fincity.nocode.kirun.engine.function.system.object;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.expression.ExpressionEvaluator;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import reactor.core.publisher.Mono;

public class ObjectMake extends AbstractReactiveFunction {

	private static final String SOURCE = "source";
	private static final String VALUE = "value";
	private static final String RESULT_STRUCT = "resultStruct";

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonElement source = context.getArguments().get(SOURCE);

		JsonObject resultStruct = context.getArguments().get(RESULT_STRUCT).getAsJsonObject();

		JsonObject result = new JsonObject();

		for (Map.Entry<String, JsonElement> entry : resultStruct.entrySet()) {
			String key = entry.getKey();
			JsonElement keyValue = entry.getValue();

			JsonElement valueToSet = keyValue.isJsonPrimitive() && keyValue.getAsJsonPrimitive().isString()
					? evaluateKeyValue(source, keyValue.getAsString(), context)
					: keyValue.deepCopy();

			result.add(key, valueToSet);
		}

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, result)))));
	}

	@Override
	public FunctionSignature getSignature() {
		return new FunctionSignature()
				.setName("ObjectMake")
				.setNamespace(Namespaces.SYSTEM_OBJECT)
				.setParameters(
						Map.ofEntries(
								Parameter.ofEntry(SOURCE, Schema.ofAny(SOURCE)),
								Parameter.ofEntry(RESULT_STRUCT, Schema.ofObject(RESULT_STRUCT))))
				.setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.ofAny(VALUE)))));
	}

	private JsonElement evaluateKeyValue(JsonElement source, String expression,
	                                     ReactiveFunctionExecutionParameters context) {

		if (expression == null || !expression.startsWith("{{") || !expression.endsWith("}}"))
			return source != null ? source.deepCopy() : JsonNull.INSTANCE;

		String innerExpression = expression.substring(2, expression.length() - 2);

		ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(innerExpression);
		JsonElement result = expressionEvaluator.evaluate(context.getValuesMap());

		return result != null ? result : JsonNull.INSTANCE;
	}
}

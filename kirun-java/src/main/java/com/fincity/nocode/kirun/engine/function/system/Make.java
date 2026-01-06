package com.fincity.nocode.kirun.engine.function.system;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import reactor.core.publisher.Mono;

public class Make extends AbstractReactiveFunction {

	private static final String RESULT_SHAPE = "resultShape";
	private static final String VALUE = "value";

	private static final FunctionSignature SIGNATURE = new FunctionSignature()
			.setName("Make")
			.setNamespace(Namespaces.SYSTEM)
			.setParameters(Map.ofEntries(Parameter.ofEntry(RESULT_SHAPE, Schema.ofAny(RESULT_SHAPE))))
			.setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.ofAny(VALUE)))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonElement resultShape = context.getArguments().get(RESULT_SHAPE);

		JsonElement result = processValue(resultShape, context);

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, result)))));
	}

	private JsonElement processValue(JsonElement value, ReactiveFunctionExecutionParameters context) {

		if (value == null || value.isJsonNull()) {
			return JsonNull.INSTANCE;
		}

		if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
			return evaluateExpression(value.getAsString(), context);
		}

		if (value.isJsonArray()) {
			JsonArray result = new JsonArray();
			for (JsonElement item : value.getAsJsonArray()) {
				result.add(processValue(item, context));
			}
			return result;
		}

		if (value.isJsonObject()) {
			JsonObject result = new JsonObject();
			for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
				result.add(entry.getKey(), processValue(entry.getValue(), context));
			}
			return result;
		}

		return value.deepCopy();
	}

	private JsonElement evaluateExpression(String expression, ReactiveFunctionExecutionParameters context) {

		if (expression == null || !expression.startsWith("{{") || !expression.endsWith("}}")) {
			return new com.google.gson.JsonPrimitive(expression);
		}

		String innerExpression = expression.substring(2, expression.length() - 2);

		ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(innerExpression);
		JsonElement result = expressionEvaluator.evaluate(context.getValuesMap());

		return result != null ? result : JsonNull.INSTANCE;
	}
}

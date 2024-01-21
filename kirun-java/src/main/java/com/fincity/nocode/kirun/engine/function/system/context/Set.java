package com.fincity.nocode.kirun.engine.function.system.context;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM_CTX;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.ParameterType;
import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.fincity.nocode.kirun.engine.runtime.expression.Expression;
import com.fincity.nocode.kirun.engine.runtime.expression.ExpressionEvaluator;
import com.fincity.nocode.kirun.engine.runtime.expression.ExpressionToken;
import com.fincity.nocode.kirun.engine.runtime.expression.ExpressionTokenValue;
import com.fincity.nocode.kirun.engine.runtime.expression.Operation;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import reactor.core.publisher.Mono;

public class Set extends AbstractReactiveFunction {

	static final String NAME = "name";

	static final String VALUE = "value";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Set")
			.setNamespace(SYSTEM_CTX)
			.setParameters(Map.ofEntries(Parameter.ofEntry(NAME, new Schema().setName(NAME)
					.setType(Type.of(SchemaType.STRING))
					.setMinLength(1)), Parameter.ofEntry(VALUE, Schema.ofAny(VALUE))))
			.setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of())));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		String key = context.getArguments()
				.get(NAME)
				.getAsString();

		if (key.isBlank()) {
			throw new KIRuntimeException("Empty string is not a valid name for the context element");
		}

		JsonElement value = context.getArguments()
				.get(VALUE);

		Expression exp = new Expression(key);

		ExpressionToken contextToken = exp.getTokens()
				.peekLast();

		if (!contextToken.getExpression()
				.startsWith("Context")
				|| contextToken instanceof Expression
				|| (contextToken instanceof ExpressionTokenValue etv && !etv.getElement()
						.toString()
						.startsWith("Context"))) {

			throw new ExecutionException(
					StringFormatter.format("The context path $ is not a valid path in context", key));
		}

		for (Operation op : exp.getOperations()) {

			if (op == Operation.ARRAY_OPERATOR || op == Operation.OBJECT_OPERATOR)
				continue;

			throw new ExecutionException(StringFormatter
					.format("Expected a reference to the context location, but found an expression $", key));
		}

		for (int i = 0; i < exp.getTokens()
				.size(); i++) {

			if (exp.getTokens()
					.get(i) instanceof Expression ex)
				exp.getTokens()
						.set(i, new ExpressionTokenValue(key,
								new ExpressionEvaluator(ex).evaluate(context.getValuesMap())));
		}

		// TODO: Here I need to validate the schema of the value I have to put in the
		// context.

		return modifyContext(context, key, value, exp);
	}

	private Mono<FunctionOutput> modifyContext(ReactiveFunctionExecutionParameters context, String key,
			JsonElement value, Expression exp) {
		LinkedList<ExpressionToken> tokens = exp.getTokens();
		tokens.removeLast();
		LinkedList<Operation> ops = exp.getOperations();
		ops.removeLast();
		ContextElement ce = context.getContext()
				.get(tokens.removeLast()
						.getExpression());

		if (ce == null) {
			throw new KIRuntimeException(
					StringFormatter.format("Context doesn't have any element with name '$' ", key));
		}

		if (ops.isEmpty()) {
			ce.setElement(value);
			return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of()))));
		}

		JsonElement el = ce.getElement();

		Operation op = ops.removeLast();
		ExpressionToken token = tokens.removeLast();
		String mem = token instanceof ExpressionTokenValue etv ? etv.getElement()
				.getAsString() : token.getExpression();

		if (el == null || el.isJsonNull()) {
			el = op == Operation.OBJECT_OPERATOR ? new JsonObject() : new JsonArray();
			ce.setElement(el);
		}

		while (!ops.isEmpty()) {

			if (op == Operation.OBJECT_OPERATOR) {
				el = this.getDataFromObject(el, mem, ops.peekLast());
			} else {
				el = this.getDataFromArray(el, mem, ops.peekLast());
			}

			op = ops.removeLast();
			token = tokens.removeLast();
			mem = token instanceof ExpressionTokenValue etv ? etv.getElement()
					.getAsString() : token.getExpression();
		}

		if (op == Operation.OBJECT_OPERATOR)
			this.putDataInObject(el, mem, value);
		else
			this.putDataInArray(el, mem, value);

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of()))));
	}

	private JsonElement getDataFromArray(JsonElement el, String mem, Operation nextOp) {

		if (!el.isJsonArray())
			throw new KIRuntimeException(StringFormatter.format("Expected an array but found $", el));

		try {
			int index = Integer.parseInt(mem);

			if (index < 0)
				throw new KIRuntimeException(StringFormatter.format("Array index is out of bound - $", mem));

			JsonArray ja = el.getAsJsonArray();
			while (index >= ja.size())
				ja.add(JsonNull.INSTANCE);

			JsonElement je = ja.get(index);
			if (je == null || je.isJsonNull()) {
				je = nextOp == Operation.OBJECT_OPERATOR ? new JsonObject() : new JsonArray();
				ja.set(index, je);
			}
			return je;
		} catch (Exception ex) {
			throw new KIRuntimeException(StringFormatter.format("Expected an array index but found $", mem));
		}
	}

	private JsonElement getDataFromObject(JsonElement el, String mem, Operation nextOp) {

		if (!el.isJsonObject())
			throw new KIRuntimeException(StringFormatter.format("Expected an object but found $", el));

		JsonObject jo = el.getAsJsonObject();
		JsonElement je = jo.get(mem);

		if (je == null || je.isJsonNull()) {
			je = nextOp == Operation.OBJECT_OPERATOR ? new JsonObject() : new JsonArray();
			jo.add(mem, je);
		}
		return je;
	}

	private void putDataInArray(JsonElement el, String mem, JsonElement value) {

		if (!el.isJsonArray())
			throw new KIRuntimeException(StringFormatter.format("Expected an array but found $", el));

		try {
			int index = Integer.parseInt(mem);

			if (index < 0)
				throw new KIRuntimeException(StringFormatter.format("Array index is out of bound - $", mem));

			JsonArray ja = el.getAsJsonArray();
			while (index >= ja.size())
				ja.add(JsonNull.INSTANCE);

			ja.set(index, value);
		} catch (Exception ex) {
			throw new KIRuntimeException(StringFormatter.format("Expected an array index but found $", mem));
		}
	}

	private void putDataInObject(JsonElement el, String mem, JsonElement value) {

		if (!el.isJsonObject())
			throw new KIRuntimeException(StringFormatter.format("Expected an object but found $", el));

		el.getAsJsonObject()
				.add(mem, value);
	}
}

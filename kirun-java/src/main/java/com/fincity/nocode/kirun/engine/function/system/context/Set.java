package com.fincity.nocode.kirun.engine.function.system.context;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM_CTX;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.AbstractFunction;
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
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.expression.Expression;
import com.fincity.nocode.kirun.engine.runtime.expression.ExpressionEvaluator;
import com.fincity.nocode.kirun.engine.runtime.expression.ExpressionToken;
import com.fincity.nocode.kirun.engine.runtime.expression.ExpressionTokenValue;
import com.fincity.nocode.kirun.engine.runtime.expression.Operation;
import com.fincity.nocode.kirun.engine.util.primitive.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuple2;

public class Set extends AbstractFunction {

	static final String NAME = "name";

	static final String VALUE = "value";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Set")
	        .setNamespace(SYSTEM_CTX)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(NAME, new Schema().setName(NAME)
	                .setType(Type.of(SchemaType.STRING))
	                .setMinLength(1), ParameterType.CONSTANT), Parameter.ofEntry(VALUE, Schema.ofAny(VALUE))))
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of())));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

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
			return new FunctionOutput(List.of(EventResult.outputOf(Map.of())));
		}

		JsonElement el = ce.getElement();
		JsonElement prev = el;
		Operation op = ops.removeLast();
		ExpressionToken token;
		JsonPrimitive mem = null;

		while (ops.size() > 1 && el != null) {

			prev = el;
			op = ops.removeLast();
			token = tokens.removeLast();

			if (op == Operation.OBJECT_OPERATOR) {

				if (!el.isJsonObject()) {
					throw new KIRuntimeException(StringFormatter.format("$ has no object in the context", key));
				}

				mem = token instanceof ExpressionTokenValue etv ? etv.getElement()
				        .getAsJsonPrimitive() : new JsonPrimitive(token.getExpression());

				el = el.getAsJsonObject()
				        .get(mem.getAsString());
			} else {
				mem = token instanceof ExpressionTokenValue etv ? etv.getElement()
				        .getAsJsonPrimitive() : new JsonPrimitive(token.getExpression());

				if (el.isJsonArray()) {

					Tuple2<SchemaType, Object> prim = PrimitiveUtil.findPrimitive(mem);

					if (prim.getT1() != SchemaType.INTEGER || prim.getT1() != SchemaType.LONG) {
						throw new KIRuntimeException(
						        StringFormatter.format("Expecting a numerical index but found $", mem));
					}

					int index = ((Number) prim.getT2()).intValue();
					JsonArray ja = el.getAsJsonArray();
					if (index < 0 || index >= ja.size())
						throw new KIRuntimeException(
						        StringFormatter.format("Index out of bound while accessing $", key));
					el = ja.get(index);
					mem = new JsonPrimitive(index);
				} else {

					el = el.getAsJsonObject()
					        .get(mem.getAsString());
				}
			}
		}

		el = prev;
		do {

			if (mem != null) {
				JsonElement je = null;
				if (op == Operation.OBJECT_OPERATOR) {
					je = new JsonObject();
				} else {
					je = new JsonArray();
				}

				if (el.isJsonObject()) {
					el.getAsJsonObject()
					        .add(mem.getAsString(), je);
				} else if (el.isJsonArray()) {
					JsonArray arr = el.getAsJsonArray();
					int ind = mem.getAsInt();
					while (ind >= arr.size())
						arr.add(JsonNull.INSTANCE);
					arr.set(ind, je);
				}
				el = je;
			}

			if (!ops.isEmpty())
				op = ops.removeLast();
			if (!tokens.isEmpty()) {
				token = tokens.removeLast();
				mem = token instanceof ExpressionTokenValue etv ? etv.getElement()
				        .getAsJsonPrimitive() : new JsonPrimitive(token.getExpression());
			}

		} while (!ops.isEmpty() && !tokens.isEmpty());

		// TODO: Here I need to validate the schema of the value I have to put in the
		// context.

		if (mem != null) {

			if (el.isJsonObject()) {
				el.getAsJsonObject()
				        .add(mem.getAsString(), value);
			} else if (el.isJsonArray()) {
				JsonArray arr = el.getAsJsonArray();
				int ind = mem.getAsInt();
				while (ind >= arr.size())
					arr.add(JsonNull.INSTANCE);
				arr.set(ind, value);
			}
		}

		return new FunctionOutput(List.of(EventResult.outputOf(Map.of())));
	}

}

package com.fincity.nocode.kirun.engine.runtime.expression;

import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.ADDITION;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.AND;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.BITWISE_AND;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.BITWISE_LEFT_SHIFT;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.BITWISE_OR;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.BITWISE_RIGHT_SHIFT;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.BITWISE_UNSIGNED_RIGHT_SHIFT;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.BITWISE_XOR;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.DIVISION;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.EQUAL;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.GREATER_THAN;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.GREATER_THAN_EQUAL;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.LESS_THAN;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.LESS_THAN_EQUAL;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.MOD;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.MULTIPLICATION;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.NOT_EQUAL;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.OR;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.SUBTRACTION;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.UNARY_BITWISE_COMPLEMENT;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.UNARY_LOGICAL_NOT;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.UNARY_MINUS;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.UNARY_PLUS;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.expression.exception.ExpressionEvaluationException;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArithmeticAdditionOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArithmeticDivisionOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArithmeticModulusOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArithmeticMultiplicationOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArithmeticSubtractionOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.BinaryOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.BitwiseAndOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.BitwiseLeftShiftOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.BitwiseOrOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.BitwiseRightShiftOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.BitwiseUnsignedRightShiftOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.BitwiseXorOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalAndOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalEqualOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalGreaterThanEqualOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalGreaterThanOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalLessThanEqualOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalLessThanOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalNotEqualOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.LogicalOrOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.unary.ArithmeticUnaryMinusOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.unary.ArithmeticUnaryPlusOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.unary.BitwiseComplementOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.unary.LogicalNotOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.unary.UnaryOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.ArgumentsTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.ContextTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.LiteralTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.OutputMapTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public class ExpressionEvaluator {

	private static final Map<Operation, UnaryOperator> UNARY_OPERATORS_MAP = new EnumMap<>(Map.of(
	        UNARY_BITWISE_COMPLEMENT, new BitwiseComplementOperator(), UNARY_LOGICAL_NOT, new LogicalNotOperator(),
	        UNARY_MINUS, new ArithmeticUnaryMinusOperator(), UNARY_PLUS, new ArithmeticUnaryPlusOperator()));

	private static final Map<Operation, BinaryOperator> BINARY_OPERATORS_MAP = new EnumMap<>(Map.ofEntries(
	        Map.entry(ADDITION, new ArithmeticAdditionOperator()),
	        Map.entry(DIVISION, new ArithmeticDivisionOperator()), Map.entry(MOD, new ArithmeticModulusOperator()),
	        Map.entry(MULTIPLICATION, new ArithmeticMultiplicationOperator()),
	        Map.entry(SUBTRACTION, new ArithmeticSubtractionOperator()),

	        Map.entry(BITWISE_AND, new BitwiseAndOperator()),
	        Map.entry(BITWISE_LEFT_SHIFT, new BitwiseLeftShiftOperator()),
	        Map.entry(BITWISE_OR, new BitwiseOrOperator()),
	        Map.entry(BITWISE_RIGHT_SHIFT, new BitwiseRightShiftOperator()),
	        Map.entry(BITWISE_UNSIGNED_RIGHT_SHIFT, new BitwiseUnsignedRightShiftOperator()),
	        Map.entry(BITWISE_XOR, new BitwiseXorOperator()),

	        Map.entry(AND, new LogicalAndOperator()), Map.entry(EQUAL, new LogicalEqualOperator()),
	        Map.entry(GREATER_THAN, new LogicalGreaterThanOperator()),
	        Map.entry(GREATER_THAN_EQUAL, new LogicalGreaterThanEqualOperator()),
	        Map.entry(LESS_THAN, new LogicalLessThanOperator()),
	        Map.entry(LESS_THAN_EQUAL, new LogicalLessThanEqualOperator()), Map.entry(OR, new LogicalOrOperator()),
	        Map.entry(NOT_EQUAL, new LogicalNotEqualOperator())));

	private static final Set<Operation> UNARY_OPERATORS_MAP_KEY_SET = UNARY_OPERATORS_MAP.keySet();

	private String expression;

	public ExpressionEvaluator(String expression) {
		this.expression = expression;
	}

	public JsonElement evaluate(FunctionExecutionParameters context,
	        Map<String, Map<String, Map<String, JsonElement>>> output) {

		Expression exp = new Expression(this.expression);

		Map<String, TokenValueExtractor> valuesMap = Map.of("Steps", new OutputMapTokenValueExtractor(output), "Argum",
		        new ArgumentsTokenValueExtractor(context.getArguments()), "Conte",
		        new ContextTokenValueExtractor(context.getContext()));

		return this.evaluateExpression(exp, valuesMap);
	}

	private JsonElement evaluateExpression(Expression exp, Map<String, TokenValueExtractor> valuesMap) {

		LinkedList<Operation> ops = exp.getOperations();
		LinkedList<ExpressionToken> tokens = exp.getTokens();

		while (!ops.isEmpty()) {

			Operation operator = ops.pop();
			ExpressionToken token = tokens.pop();

			if (UNARY_OPERATORS_MAP_KEY_SET.contains(operator)) {

				tokens.push(applyOperation(operator, getValueFromToken(valuesMap, token)));
			} else {
				ExpressionToken token2 = tokens.pop();
				var v1 = getValueFromToken(valuesMap, token2);
				var v2 = getValueFromToken(valuesMap, token);
				tokens.push(applyOperation(operator, v1, v2));
			}
		}

		if (tokens.isEmpty())
			throw new ExecutionException(StringFormatter.format("Expression : $ evaluated to null", exp));

		if (tokens.size() != 1)
			throw new ExecutionException(
			        StringFormatter.format("Expression : $ evaluated multiple values $", exp, tokens));

		ExpressionToken token = tokens.get(0);
		if (token instanceof ExpressionTokenValue etv)
			return etv.getElement();
		else if (!(token instanceof Expression))
			return getValueFromToken(valuesMap, token);

		throw new ExecutionException(StringFormatter.format("Expression : $ evaluated to $", exp, tokens.get(0)));
	}

	private ExpressionToken applyOperation(Operation operator, JsonElement v1, JsonElement v2) {

		if ((v1 != null && (v1 != JsonNull.INSTANCE && !v1.isJsonPrimitive()))
		        && (v2 != null && (v2 != JsonNull.INSTANCE && !v2.isJsonPrimitive())))
			throw new ExpressionEvaluationException(this.expression,
			        StringFormatter.format("Cannot evaluate expression $ $ $", v1, operator.getOperator(), v2));

		BinaryOperator op = BINARY_OPERATORS_MAP.get(operator);

		if (op == null)
			throw new ExpressionEvaluationException(this.expression,
			        StringFormatter.format("No operator found to evaluate $ $ $", v1, operator.getOperator(), v2));

		return new ExpressionTokenValue(operator.toString(), op.apply(v1, v2));
	}

	private ExpressionToken applyOperation(Operation operator, JsonElement value) {

		if (value != null && (value != JsonNull.INSTANCE && !value.isJsonPrimitive()))
			throw new ExpressionEvaluationException(this.expression,
			        StringFormatter.format("The operator $ cannot be applied to $", operator.getOperator(), value));

		UnaryOperator op = UNARY_OPERATORS_MAP.get(operator);

		if (op == null)
			throw new ExpressionEvaluationException(this.expression, StringFormatter
			        .format("No Unary operator $ is found to apply on $", operator.getOperator(), value));

		return new ExpressionTokenValue(operator.toString(), op.apply(value));
	}

	private JsonElement getValueFromToken(Map<String, TokenValueExtractor> valuesMap, ExpressionToken token) {

		if (token instanceof Expression ex) {
			return this.evaluateExpression(ex, valuesMap);
		} else if (token instanceof ExpressionTokenValue v) {
			return v.getElement();
		}
		return getValue(token.getExpression(), valuesMap);
	}

	private JsonElement getValue(String path, Map<String, TokenValueExtractor> valuesMap) {

		path = solveNestedSquareBrackets(path, valuesMap);

		if (path.length() <= 5)
			return LiteralTokenValueExtractor.INSTANCE.getValue(path);

		return valuesMap.getOrDefault(path.substring(0, 5), LiteralTokenValueExtractor.INSTANCE)
		        .getValue(path);
	}

	private String solveNestedSquareBrackets(String path, Map<String, TokenValueExtractor> valuesMap) { // NOSONAR
		// Breaking this logic wont make sense.

		int ind = path.indexOf('[');
		if (ind == -1)
			return path;

		int st = ind + 1;
		List<Tuple3<Integer, Integer, String>> evaluatedStrings = new ArrayList<>(4);
		while (st < path.length()) {

			int count = 1;
			while (st < path.length() && count > 0) {
				if (path.charAt(st) == '[')
					count++;
				else if (path.charAt(st) == ']')
					count--;
				st++;
			}

			if (st == path.length() || path.charAt(st) != ']') {
				throw new ExpressionEvaluationException(this.expression, "Missing ']' or not closed properly");
			}

			JsonElement element = this.evaluateExpression(new Expression(path.substring(ind + 1, st)), valuesMap);
			if (element.isJsonPrimitive() && ((JsonPrimitive) element).isNumber()) {
				evaluatedStrings.add(Tuples.of(ind + 1, st, element.toString()));
			} else {
				evaluatedStrings.add(Tuples.of(ind + 1, st, "\"" + element.toString() + "\""));
			}

			while (st < path.length() && path.charAt(st) != '[')
				st++;

			ind = st;
			st++;
		}

		return removeExpressionsInSquareBrackets(path, evaluatedStrings);
	}

	private String removeExpressionsInSquareBrackets(String path,
	        List<Tuple3<Integer, Integer, String>> evaluatedStrings) {

		StringBuilder sb = new StringBuilder(path);
		for (int i = evaluatedStrings.size() - 1; i >= 0; i--) {
			var tuple = evaluatedStrings.get(i);
			sb.replace(tuple.getT1(), tuple.getT2(), tuple.getT3());
		}

		return sb.toString();
	}
}
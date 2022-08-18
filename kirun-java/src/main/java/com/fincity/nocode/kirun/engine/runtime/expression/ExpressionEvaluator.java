package com.fincity.nocode.kirun.engine.runtime.expression;

import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.ADDITION;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.AND;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.ARRAY_OPERATOR;
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
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.OBJECT_OPERATOR;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.OR;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.SUBTRACTION;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.UNARY_BITWISE_COMPLEMENT;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.UNARY_LOGICAL_NOT;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.UNARY_MINUS;
import static com.fincity.nocode.kirun.engine.runtime.expression.Operation.UNARY_PLUS;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.runtime.expression.exception.ExpressionEvaluationException;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArithmeticAdditionOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArithmeticDivisionOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArithmeticModulusOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArithmeticMultiplicationOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArithmeticSubtractionOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ArrayOperator;
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
import com.fincity.nocode.kirun.engine.runtime.expression.operators.binary.ObjectOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.unary.ArithmeticUnaryMinusOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.unary.ArithmeticUnaryPlusOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.unary.BitwiseComplementOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.unary.LogicalNotOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.operators.unary.UnaryOperator;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.LiteralTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

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
	        Map.entry(NOT_EQUAL, new LogicalNotEqualOperator()),

	        Map.entry(ARRAY_OPERATOR, new ArrayOperator()), Map.entry(OBJECT_OPERATOR, new ObjectOperator())));

	private static final Set<Operation> UNARY_OPERATORS_MAP_KEY_SET = UNARY_OPERATORS_MAP.keySet();

	private String expression;
	private Expression exp;

	public ExpressionEvaluator(String expression) {
		this.expression = expression;
	}

	public ExpressionEvaluator(Expression exp) {
		this.exp = exp;
		this.expression = exp.getExpression();
	}

	public JsonElement evaluate(Map<String, TokenValueExtractor> valuesMap) {

		if (exp == null)
			exp = new Expression(this.expression);

		return this.evaluateExpression(exp, valuesMap);
	}

	public Expression getExpression() {

		if (this.exp == null)
			this.exp = new Expression(this.expression);

		return this.exp;
	}

	public String getExpressionString() {
		return this.expression;
	}

	private JsonElement evaluateExpression(Expression exp, Map<String, TokenValueExtractor> valuesMap) {

		LinkedList<Operation> ops = exp.getOperations();
		LinkedList<ExpressionToken> tokens = exp.getTokens();

		while (!ops.isEmpty()) {

			Operation operator = ops.pop();
			ExpressionToken token = tokens.pop();

			if (UNARY_OPERATORS_MAP_KEY_SET.contains(operator)) {
				tokens.push(applyOperation(operator, getValueFromToken(valuesMap, token)));
			} else if (operator == OBJECT_OPERATOR || operator == ARRAY_OPERATOR) {
				processObjectOrArrayOperator(valuesMap, ops, tokens, operator, token);
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

	private void processObjectOrArrayOperator(Map<String, TokenValueExtractor> valuesMap, LinkedList<Operation> ops,
	        LinkedList<ExpressionToken> tokens, Operation operator, ExpressionToken token) {
		LinkedList<ExpressionToken> objTokens = new LinkedList<>();
		LinkedList<Operation> objOperations = new LinkedList<>();

		do {
			objOperations.push(operator);
			if (token instanceof Expression ex)
				objTokens.push(new ExpressionTokenValue(token.toString(), this.evaluateExpression(ex, valuesMap)));
			else
				objTokens.push(token);
			token = tokens.isEmpty() ? null : tokens.pop();
			operator = ops.isEmpty() ? null : ops.pop();
		} while (operator == OBJECT_OPERATOR || operator == ARRAY_OPERATOR);

		if (token != null) {
			if (token instanceof Expression ex)
				objTokens.push(new ExpressionTokenValue(token.toString(), this.evaluateExpression(ex, valuesMap)));
			else
				objTokens.push(token);
		}

		if (operator != null)
			ops.push(operator);

		ExpressionToken objToken = objTokens.pop();
		StringBuilder sb = new StringBuilder((objToken instanceof ExpressionTokenValue etv ? etv.getTokenValue()
		        .getAsString() : objToken.toString()));

		while (!objTokens.isEmpty()) {
			objToken = objTokens.pop();
			operator = objOperations.pop();
			sb.append(operator.getOperator())
			        .append((objToken instanceof ExpressionTokenValue etv ? etv.getTokenValue()
			                .getAsString() : objToken.toString()));
			if (operator == ARRAY_OPERATOR)
				sb.append(']');
		}

		String str = sb.toString();
		String key = str.substring(0, str.indexOf('.')+1);
		if (key.length() > 2 && valuesMap.containsKey(key))
			tokens.push(new ExpressionTokenValue(str, getValue(str, valuesMap)));
		else {
			JsonElement v = null;
			try {
				v = LiteralTokenValueExtractor.INSTANCE.getValue(str);
			} catch (Exception ex) {
				v = new JsonPrimitive(str);
			}
			tokens.push(new ExpressionTokenValue(str, v));
		}
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

		if (path.length() <= 5)
			return LiteralTokenValueExtractor.INSTANCE.getValue(path);

		String pathPrefix = path.substring(0, path.indexOf('.') + 1);
		return valuesMap.getOrDefault(pathPrefix, LiteralTokenValueExtractor.INSTANCE)
		        .getValue(path);
	}
}
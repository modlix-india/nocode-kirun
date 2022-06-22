package com.fincity.nocode.kirun.engine.runtime.expression;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.expression.exception.ExpressionEvaluationException;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.ArgumentsTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.ContextTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.LiteralTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.OutputMapTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public class ExpressionEvaluator {

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

			if (Operation.UNARY_OPERATORS.contains(operator)) {
				tokens.push(applyOperation(operator, getValueFromToken(valuesMap, token)));
			} else {
				ExpressionToken token2 = tokens.pop();
				
				tokens.push(applyOperation(operator, getValueFromToken(valuesMap, token), getValueFromToken(valuesMap, token2)));
			}
		}

		return null;
	}

	private ExpressionToken applyOperation(Operation operator, JsonElement valueFromToken,
	        JsonElement valueFromToken2) {
		// TODO Auto-generated method stub
		return null;
	}

	private ExpressionToken applyOperation(Operation operator, JsonElement valueFromToken) {
		// TODO Auto-generated method stub
		return null;
	}

	private JsonElement getValueFromToken(Map<String, TokenValueExtractor> valuesMap,
	        ExpressionToken token) {

		
		if (token instanceof Expression ex) {
			return this.evaluateExpression(ex, valuesMap);
		} else if (token instanceof ExpressionTokenValue v) {
			return v.getElement();
		}
		return getValue(token.getExpression(), valuesMap);
	}

	private JsonElement getValue(String path, Map<String, TokenValueExtractor> valuesMap) {

		path = solveNestedSquareBrackets(path, valuesMap);
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
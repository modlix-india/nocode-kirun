package com.fincity.nocode.kirun.engine.runtime.expression;

import java.util.LinkedList;
import java.util.Map;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.ArgumentsTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.ContextTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.OutputMapTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.google.gson.JsonElement;

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

		LinkedList<String> ops = exp.getOperations();
		LinkedList<ExpressionToken> tokens = exp.getTokens();

		while (!ops.isEmpty()) {

			String operator = ops.pop();

			if (operator.startsWith("UN: ")) {

				JsonElement element = null;

				getValueFromToken(valuesMap, tokens);
			}
		}
	}

	private JsonElement getValueFromToken(Map<String, TokenValueExtractor> valuesMap,
	        LinkedList<ExpressionToken> tokens) {

		ExpressionToken token = tokens.pop();
		if (token instanceof Expression ex) {
			return this.evaluateExpression(ex, valuesMap);
		} else if (token instanceof ExpressionTokenValue v) {
			return v.getElement();
		}
		return getValue(token.getExpression(), valuesMap);
	}

	private JsonElement getValue(String path, Map<String, TokenValueExtractor> valuesMap) {
		return valuesMap.get(path.substring(0, 5))
		        .getValue(path);
	}
}
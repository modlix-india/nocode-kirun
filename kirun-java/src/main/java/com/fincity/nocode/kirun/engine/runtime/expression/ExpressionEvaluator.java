package com.fincity.nocode.kirun.engine.runtime.expression;

import java.util.LinkedList;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.ArgumentsTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.ContextTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.OutputMapTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;

public class ExpressionEvaluator {

	private String expression;

	public ExpressionEvaluator(String expression) {
		this.expression = expression;
	}

	public JsonElement evaluate(FunctionExecutionParameters context,
	        Map<String, Map<String, Map<String, JsonElement>>> output) {

		Expression exp = new Expression(this.expression);

		LinkedList<JsonElement> values = new LinkedList<>();

		LinkedList<Expression> exprQue = new LinkedList<>();
		exprQue.add(exp);

		Map<String, TokenValueExtractor> expressions = Map.of("Steps", new OutputMapTokenValueExtractor(output),
		        "Argum", new ArgumentsTokenValueExtractor(context.getArguments()), "Conte",
		        new ContextTokenValueExtractor(context.getContext()));

		while (!exprQue.isEmpty()) {

			
		}

		if (values.size() != 1) {

			throw new KIRuntimeException(StringFormatter.format(
			        "Unable to evaluate the expression : $, The expression is evaluted to $", this.expression, exp));
		}

		return values.get(0);
	}
}
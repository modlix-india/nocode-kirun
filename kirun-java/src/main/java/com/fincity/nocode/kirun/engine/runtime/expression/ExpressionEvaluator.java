package com.fincity.nocode.kirun.engine.runtime.expression;

import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonElement;

public class ExpressionEvaluator {

	private String expression;

	public ExpressionEvaluator(String expression) {
		this.expression = expression;
	}

	public JsonElement evaluate(FunctionExecutionParameters context,
	        Map<String, Map<String, Map<String, JsonElement>>> steps) {

		return null;
	}

}

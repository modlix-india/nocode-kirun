package com.fincity.nocode.kirun.engine.runtime.expression.exception;

import com.fincity.nocode.kirun.engine.util.string.StringFormatter;

public class ExpressionEvaluationException extends RuntimeException {

	private static final long serialVersionUID = -2692016888435009526L;

	public ExpressionEvaluationException(String expression, String message) {
		super(StringFormatter.format("$ : $", expression, message));
	}
}

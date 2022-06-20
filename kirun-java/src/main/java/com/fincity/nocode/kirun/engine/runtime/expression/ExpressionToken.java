package com.fincity.nocode.kirun.engine.runtime.expression;

import java.util.Objects;

import lombok.Data;

@Data
public class ExpressionToken {

	protected final String expression;

	public ExpressionToken(String expression) {
		this.expression = expression;
	}

	@Override
	public String toString() {
		return Objects.toString(expression);
	}
}

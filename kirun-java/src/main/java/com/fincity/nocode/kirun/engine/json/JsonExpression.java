package com.fincity.nocode.kirun.engine.json;

import com.google.gson.JsonElement;

public class JsonExpression extends JsonElement {

	private final String expression;

	public JsonExpression(String expression) {
		this.expression = expression;
	}

	@Override
	public JsonElement deepCopy() {
		return new JsonExpression(this.expression);
	}

	public String getExpression() {
		return this.expression;
	}

	@Override
	public String getAsString() {
		return "Expression : " + expression;
	}
	
	@Override
	public String toString() {
		return this.getAsString();
	}
}

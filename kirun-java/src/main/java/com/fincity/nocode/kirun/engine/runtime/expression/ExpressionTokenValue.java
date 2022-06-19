package com.fincity.nocode.kirun.engine.runtime.expression;

import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExpressionTokenValue extends ExpressionToken{
	
	private JsonElement element;

	public ExpressionTokenValue(String expression, JsonElement element) {
		
		super(expression);
		this.element = element;
	}
	
	@Override
	public String toString() {
	
		return StringFormatter.format("$: $", this.expression, this.element);
	}
}

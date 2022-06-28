package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import java.util.Map;

import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.google.gson.JsonElement;

public class ContextTokenValueExtractor extends TokenValueExtractor {
	
	public static final String PREFIX = "Arguments.";

	private Map<String, ContextElement> context;

	public ContextTokenValueExtractor(Map<String, ContextElement> context) {

		this.context = context;
	}

	@Override
	protected JsonElement getValueInternal(String token) {
		String[] parts = token.split("\\.");

		return retrieveElementFrom(token, parts, 2, context.getOrDefault(parts[1], ContextElement.NULL)
		        .getElement());
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}
}

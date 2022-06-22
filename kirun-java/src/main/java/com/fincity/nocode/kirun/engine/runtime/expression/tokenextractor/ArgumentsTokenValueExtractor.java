package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import java.util.Map;

import com.google.gson.JsonElement;

public class ArgumentsTokenValueExtractor extends TokenValueExtractor {

	public static final String PREFIX = "Arguments.";

	private final Map<String, JsonElement> arguments;

	public ArgumentsTokenValueExtractor(Map<String, JsonElement> arguments) {

		this.arguments = arguments;
	}

	@Override
	protected JsonElement getValueInternal(String token) {

		String[] parts = token.split(".");

		return retrieveElementFrom(token, parts, 2, arguments.get(parts[1]));
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}
}

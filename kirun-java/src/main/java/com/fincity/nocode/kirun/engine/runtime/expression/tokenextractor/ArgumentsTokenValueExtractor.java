package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import java.util.Map;

import com.google.gson.JsonElement;

public class ArgumentsTokenValueExtractor extends TokenValueExtractor {

	private Map<String, JsonElement> arguments;

	public ArgumentsTokenValueExtractor(Map<String, JsonElement> arguments) {

		this.arguments = arguments;
	}

	@Override
	public JsonElement getValue(String token) {

		return null;
	}
}

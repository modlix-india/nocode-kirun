package com.fincity.nocode.kirun.engine.runtime.expression;

import java.util.HashMap;
import java.util.Map;

import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public class ExpressionInternalValueExtractor extends TokenValueExtractor {

	public static final String PREFIX = "_internal.";

	private Map<String, JsonElement> values = new HashMap<>();

	public void addValue(String key, JsonElement value) {
		this.values.put(key, value);
	}

	@Override
	protected JsonElement getValueInternal(String token) {
		String[] parts = token.split("\\.");

		String key = parts[1];
		int bIndex = key.indexOf('[');
		int fromIndex = 2;
		if (bIndex != -1) {
			key = parts[1].substring(0, bIndex);
			parts[1] = parts[1].substring(bIndex);
			fromIndex = 1;
		}

		return this.retrieveElementFrom(token, parts, fromIndex, this.values.get(key));
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public JsonElement getStore() {
		return JsonNull.INSTANCE;
	}
}
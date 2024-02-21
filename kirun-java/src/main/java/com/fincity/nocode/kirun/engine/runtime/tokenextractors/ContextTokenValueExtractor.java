package com.fincity.nocode.kirun.engine.runtime.tokenextractors;

import java.util.Map;

import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class ContextTokenValueExtractor extends TokenValueExtractor {

	public static final String PREFIX = "Context.";

	private Map<String, ContextElement> context;

	public ContextTokenValueExtractor(Map<String, ContextElement> context) {

		this.context = context;
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

		return retrieveElementFrom(token, parts, fromIndex, context.getOrDefault(key, ContextElement.NULL)
				.getElement());
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public JsonElement getStore() {

		if (this.context == null)
			return JsonNull.INSTANCE;

		JsonObject job = new JsonObject();

		for (Map.Entry<String, ContextElement> entry : this.context.entrySet()) {
			job.add(entry.getKey(), entry.getValue().getElement());
		}

		return job;
	}
}

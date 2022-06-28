package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public class OutputMapTokenValueExtractor extends TokenValueExtractor {
	
	public static final String PREFIX = "Steps.";

	private Map<String, Map<String, Map<String, JsonElement>>> output;

	public OutputMapTokenValueExtractor(Map<String, Map<String, Map<String, JsonElement>>> output) {

		this.output = output;
	}
	
	@Override
	protected JsonElement getValueInternal(String token) {
		String[] parts = token.split("\\.");
		
		int ind = 1;
		
		Map<String, Map<String, JsonElement>> events = output.get(parts[ind++]);
		if (events == null || ind >= parts.length) return JsonNull.INSTANCE;
		
		Map<String, JsonElement> eachEvent = events.get(parts[ind++]);
		if (eachEvent == null || ind >= parts.length) return JsonNull.INSTANCE;
		
		JsonElement element = eachEvent.get(parts[ind++]);

		return retrieveElementFrom(token, parts, ind, element);
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}
}

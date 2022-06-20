package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import java.util.Map;

import com.google.gson.JsonElement;

public class OutputMapTokenValueExtractor extends TokenValueExtractor {

	private Map<String, Map<String, Map<String, JsonElement>>> output;

	public OutputMapTokenValueExtractor(Map<String, Map<String, Map<String, JsonElement>>> output) {

		this.output = output;
	}
	
	@Override
	public JsonElement getValue(String token) {
	
		return null;
	}
}

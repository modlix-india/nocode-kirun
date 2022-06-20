package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import java.util.Map;

import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.google.gson.JsonElement;

public class ContextTokenValueExtractor extends TokenValueExtractor {

	private Map<String, ContextElement> context;

	public ContextTokenValueExtractor(Map<String, ContextElement> context) {

		this.context= context;
	}
	
	@Override
	public JsonElement getValue(String token) {
	
		return null;
	}
}

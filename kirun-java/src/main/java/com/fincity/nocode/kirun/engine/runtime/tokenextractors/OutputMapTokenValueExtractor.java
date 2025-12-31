package com.fincity.nocode.kirun.engine.runtime.tokenextractors;

import java.util.Map;

import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class OutputMapTokenValueExtractor extends TokenValueExtractor {

	public static final String PREFIX = "Steps.";

	private Map<String, Map<String, Map<String, JsonElement>>> output;

	public OutputMapTokenValueExtractor(Map<String, Map<String, Map<String, JsonElement>>> output) {

		this.output = output;
	}

	@Override
	protected JsonElement getValueInternal(String token) {
		String[] parts = splitPath(token);

		int ind = 1;

		Map<String, Map<String, JsonElement>> events = output.get(parts[ind++]);
		if (events == null || ind >= parts.length)
			return JsonNull.INSTANCE;

		Map<String, JsonElement> eachEvent = events.get(parts[ind++]);
		if (eachEvent == null || ind >= parts.length)
			return JsonNull.INSTANCE;

		int bracket = parts[ind].indexOf('[');

		if (bracket == -1) {
			JsonElement element = eachEvent.get(parts[ind++]);
			return this.retrieveElementFrom(token, parts, ind, element);
		}

		String evParamName = parts[ind].substring(0, bracket);
		JsonElement element = eachEvent.get(evParamName);

		JsonObject object = new JsonObject();
		object.add(evParamName, element);
		return retrieveElementFrom(token, parts, ind, object);

	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public JsonElement getStore() {
		if (this.output == null)
			return JsonNull.INSTANCE;

		JsonObject job = new JsonObject();

		for (Map.Entry<String, Map<String, Map<String, JsonElement>>> entry : this.output.entrySet()) {
			JsonObject innerJob = new JsonObject();
			for (Map.Entry<String, Map<String, JsonElement>> innerEntry : entry.getValue().entrySet()) {
				JsonObject innerInnerJob = new JsonObject();
				for (Map.Entry<String, JsonElement> innerInnerEntry : innerEntry.getValue().entrySet()) {
					innerInnerJob.add(innerInnerEntry.getKey(), innerInnerEntry.getValue());
				}
				innerJob.add(innerEntry.getKey(), innerInnerJob);
			}
			job.add(entry.getKey(), innerJob);
		}

		return job;
	}
}

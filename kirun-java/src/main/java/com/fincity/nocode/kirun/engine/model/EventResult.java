package com.fincity.nocode.kirun.engine.model;

import java.util.Map;

import com.google.gson.JsonElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode
public class EventResult {

	private String name;
	private Map<String, JsonElement> result;

	public static final EventResult outputOf(Map<String, JsonElement> result) {
		return of(Event.OUTPUT, result);
	}

	public static final EventResult of(String eventName, Map<String, JsonElement> result) {
		return new EventResult().setName(eventName)
		        .setResult(result);
	}
}

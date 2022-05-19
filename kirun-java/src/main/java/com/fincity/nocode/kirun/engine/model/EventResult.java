package com.fincity.nocode.kirun.engine.model;

import java.util.Map;

import com.google.gson.JsonElement;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class EventResult {

	private String name;
	private Map<String, JsonElement> result;

	public static final EventResult outputResult(Map<String, JsonElement> result) {
		return new EventResult().setName(Event.OUTPUT)
		        .setResult(result);
	}
}

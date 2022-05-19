package com.fincity.nocode.kirun.engine.model;

import com.google.gson.JsonElement;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class EventResult {

	private String name;
	private JsonElement result;

	public static final EventResult outputResult(JsonElement result) {
		return new EventResult().setName(Event.OUTPUT)
		        .setResult(result);
	}
}

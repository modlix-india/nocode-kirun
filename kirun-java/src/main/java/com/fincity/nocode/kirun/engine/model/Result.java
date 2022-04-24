package com.fincity.nocode.kirun.engine.model;

import com.google.gson.JsonElement;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Result {

	private JsonElement[] value;

	public Result setValue(JsonElement... value) {

		this.value = value;
		return this;
	}
}

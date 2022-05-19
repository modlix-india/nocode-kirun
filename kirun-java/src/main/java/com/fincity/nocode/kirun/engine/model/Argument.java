package com.fincity.nocode.kirun.engine.model;

import com.google.gson.JsonElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Argument {

	private String name;
	private JsonElement value;

	public static final Argument of(String name, JsonElement value) {
		return new Argument(name, value);
	}
}

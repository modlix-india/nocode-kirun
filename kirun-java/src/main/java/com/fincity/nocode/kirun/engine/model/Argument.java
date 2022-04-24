package com.fincity.nocode.kirun.engine.model;

import com.google.gson.JsonElement;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Argument implements Comparable<Argument> {

	private int argumentIndex = 0;
	private String name;
	private JsonElement value;

	public int compareTo(Argument o) {
		return Integer.compare(argumentIndex, o.argumentIndex);
	}
}

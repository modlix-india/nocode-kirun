package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;

import com.google.gson.JsonPrimitive;

public class Min extends Max {

	public Min() {
		super("Min", List.of(PARAMETER_ARRAY_SOURCE_PRIMITIVE), EVENT_RESULT_ANY);
	}

	@Override
	protected boolean functionSpecificComparator(JsonPrimitive min, JsonPrimitive y) {
		return compareTo(min, y) <= 0;
	}
}

package com.fincity.nocode.kirun.engine.json.schema.convertor.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public enum ConversionMode {

	STRICT,
	LENIENT,
	USE_DEFAULT,
	SKIP;

	public static ConversionMode genericValueOf(String mode) {
		return ConversionMode.valueOf(mode.toUpperCase());
	}

	public static List<JsonElement> getConversionModes() {
		return Arrays.stream(ConversionMode.values()).map(conversionMode ->
				new JsonPrimitive(conversionMode.toString())).collect(Collectors.toList());
	}

}

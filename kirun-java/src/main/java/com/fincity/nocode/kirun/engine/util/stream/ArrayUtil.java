package com.fincity.nocode.kirun.engine.util.stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class ArrayUtil {

	public static JsonElement[] jsonArrayToArray(JsonArray array) {

		JsonElement[] objArray = new JsonElement[array.size()];

		for (int i = 0; i < array.size(); i++)
			objArray[i] = array.get(i);

		return objArray;
	}

	private ArrayUtil() {

	}
}

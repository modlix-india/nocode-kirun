package com.fincity.nocode.kirun.engine.util.stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ArrayUtil {

	public static JsonElement[] jsonArrayToArray(JsonArray array) {

		JsonElement[] objArray = new JsonElement[array.size()];

		for (int i = 0; i < array.size(); i++)
			objArray[i] = array.get(i);

		return objArray;
	}

	public static JsonArray jsonElementsToArray(JsonElement[] elements) {
		JsonArray objArray = new JsonArray(elements.length);

		for (int i = 0; i < elements.length; i++)
			objArray.add(elements[i]);

		return objArray;
	}

	public static JsonPrimitive[] jsonArrayToPrimitive(JsonArray array) {

		JsonPrimitive[] objPrim = new JsonPrimitive[array.size()];

		for (int i = 0; i < array.size(); i++) {
			objPrim[i] = array.get(i).getAsJsonPrimitive();
		}

		return objPrim;
	}

	private ArrayUtil() {

	}
}

package com.fincity.nocode.kirun.engine.util.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.util.primitive.PrimitiveUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtil {

	private JsonUtil() {
	}

	public static Map<String, Object> toMap(JsonObject jsonObject) {

		if (jsonObject == null || jsonObject.isJsonNull())
			return Collections.emptyMap();

		Map<String, Object> map = new HashMap<>();

		for (var entry : jsonObject.entrySet()) {
			map.put(entry.getKey(), toObject(entry.getValue()));
		}

		return map;
	}

	public static List<Object> toList(JsonArray jsonArray) {

		if (jsonArray == null || jsonArray.isEmpty())
			return Collections.emptyList();

		List<Object> list = new ArrayList<>();

		for (JsonElement jsonElement : jsonArray) {
			list.add(toObject(jsonElement));
		}

		return list;
	}

	public static Object toObject(JsonElement jsonElement) {

		if (jsonElement == null || jsonElement.isJsonNull())
			return null;
		if (jsonElement.isJsonObject())
			return toMap(jsonElement.getAsJsonObject());
		if (jsonElement.isJsonArray())
			return toList(jsonElement.getAsJsonArray());
		if (jsonElement.isJsonPrimitive())
			return PrimitiveUtil.findPrimitive(jsonElement).getT2();

		return null;
	}
}


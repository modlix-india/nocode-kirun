package com.fincity.nocode.kirun.engine.util.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class JsonUtilTest {

	@Test
	void testToMapNullInput() {
		Map<String, Object> result = JsonUtil.toMap(null);
		assertTrue(result.isEmpty());
	}

	@Test
	void testToMapJsonNull() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("key", JsonNull.INSTANCE);
		Map<String, Object> result = JsonUtil.toMap(jsonObject);
		assertTrue(result.containsKey("key"));
		assertNull(result.get("key"));
	}

	@Test
	void testToMapNonNullInput() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("str", "value");
		jsonObject.addProperty("num", 42);
		Map<String, Object> result = JsonUtil.toMap(jsonObject);
		assertEquals("value", result.get("str"));
		assertEquals(42, result.get("num"));
	}

	@Test
	void testToListNullInput() {
		List<Object> result = JsonUtil.toList(null);
		assertTrue(result.isEmpty());
	}

	@Test
	void testToListEmptyArray() {
		JsonArray jsonArray = new JsonArray();
		List<Object> result = JsonUtil.toList(jsonArray);
		assertTrue(result.isEmpty());
	}

	@Test
	void testToListNonEmptyArray() {
		JsonArray jsonArray = new JsonArray();
		jsonArray.add(new JsonPrimitive("str"));
		jsonArray.add(new JsonPrimitive(42));
		List<Object> result = JsonUtil.toList(jsonArray);
		assertEquals(2, result.size());
		assertEquals("str", result.get(0));
		assertEquals(42, result.get(1));
	}

	@Test
	void testToObjectNullInput() {

		Object result = JsonUtil.toObject(null);

		assertNull(result);
	}

	@Test
	void testToObjectJsonNull() {
		JsonElement jsonElement = JsonNull.INSTANCE;
		Object result = JsonUtil.toObject(jsonElement);
		assertNull(result);
	}

	@Test
	void testToObjectJsonObject() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("key", "value");

		Object result = JsonUtil.toObject(jsonObject);

		assertInstanceOf(Map.class, result);

		Map<String, Object> map = (Map<String, Object>) result;
		assertEquals("value", map.get("key"));
	}

	@Test
	void testToObjectJsonArray() {
		JsonArray jsonArray = new JsonArray();
		jsonArray.add(new JsonPrimitive("str"));
		jsonArray.add(new JsonPrimitive(42));

		Object result = JsonUtil.toObject(jsonArray);

		assertInstanceOf(List.class, result);

		List<Object> list = (List<Object>) result;

		assertEquals(2, list.size());
		assertEquals("str", list.get(0));
		assertEquals(42, list.get(1));
	}

	@Test
	void testToObjectJsonPrimitive() {
		JsonPrimitive jsonPrimitive = new JsonPrimitive("str");
		Object result = JsonUtil.toObject(jsonPrimitive);
		assertEquals("str", result);
	}
}

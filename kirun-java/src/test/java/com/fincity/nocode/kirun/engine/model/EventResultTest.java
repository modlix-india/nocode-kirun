package com.fincity.nocode.kirun.engine.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonPrimitive;

class EventResultTest {

	@Test
	void test() {
		assertEquals(EventResult.outputOf(Map.of("k", new JsonPrimitive(10))), EventResult.outputOf(Map.of("k", new JsonPrimitive(10))));
	}

}

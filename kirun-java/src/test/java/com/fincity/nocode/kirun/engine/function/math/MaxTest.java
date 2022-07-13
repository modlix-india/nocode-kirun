package com.fincity.nocode.kirun.engine.function.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

class MaxTest {

	@Test
	void test() {

		var maxFunction = new Max();
		var nums = new JsonArray();
		nums.add(3);
		nums.add(5);
		nums.add(6);
		nums.add(10.2);

		assertEquals(new JsonPrimitive(10.2),
		        maxFunction.execute(new FunctionExecutionParameters().setArguments(Map.of("value", (JsonElement) nums)))
		                .get(0)
		                .getResult()
		                .get("value"));
	}

}

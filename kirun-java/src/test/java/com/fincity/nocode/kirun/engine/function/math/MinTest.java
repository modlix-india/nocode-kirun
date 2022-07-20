package com.fincity.nocode.kirun.engine.function.math;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.system.math.Minimum;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

class MinTest {

	@Test
	void test() {

		var minFunction = new Minimum();
		var nums = new JsonArray();
		nums.add(3);
		nums.add(5);
		nums.add(6);
		nums.add(10.2);

		assertEquals(new JsonPrimitive(3),
				minFunction.execute(new FunctionExecutionParameters().setArguments(Map.of("value", (JsonElement) nums)))
						.next().getResult().get("value"));
	}

	@Test
	void test2() {

		var minFunction = new Minimum();
		var nums = new JsonArray();
		Object empty = null;

		assertEquals(empty,
				minFunction.execute(new FunctionExecutionParameters().setArguments(Map.of("value", (JsonElement) nums)))
						.next().getResult().get("value"));
	}

	@Test
	void test3() {

		var minFunction = new Minimum();
		var nums = new JsonArray();
		nums.add(3);
		nums.add(5);
		nums.add(6);
		nums.add(10.2);
		nums.add(0 / 0.0);

		assertEquals(new JsonPrimitive(Double.POSITIVE_INFINITY - Double.POSITIVE_INFINITY),
				minFunction.execute(new FunctionExecutionParameters().setArguments(Map.of("value", (JsonElement) nums)))
						.next().getResult().get("value"));
	}

}

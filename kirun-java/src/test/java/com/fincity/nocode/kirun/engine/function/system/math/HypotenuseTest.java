package com.fincity.nocode.kirun.engine.function.system.math;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

class HypotenuseTest {

	@Test
	void test() {

		Hypotenuse hyp = new Hypotenuse();
		var nums = new JsonArray();
		nums.add(3);
		nums.add(5);
		nums.add(6);

		assertEquals(new JsonPrimitive(8.366600265340756d),
				hyp.execute(new FunctionExecutionParameters().setArguments(Map.of("value", nums))).next().getResult()
						.get("value"));
	}

	@Test
	void test2() {

		Hypotenuse hyp = new Hypotenuse();
		var nums = new JsonArray();

		assertEquals(new JsonPrimitive(0),
				hyp.execute(new FunctionExecutionParameters().setArguments(Map.of("value", nums))).next().getResult()
						.get("value"));
	}

}

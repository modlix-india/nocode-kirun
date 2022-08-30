package com.fincity.nocode.kirun.engine.function.system.math;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.MATH;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class RandomFloatTest {

	private RandomRepository rand = new RandomRepository();

	 @Test
	void test() {
		var min = new JsonPrimitive(1.09e1);

		var max = new JsonPrimitive(1000e4);

		AbstractRandom ran = rand.find(MATH, "RandomFloat");
		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of("minValue", min, "maxValue", max));

		float val = ran.execute(fep).allResults().get(0).getResult().get("value").getAsFloat();
		System.out.println(val);

		assertTrue(val >= min.getAsFloat() && val <= max.getAsFloat());
	}

	@Test
	void test2() {
		var min = new JsonPrimitive(1.09e2);

		AbstractRandom ran = rand.find(MATH, "RandomFloat");
		FunctionExecutionParameters fep = new FunctionExecutionParameters().setArguments(Map.of("minValue", min));

		float val = ran.execute(fep).allResults().get(0).getResult().get("value").getAsFloat();
		System.out.println(val);

		assertTrue(val >= min.getAsFloat() && val <= Float.MAX_VALUE);
	}

	@Test
	void test3() {

		var max = new JsonPrimitive(1.23e4);

		AbstractRandom ran = rand.find(MATH, "RandomFloat");
		FunctionExecutionParameters fep = new FunctionExecutionParameters().setArguments(Map.of("maxValue", max));

		float val = ran.execute(fep).allResults().get(0).getResult().get("value").getAsFloat();
		System.out.println(val);

		assertTrue(val >= Float.MIN_VALUE && val <= max.getAsFloat());
	}
}

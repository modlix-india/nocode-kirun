package com.fincity.nocode.kirun.engine.function.system.math;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.MATH;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class RandomIntTest {

	RandomRepository rand = new RandomRepository();

	@Test
	void test1() {

		var min = new JsonPrimitive(1009);

		var max = new JsonPrimitive(1000012);

		AbstractRandom ran = rand.find(MATH, "RandomInteger");
		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of("minValue", min, "maxValue", max));

		int val = ran.execute(fep).allResults().get(0).getResult().get("value").getAsInt();
		System.out.println(val);

		assertTrue(val >= min.getAsInt() && val <= max.getAsInt());
	}

	@Test
	void test2() {

		var min = new JsonPrimitive(1009);

		AbstractRandom ran = rand.find(MATH, "RandomInteger");
		FunctionExecutionParameters fep = new FunctionExecutionParameters().setArguments(Map.of("minValue", min));

		int val = ran.execute(fep).allResults().get(0).getResult().get("value").getAsInt();
		System.out.println(val);

		assertTrue(val >= min.getAsInt() && val <= Integer.MAX_VALUE);
	}

	@Test
	void test3() {

		var min = new JsonPrimitive(1);

		var max = new JsonPrimitive(2);

		AbstractRandom ran = rand.find(MATH, "RandomInteger");
		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of("minValue", min, "maxValue", max));

		int val = ran.execute(fep).allResults().get(0).getResult().get("value").getAsInt();
		System.out.println(val);

		assertTrue(val >= min.getAsInt() && val <= max.getAsInt());
	}

	@Test
	void test4() {
		AbstractRandom ran = rand.find(MATH, "RandomInteger");
		FunctionExecutionParameters fep = new FunctionExecutionParameters().setArguments(Map.of());

		int val = ran.execute(fep).allResults().get(0).getResult().get("value").getAsInt();
		System.out.println(val);

		assertTrue(val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE);

	}
}

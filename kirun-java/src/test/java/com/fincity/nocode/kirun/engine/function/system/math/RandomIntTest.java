package com.fincity.nocode.kirun.engine.function.system.math;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class RandomIntTest {
	@Test
	void test1() {

		var min = new JsonPrimitive(1009);

		var max = new JsonPrimitive(1000012);

		RandomInt ran = new RandomInt();
		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("minValue", min, "maxValue", max));

		int val = ran.execute(fep).allResults().get(0).getResult().get("value").getAsInt();
		System.out.println(val);

		assertTrue(val >= min.getAsInt() && val <= max.getAsInt());
	}

	@Test
	void test2() {

		var min = new JsonPrimitive(1009);
		RandomInt ran = new RandomInt();
		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("minValue", min));

		int val = ran.execute(fep).allResults().get(0).getResult().get("value").getAsInt();
		System.out.println(val);

		assertTrue(val >= min.getAsInt() && val <= Integer.MAX_VALUE);
	}

	@Test
	void test3() {

		var min = new JsonPrimitive(1);

		var max = new JsonPrimitive(2);
		RandomInt ran = new RandomInt();
		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("minValue", min, "maxValue", max));

		int val = ran.execute(fep).allResults().get(0).getResult().get("value").getAsInt();
		System.out.println(val);

		assertTrue(val >= min.getAsInt() && val <= max.getAsInt());
	}

	@Test
	void test4() {
		RandomInt ran = new RandomInt();
		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of());

		int val = ran.execute(fep).allResults().get(0).getResult().get("value").getAsInt();
		System.out.println(val);

		assertTrue(val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE);

	}
}

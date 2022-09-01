package com.fincity.nocode.kirun.engine.function.system.math;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class RandomFloatTest {

	@Test
	void test() {
		var min = new JsonPrimitive(1.09e2);

		var max = new JsonPrimitive(1000f);

		RandomInt ran = new RandomInt();
		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("minValue", min, "maxValue", max));

		float val = ran.execute(fep).allResults().get(0).getResult().get("value").getAsFloat();
		System.out.println(val);

		assertTrue(val >= min.getAsFloat() && val <= max.getAsFloat());
	}

	@Test
	void test2() {
		var min = new JsonPrimitive(1.09e2);
		RandomInt ran = new RandomInt();
		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("minValue", min));
		float val = ran.execute(fep).allResults().get(0).getResult().get("value").getAsFloat();
		System.out.println(val);
		assertTrue(val >= min.getAsFloat() && val <= Float.MAX_VALUE);
	}

	@Test
	void test3() {
		var max = new JsonPrimitive(1.23e4);
		RandomInt ran = new RandomInt();
		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("maxValue", max));

		float val = ran.execute(fep).allResults().get(0).getResult().get("value").getAsFloat();
		System.out.println(val);

		assertTrue(val >= Float.MIN_VALUE && val <= max.getAsFloat());
	}
}

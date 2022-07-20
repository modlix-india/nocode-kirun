package com.fincity.nocode.kirun.engine.function.math;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.system.MathFunctionRepository;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class MathFunctionRepositoryTest {

	@Test
	void test() {
		MathFunctionRepository math = new MathFunctionRepository();

		assertEquals(new JsonPrimitive(45),
				math.find(Namespaces.MATH, "Absolute")
						.execute(new FunctionExecutionParameters().setArguments(Map.of("value", new JsonPrimitive(45))))
						.allResults().get(0).getResult().get("value"));
	}

	@Test
	void testRound() {
		MathFunctionRepository math = new MathFunctionRepository();

		assertEquals(new JsonPrimitive(12), math.find(Namespaces.MATH, "Round")
				.execute(new FunctionExecutionParameters().setArguments(Map.of("value", new JsonPrimitive(11.6))))
				.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive(18),
				math.find(Namespaces.MATH, "Round")
						.execute(new FunctionExecutionParameters().setArguments(Map.of("value", new JsonPrimitive(18))))
						.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive(1212312434),
				math.find(Namespaces.MATH, "Round")
						.execute(new FunctionExecutionParameters()
								.setArguments(Map.of("value", new JsonPrimitive(1212312434.43))))
						.allResults().get(0).getResult().get("value"));

	}

}

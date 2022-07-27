package com.fincity.nocode.kirun.engine.function.math;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.system.math.MathFunctionRepository;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class MathFunctionRepositoryBinaryTest {

	@Test
	void test() {
		MathFunctionRepository math = new MathFunctionRepository();

		assertEquals(new JsonPrimitive(1.849095985800008d),
				math.find(Namespaces.MATH, "ArcTangent2")
						.execute(new FunctionExecutionParameters()
								.setArguments(Map.of("value1", new JsonPrimitive(14), "value2", new JsonPrimitive(-4))))
						.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive(1.968424318317026d), math.find(Namespaces.MATH, "ArcTangent2")
				.execute(new FunctionExecutionParameters()
						.setArguments(Map.of("value1", new JsonPrimitive(100), "value2", new JsonPrimitive(-42))))
				.allResults().get(0).getResult().get("value"));

	}

	@Test
	void test2() {
		MathFunctionRepository math = new MathFunctionRepository();

		assertEquals(new JsonPrimitive(10000),
				math.find(Namespaces.MATH, "Power")
						.execute(new FunctionExecutionParameters()
								.setArguments(Map.of("value1", new JsonPrimitive(100), "value2", new JsonPrimitive(2))))
						.allResults().get(0).getResult().get("value"));
	}

}

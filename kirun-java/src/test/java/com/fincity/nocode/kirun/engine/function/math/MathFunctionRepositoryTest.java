package com.fincity.nocode.kirun.engine.function.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.system.math.MathFunctionRepository;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class MathFunctionRepositoryTest {

	@Test
	void test() {
		MathFunctionRepository math = new MathFunctionRepository();

		assertEquals(new JsonPrimitive(4),
				math.find(Namespaces.MATH, "Absolute")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("value", new JsonPrimitive(-4))))
						.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive(4.5), math.find(Namespaces.MATH, "Absolute")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("value", new JsonPrimitive(-4.5))))
				.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive(409238490.23),
				math.find(Namespaces.MATH, "Absolute")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(-409238490.23))))
						.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive(0), math.find(Namespaces.MATH, "Absolute")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("value", new JsonPrimitive(-0.0))))
				.allResults().get(0).getResult().get("value"));

	}

	@Test
	void test2() {
		MathFunctionRepository math = new MathFunctionRepository();

		assertEquals(new JsonPrimitive(12), math.find(Namespaces.MATH, "SquareRoot")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("value", new JsonPrimitive(144))))
				.allResults().get(0).getResult().get("value"));
	}

	@Test
	void testRound() {
		MathFunctionRepository math = new MathFunctionRepository();

		assertEquals(new JsonPrimitive(12), math.find(Namespaces.MATH, "Round")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("value", new JsonPrimitive(11.6))))
				.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive(18), math.find(Namespaces.MATH, "Round")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("value", new JsonPrimitive(17.8))))
				.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive(1212312434),
				math.find(Namespaces.MATH, "Round")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(1212312434.43))))
						.allResults().get(0).getResult().get("value"));

	}

	@Test
	void test3() {
		MathFunctionRepository math = new MathFunctionRepository();

		assertEquals(new JsonPrimitive(3), math.find(Namespaces.MATH, "Log10")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("value", new JsonPrimitive(1000))))
				.allResults().get(0).getResult().get("value"));
	}

}

package com.fincity.nocode.kirun.engine.function.math;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.system.math.MathFunctionRepository;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class MathFunctionRepositoryTest {

	@Test
	void test() {
		MathFunctionRepository math = new MathFunctionRepository();

		StepVerifier.create(math.find(Namespaces.MATH, "Absolute")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(-4))))))
				.expectNextMatches(result -> result.next().getResult().get("value").getAsInt() == 4)
				.verifyComplete();

		StepVerifier.create(math.find(Namespaces.MATH, "Absolute")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(-4.5))))))
				.expectNextMatches(result -> result.next().getResult().get("value").getAsDouble() == 4.5)
				.verifyComplete();

		StepVerifier.create(math.find(Namespaces.MATH, "Absolute")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(-409238490.23))))))
				.expectNextMatches(result -> result.next().getResult().get("value").getAsDouble() == 409238490.23)
				.verifyComplete();

		StepVerifier.create(math.find(Namespaces.MATH, "Absolute")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(-0.0))))))
				.expectNextMatches(result -> result.next().getResult().get("value").getAsDouble() == 0.0)
				.verifyComplete();

	}

	@Test
	void test2() {
		MathFunctionRepository math = new MathFunctionRepository();

		StepVerifier.create(math.find(Namespaces.MATH, "SquareRoot")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(144.0))))))
				.expectNextMatches(result -> result.next().getResult().get("value").getAsDouble() == 12.0)
				.verifyComplete();
	}

	@Test
	void testRound() {
		MathFunctionRepository math = new MathFunctionRepository();

		StepVerifier.create(math.find(Namespaces.MATH, "Round")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(11.6))))))
				.expectNextMatches(result -> result.next().getResult().get("value").getAsInt() == 12)
				.verifyComplete();

		StepVerifier.create(math.find(Namespaces.MATH, "Round")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(17.8))))))
				.expectNextMatches(result -> result.next().getResult().get("value").getAsInt() == 18)
				.verifyComplete();

		StepVerifier.create(math.find(Namespaces.MATH, "Round")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(1212312434.43))))))
				.expectNextMatches(result -> result.next().getResult().get("value").getAsInt() == 1212312434)
				.verifyComplete();

	}

	@Test
	void test3() {
		MathFunctionRepository math = new MathFunctionRepository();

		StepVerifier.create(math.find(Namespaces.MATH, "Log10")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(1000))))))
				.expectNextMatches(result -> result.next().getResult().get("value").getAsInt() == 3)
				.verifyComplete();
	}

}

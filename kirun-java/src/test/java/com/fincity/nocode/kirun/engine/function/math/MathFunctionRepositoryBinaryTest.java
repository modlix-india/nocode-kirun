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

class MathFunctionRepositoryBinaryTest {

	@Test
	void test() {
		MathFunctionRepository math = new MathFunctionRepository();

		StepVerifier
				.create(math.find(Namespaces.MATH, "ArcTangent2")
						.flatMap(fun -> fun
								.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
										new KIRunReactiveSchemaRepository())
										.setArguments(Map.of("value1", new JsonPrimitive(14), "value2",
												new JsonPrimitive(-4))))))
				.expectNextMatches(
						fo -> fo.next().getResult().get("value").equals(new JsonPrimitive(1.849095985800008d)))
				.verifyComplete();

		StepVerifier
				.create(math.find(Namespaces.MATH, "ArcTangent2")
						.flatMap(fun -> fun
								.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
										new KIRunReactiveSchemaRepository())
										.setArguments(Map.of("value1", new JsonPrimitive(100), "value2",
												new JsonPrimitive(-42))))))
				.expectNextMatches(
						fo -> fo.next().getResult().get("value").equals(new JsonPrimitive(1.968424318317026d)))
				.verifyComplete();

	}

	@Test
	void test2() {
		MathFunctionRepository math = new MathFunctionRepository();

		StepVerifier
				.create(math.find(Namespaces.MATH, "Power")
						.flatMap(fun -> fun
								.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
										new KIRunReactiveSchemaRepository())
										.setArguments(Map.of("value1", new JsonPrimitive(100), "value2",
												new JsonPrimitive(2))))))
				.expectNextMatches(
						fo -> fo.next().getResult().get("value").equals(new JsonPrimitive(10000)))
				.verifyComplete();
	}

}

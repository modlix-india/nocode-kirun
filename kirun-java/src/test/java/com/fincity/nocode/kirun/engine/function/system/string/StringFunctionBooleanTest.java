package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

public class StringFunctionBooleanTest {

	@Test
	void test() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "IsBlank")
				.flatMap(fun -> {
					return fun
							.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
									new KIRunReactiveSchemaRepository())
									.setArguments(Map.of("value",
											new JsonPrimitive("			no code  Kirun  PLATform		"))))
							.map(fo -> fo.allResults().get(0).getResult().get("value"));
				}))
				.expectNext(new JsonPrimitive(false))
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "IsBlank")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive("						"))))
						.map(fo -> fo.allResults().get(0).getResult().get("value"))))
				.expectNext(new JsonPrimitive(true))
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "IsBlank")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(""))))
						.map(fo -> fo.allResults().get(0).getResult().get("value"))))
				.expectNext(new JsonPrimitive(true))
				.verifyComplete();
	}

	@Test
	void test2() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "IsEmpty")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value",
										new JsonPrimitive("			no code  Kirun  PLATform		"))))
						.map(fo -> fo.allResults().get(0).getResult().get("value"))))
				.expectNext(new JsonPrimitive(false))
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "IsEmpty")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive("						"))))
						.map(fo -> fo.allResults().get(0).getResult().get("value"))))
				.expectNext(new JsonPrimitive(false))
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "IsEmpty")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(""))))
						.map(fo -> fo.allResults().get(0).getResult().get("value"))))
				.expectNext(new JsonPrimitive(true))
				.verifyComplete();
	}
}

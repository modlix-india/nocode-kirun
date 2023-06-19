package com.fincity.nocode.kirun.engine.function.math;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.system.math.Maximum;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class MaxTest {

	@Test
	void test() {

		var maxFunction = new Maximum();
		var nums = new JsonArray();
		nums.add(3);
		nums.add(5);
		nums.add(6);
		nums.add(10.2);

		StepVerifier.create(
				maxFunction.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(Map.of("value", (JsonElement) nums))))
				.expectNextMatches(result -> result.next().getResult().get("value").equals(new JsonPrimitive(10.2)))
				.verifyComplete();

	}

	@Test
	void test2() {

		var maxFunction = new Maximum();
		var nums = new JsonArray();
		Object nullVar = null;

		StepVerifier.create(
				maxFunction.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(Map.of("value", (JsonElement) nums))))
				.expectNextMatches(result -> result.next().getResult().get("value") == nullVar).verifyComplete();
	}

	@Test
	void test3() {

		var maxFunction = new Maximum();
		var nums = new JsonArray();
		nums.add(3);
		nums.add(5);
		nums.add(6);
		nums.add(10.2);
		nums.add(0 / 0.0);

		StepVerifier.create(
				maxFunction.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(Map.of("value", (JsonElement) nums))))
				.expectNextMatches(result -> result.next().getResult().get("value")
						.equals(new JsonPrimitive(Double.NaN)))
				.verifyComplete();
	}

}

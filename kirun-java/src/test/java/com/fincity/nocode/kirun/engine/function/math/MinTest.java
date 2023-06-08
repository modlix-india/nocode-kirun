package com.fincity.nocode.kirun.engine.function.math;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.system.math.Minimum;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class MinTest {

	@Test
	void test() {

		var minFunction = new Minimum();
		var nums = new JsonArray();
		nums.add(3);
		nums.add(5);
		nums.add(6);
		nums.add(10.2);

		StepVerifier.create(
				minFunction.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(Map.of("value", (JsonElement) nums))))
				.expectNextMatches(result -> result.next().getResult().get("value").equals(new JsonPrimitive(3)));
	}

	@Test
	void test2() {

		var minFunction = new Minimum();
		var nums = new JsonArray();
		Object empty = null;

		StepVerifier.create(
				minFunction.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(Map.of("value", (JsonElement) nums))))
				.expectNextMatches(result -> result.next().getResult().get("value") == empty);
	}

	@Test
	void test3() {

		var minFunction = new Minimum();
		var nums = new JsonArray();
		nums.add(3);
		nums.add(5);
		nums.add(6);
		nums.add(10.2);
		nums.add(0 / 0.0);

		StepVerifier.create(
				minFunction.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(Map.of("value", (JsonElement) nums))))
				.expectNextMatches(
						result -> result.next().getResult().get("value").equals(new JsonPrimitive(Double.NaN)));
	}

}

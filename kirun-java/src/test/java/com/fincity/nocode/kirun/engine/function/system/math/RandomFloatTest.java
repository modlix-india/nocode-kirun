package com.fincity.nocode.kirun.engine.function.system.math;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class RandomFloatTest {

	@Test
	void test() {
		var min = new JsonPrimitive(1.09e2);

		var max = new JsonPrimitive(1000f);

		RandomInt ran = new RandomInt();
		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("minValue", min, "maxValue", max));

		StepVerifier.create(ran.execute(fep)).expectNextMatches(r -> {
			var x = r.next().getResult().get("value");
			assertTrue(x.getAsFloat() >= min.getAsFloat() && x.getAsFloat() <= max.getAsFloat());
			return true;
		}).verifyComplete();
	}

	@Test
	void test2() {
		var min = new JsonPrimitive(1.09e2);
		RandomInt ran = new RandomInt();
		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("minValue", min));

		StepVerifier.create(ran.execute(fep)).expectNextMatches(r -> {
			var x = r.next().getResult().get("value");
			assertTrue(x.getAsFloat() >= min.getAsFloat() && x.getAsFloat() <= Float.MAX_VALUE);
			return true;
		}).verifyComplete();
	}

	@Test
	void test3() {
		var max = new JsonPrimitive(1.23e4);
		RandomInt ran = new RandomInt();
		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("maxValue", max));

		StepVerifier.create(ran.execute(fep)).expectNextMatches(r -> {
			var x = r.next().getResult().get("value");
			assertTrue(x.getAsFloat() >= Float.MIN_VALUE && x.getAsFloat() <= max.getAsFloat());
			return true;
		}).verifyComplete();
	}
}

package com.fincity.nocode.kirun.engine.function.system.math;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class RandomIntTest {
	@Test
	void test1() {

		var min = new JsonPrimitive(1009);

		var max = new JsonPrimitive(1000012);

		RandomInt ran = new RandomInt();
		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("minValue", min, "maxValue", max));

		StepVerifier.create(ran.execute(fep)).expectNextMatches(r -> {
			var x = r.next().getResult().get("value");
			assertTrue(x.getAsInt() >= min.getAsInt() && x.getAsInt() <= max.getAsInt());
			return true;
		}).verifyComplete();
	}

	@Test
	void test2() {

		var min = new JsonPrimitive(1009);
		RandomInt ran = new RandomInt();
		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("minValue", min));

		StepVerifier.create(ran.execute(fep)).expectNextMatches(r -> {
			var x = r.next().getResult().get("value");
			assertTrue(x.getAsInt() >= min.getAsInt() && x.getAsInt() <= Integer.MAX_VALUE);
			return true;
		}).verifyComplete();
	}

	@Test
	void test3() {

		var min = new JsonPrimitive(1);

		var max = new JsonPrimitive(2);
		RandomInt ran = new RandomInt();
		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("minValue", min, "maxValue", max));

		StepVerifier.create(ran.execute(fep)).expectNextMatches(r -> {
			var x = r.next().getResult().get("value");
			assertTrue(x.getAsInt() >= min.getAsInt() && x.getAsInt() <= max.getAsInt());
			return true;
		}).verifyComplete();
	}

	@Test
	void test4() {
		RandomInt ran = new RandomInt();
		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository()).setArguments(Map.of());

		StepVerifier.create(ran.execute(fep)).expectNextMatches(r -> {
			var x = r.next().getResult().get("value");
			assertTrue(x.getAsInt() >= Integer.MIN_VALUE && x.getAsInt() <= Integer.MAX_VALUE);
			return true;
		}).verifyComplete();

	}
}

package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class IsLeapYearTest {

	IsLeapYear isleap = new IsLeapYear();
	ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
			new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

	@Test
	void test1() {

		ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("date", new JsonPrimitive("2023-09-07T17:35:17.000Z")));

		StepVerifier.create(isleap.execute(rfep))
				.expectNextMatches(r -> !r.next().getResult().get("output").getAsBoolean()).verifyComplete();

	}

	@Test
	void test2() {

		ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("date", new JsonPrimitive("2020-09-07T17:35:17.000Z")));

		StepVerifier.create(isleap.execute(rfep))
				.expectNextMatches(r -> r.next().getResult().get("output").getAsBoolean()).verifyComplete();

	}

	@Test
	void test3() {

		ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("date", new JsonPrimitive("2020-09-07T17:35:17.000+11:00")));

		StepVerifier.create(isleap.execute(rfep))
				.expectNextMatches(r -> r.next().getResult().get("output").getAsBoolean()).verifyComplete();

	}

	@Test
	void test4() {

		ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("date", new JsonPrimitive("2020-09-07T17:35:17.000+09:00")));

		StepVerifier.create(isleap.execute(rfep))
				.expectNextMatches(r -> r.next().getResult().get("output").getAsBoolean()).verifyComplete();

	}

	@Test
	void test5() {

		ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("date", new JsonPrimitive("2020-12-31T12:13:51.200-12:00")));

		StepVerifier.create(isleap.execute(rfep))
				.expectNextMatches(r -> !r.next().getResult().get("output").getAsBoolean()).verifyComplete();

	}

}

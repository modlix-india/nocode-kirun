package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class MaxTest {

	@Test
	void test() {
		var arr = new JsonArray();
		arr.add(12);

		Max max = new Max();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr));

		StepVerifier.create(max.execute(fep))
				.expectNextMatches(r -> r.next().getResult().get("output").equals(new JsonPrimitive(12)))
				.verifyComplete();

		var arr1 = new JsonArray();
		ReactiveFunctionExecutionParameters fep1 = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr1));

		StepVerifier.create(max.execute(fep1))
				.verifyError(KIRuntimeException.class);
	}

	@Test
	void test2() {
		var arr = new JsonArray();
		arr.add(12);
		arr.add(15);
		arr.add(98);
		arr.add(1);

		Max max = new Max();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr));

		StepVerifier.create(max.execute(fep))
				.expectNextMatches(r -> r.next().getResult().get("output").equals(new JsonPrimitive(98)))
				.verifyComplete();

		var arr1 = new JsonArray();

		arr1.add('c');
		arr1.add('r');
		arr1.add('d');
		arr1.add('s');
		ReactiveFunctionExecutionParameters fep1 = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr1));

		StepVerifier.create(max.execute(fep1))
				.expectNextMatches(r -> r.next().getResult().get("output").equals(new JsonPrimitive('s')))
				.verifyComplete();

	}

	@Test
	void test3() {
		var arr = new JsonArray();
		Max max = new Max();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr));

		StepVerifier.create(max.execute(fep))
				.verifyError(KIRuntimeException.class);
	}

	@Test
	void test4() {
		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("NoCode");
		arr.add("platform");

		Max max = new Max();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr));

		StepVerifier.create(max.execute(fep))
				.expectNextMatches(r -> r.next().getResult().get("output").equals(new JsonPrimitive("platform")))
				.verifyComplete();
	}

	@Test
	void test5() {
		var arr = new JsonArray();
		arr.add(456);
		arr.add("nocode");
		arr.add("NoCode");
		arr.add("platform");
		arr.add(123);

		Max max = new Max();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr));

		StepVerifier.create(max.execute(fep))
				.expectNextMatches(r -> r.next().getResult().get("output").equals(new JsonPrimitive("platform")))
				.verifyComplete();
	}

	@Test
	void test6() {
		Max max = new Max();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", JsonNull.INSTANCE));

		StepVerifier.create(max.execute(fep))
				.verifyError(KIRuntimeException.class);
	}
}

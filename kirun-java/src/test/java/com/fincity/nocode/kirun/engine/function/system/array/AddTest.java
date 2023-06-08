package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;

import reactor.test.StepVerifier;

class AddTest {

	@Test
	void test() {
		var arr = new JsonArray();
		arr.add(12);

		var arr2 = new JsonArray();
		arr2.add(14);

		Add ad = new Add();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr, "secondSource", arr2)).setContext(Map.of()).setSteps(Map.of());

		var res = new JsonArray();
		res.add(12);
		res.add(14);

		StepVerifier.create(ad.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("source").equals(res));

		var arr1 = new JsonArray();
		arr1.add(12);

		var res1 = new JsonArray();
		res1.add(12);
		res1.add(12);
		res1.add(14);

		ReactiveFunctionExecutionParameters fep1 = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr1, "secondSource", arr)).setContext(Map.of()).setSteps(Map.of());

		StepVerifier.create(ad.execute(fep1))
				.expectNextMatches(result -> result.next().getResult().get("source").equals(res1));
	}

	@Test
	void test2() {

		var emp = new JsonArray();

		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("platform");

		Add ad = new Add();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr, "secondSource", emp)).setContext(Map.of()).setSteps(Map.of());

		var res = new JsonArray();
		res.add("nocode");
		res.add("platform");

		StepVerifier.create(ad.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("source").equals(res));
	}

	@Test
	void test3() {
		var emp = new JsonArray();

		var arr = new JsonArray();

		Add ad = new Add();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr, "secondSource", emp)).setContext(Map.of()).setSteps(Map.of());

		StepVerifier.create(ad.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("source").equals(emp));
	}

	@Test
	void test4() {

		var emp = new JsonArray();

		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("platform");

		Add ad = new Add();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", emp, "secondSource", arr)).setContext(Map.of()).setSteps(Map.of());

		var res = new JsonArray();
		res.add("nocode");
		res.add("platform");

		StepVerifier.create(ad.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("source").equals(res));

	}

	@Test
	void test5() {

		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("platform");

		Add ad = new Add();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", JsonNull.INSTANCE, "secondSource", arr)).setContext(Map.of())
				.setSteps(Map.of());

		var res = new JsonArray();
		res.add("nocode");
		res.add("platform");

		StepVerifier.create(ad.execute(fep))
				.verifyError(KIRuntimeException.class);
	}

	@Test
	void test6() {

		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("platform");

		Add ad = new Add();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr, "secondSource", JsonNull.INSTANCE)).setContext(Map.of())
				.setSteps(Map.of());

		var res = new JsonArray();
		res.add("nocode");
		res.add("platform");

		StepVerifier.create(ad.execute(fep))
				.verifyError(KIRuntimeException.class);

	}
}

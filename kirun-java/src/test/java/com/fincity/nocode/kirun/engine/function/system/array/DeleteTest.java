package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import reactor.test.StepVerifier;

class DeleteTest {

	// @Test
	void test() {
		var arr = new JsonArray();
		arr.add(12);
		arr.add(14);
		arr.add(15);
		arr.add(9);

		var res = new JsonArray();
		res.add(14);
		res.add(15);
		res.add(16);

		var ares = new JsonArray();
		ares.add(12);
		ares.add(9);

		Delete del = new Delete();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Delete.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						Delete.PARAMETER_ANY_VAR_ARGS.getParameterName(), res))
				.setContext(Map.of()).setSteps(Map.of());

		StepVerifier.create(del.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("result").equals(ares)).verifyComplete();
	}

	// @Test
	void test2() {

		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("platform");
		arr.add(14);

		var res = new JsonArray();
		res.add("platform");

		Delete del = new Delete();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Delete.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						Delete.PARAMETER_ANY_VAR_ARGS.getParameterName(), res))
				.setSteps(Map.of()).setContext(Map.of());

		var ares = new JsonArray();
		ares.add("nocode");
		ares.add(14);

		StepVerifier.create(del.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("result").equals(ares)).verifyComplete();
	}

	// //@Test
	void test3() {
		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("platform");
		arr.add(14);

		var res = new JsonArray();
		res.add("platform");
		res.add("Nocode");
		res.add(15);

		Delete del = new Delete();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Delete.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						Delete.PARAMETER_ANY_VAR_ARGS.getParameterName(), JsonNull.INSTANCE))
				.setSteps(Map.of()).setContext(Map.of());

		StepVerifier.create(del.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("result").equals(arr)).verifyComplete();
	}

	// @Test
	void test4() {
		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("platform");
		arr.add(14);

		var res = new JsonArray();
		res.add("platform");

		Delete del = new Delete();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Delete.PARAMETER_ARRAY_SOURCE.getParameterName(), JsonNull.INSTANCE,
						Delete.PARAMETER_ANY_VAR_ARGS.getParameterName(), arr))
				.setSteps(Map.of()).setContext(Map.of());

		StepVerifier.create(del.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("result").equals(arr)).verifyComplete();
	}

	@Test
	void test5() {
		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("platform");
		arr.add(14);

		var res = new JsonArray();
		res.add("platform");
		res.add("noCode");

		Delete del = new Delete();

		var ares = new JsonArray();
		ares.add("nocode");
		ares.add(14);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Delete.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						Delete.PARAMETER_ANY_VAR_ARGS.getParameterName(), res))
				.setSteps(Map.of()).setContext(Map.of());

		StepVerifier.create(del.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("result").equals(ares)).verifyComplete();
	}

	@Test
	void test6() {

		var arr1 = new JsonArray();
		arr1.add("nocode");
		arr1.add("platform");
		arr1.add(14);

		var arr2 = new JsonArray();
		arr2.add("nocode");
		arr2.add("platiform");
		arr2.add(14);

		var obj = new JsonObject();
		obj.add("arr", arr1);
		obj.addProperty("sri", "krishna");
		obj.addProperty("name", "surendhar");

		var arr = new JsonArray();
		arr.add(arr1);
		arr.add(arr2);
		arr.add(obj);
		arr.add(arr2);
		arr.add(obj);

		var delArr = new JsonArray();
		delArr.add(obj);
		delArr.add('2');
		delArr.add(new JsonArray());

		var res = new JsonArray();
		res.add(arr1);
		res.add(arr2);
		res.add(arr2);

		Delete del = new Delete();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Delete.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						Delete.PARAMETER_ANY_VAR_ARGS.getParameterName(), delArr))
				.setSteps(Map.of()).setContext(Map.of());

		StepVerifier.create(del.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("result").equals(res)).verifyComplete();
	}

	@Test
	void failingTest() {

		var arr1 = new JsonArray();
		arr1.add("nocode");
		arr1.add("platform");
		arr1.add(14);

		var delArr = new JsonArray();

		delArr.add("platform");
		delArr.add(14);

		Delete del = new Delete();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Delete.PARAMETER_ARRAY_SOURCE.getParameterName(), arr1,
						Delete.PARAMETER_ANY_VAR_ARGS.getParameterName(), delArr))
				.setSteps(Map.of()).setContext(Map.of());

		del.execute(fep);

		var res = new JsonArray();
		res.add("nocode");

		StepVerifier.create(del.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("result").equals(res)).verifyComplete();
	}
}

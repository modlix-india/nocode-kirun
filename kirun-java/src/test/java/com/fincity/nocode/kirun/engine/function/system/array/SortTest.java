package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class SortTest {

	@Test
	void test() {

		var arr = new JsonArray();
		arr.add(12);
		arr.add(15);
		arr.add(98);
		arr.add(1);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository()).setArguments(
						Map.of("source", arr, "findFrom", new JsonPrimitive(0), "length",
								new JsonPrimitive(arr.size())));

		var res = new JsonArray();
		res.add(1);
		res.add(12);
		res.add(15);
		res.add(98);

		Sort sort = new Sort();

		StepVerifier.create(sort.execute(fep))
				.expectNextMatches(r -> r.next().getResult().get("output").equals(res))
				.verifyComplete();
	}

	@Test
	void mytest() {
		var arr = new JsonArray();
		arr.add(12);
		arr.add(15);
		arr.add(98);
		arr.add(1);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository()).setArguments(
						Map.of("source", arr, "findFrom", new JsonPrimitive(1), "ascending", new JsonPrimitive(false)));

		var res = new JsonArray();

		res.add(12);
		res.add(98);
		res.add(15);
		res.add(1);

		Sort sort = new Sort();

		StepVerifier.create(sort.execute(fep))
				.expectNextMatches(r -> r.next().getResult().get("output").equals(res))
				.verifyComplete();
	}

	@Test
	void test2() {

		var arr = new JsonArray();
		arr.add(12);
		arr.add(15);
		arr.add(98);
		arr.add(1);
		arr.add("sure");
		arr.add('c');

		var res = new JsonArray();
		res.add(12);
		res.add(15);
		res.add("sure");
		res.add('c');
		res.add(98);
		res.add(1);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository()).setArguments(
						Map.of("source", arr, "findFrom", new JsonPrimitive(2), "ascending", new JsonPrimitive(false)));

		Sort sort = new Sort();

		StepVerifier.create(sort.execute(fep))
				.expectNextMatches(r -> r.next().getResult().get("output").equals(res))
				.verifyComplete();

	}

}

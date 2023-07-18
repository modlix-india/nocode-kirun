package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import netscape.javascript.JSObject;
import reactor.core.publisher.Mono;
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
		        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
		        .setArguments(Map.of("source", arr, "findFrom", new JsonPrimitive(0), "length",
		                new JsonPrimitive(arr.size())));

		var res = new JsonArray();
		res.add(1);
		res.add(12);
		res.add(15);
		res.add(98);

		Sort sort = new Sort();

		StepVerifier.create(sort.execute(fep))
		        .expectNextMatches(r -> r.next()
		                .getResult()
		                .get("result")
		                .equals(res))
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

		Mono<JsonElement> sort = (new Sort()).execute(fep)
		        .map(FunctionOutput::next)
		        .map(EventResult::getResult)
		        .map(e -> e.get("result"));

		StepVerifier.create(sort)
		        .expectNext(res)
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
		        .expectNextMatches(r -> r.next()
		                .getResult()
		                .get("result")
		                .equals(res))
		        .verifyComplete();

	}

	@Test
	void test3() {

		Gson gson = new Gson();

		String firstJson = """
			[
				{ "order": { "order": 13 } },
						{ "order": { "order": 3 } },
						{ "order": { "order": 130 } },
						{ "order": { "order": 10 } },
						{ "order": { "order": 21 } },
						{ "order": { "order": 1 } }
				]
				""";

		String secondJson = """
			[
				{ "order": { "order": 1 } },
				{ "order": { "order": 3 } },
				{ "order": { "order": 10 } },
				{ "order": { "order": 13 } },
				{ "order": { "order": 21 } },
				{ "order": { "order": 130 } }
			]
				""";
		
		JsonArray arr = gson.fromJson(firstJson,JsonArray.class);
		
		JsonArray res = gson.fromJson(secondJson,JsonArray.class);
		System.out.println(arr);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
		        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository()).setArguments(
		                Map.of("source", arr, "keyPath", new JsonPrimitive("order.order"), "ascending", new JsonPrimitive(true)));

		Sort sort = new Sort();

		StepVerifier.create(sort.execute(fep))
		        .expectNextMatches(r -> r.next()
		                .getResult()
		                .get("result")
		                .equals(res))
		        .verifyComplete();

	}

	@Test
	void test4() {

		Gson gson = new Gson();

		String firstJson = """
			[
        { "order": 13 },
        { "order": 3 },
        { "order": 130 },
        { "order": 10 },
        { "order": 21 },
        { "order": 1 }
    ]
				""";

		String secondJson = """
			[
				{ "order": 1 },
				{ "order": 3 },
				{ "order": 10 },
				{ "order": 13 },
				{ "order": 21 },
				{ "order": 130 }
			]
				""";
		
		JsonArray arr = gson.fromJson(firstJson,JsonArray.class);
		
		JsonArray res = gson.fromJson(secondJson,JsonArray.class);
		System.out.println(arr);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
		        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository()).setArguments(
		                Map.of("source", arr, "keyPath", new JsonPrimitive("order"), "ascending", new JsonPrimitive(true)));

		Sort sort = new Sort();

		StepVerifier.create(sort.execute(fep))
		        .expectNextMatches(r -> r.next()
		                .getResult()
		                .get("result")
		                .equals(res))
		        .verifyComplete();

	}

	@Test
	void test5() {

		Gson gson = new Gson();

		String firstJson = """
			[
        { "order": { "order": 13 } },
        { "order": { "order": 3 } },
        { "order": { "order": 130 } },
        { "order": { "order": 10 } },
        { "order": { "order": 21 } },
        { "order": { "order": 1 } }
    ]
				""";

		String secondJson = """
			[
        { "order": { "order": 130 } },
        { "order": { "order": 21 } },
        { "order": { "order": 13 } },
        { "order": { "order": 10 } },
        { "order": { "order": 3 } },
        { "order": { "order": 1 } }
    ]
				""";
		
		JsonArray arr = gson.fromJson(firstJson,JsonArray.class);
		
		JsonArray res = gson.fromJson(secondJson,JsonArray.class);
		System.out.println(arr);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
		        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository()).setArguments(
		                Map.of("source", arr, "keyPath", new JsonPrimitive("order.order"), "ascending", new JsonPrimitive(false)));

		Sort sort = new Sort();

		StepVerifier.create(sort.execute(fep))
		        .expectNextMatches(r -> r.next()
		                .getResult()
		                .get("result")
		                .equals(res))
		        .verifyComplete();

	}

}

package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class DeleteFromTest {

	@Test
	void test() {
		DeleteFrom del = new DeleteFrom();

		JsonArray arr = new JsonArray();
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('d');
		arr.add('e');
		arr.add('f');
		arr.add('g');
		arr.add('h');
		arr.add('i');
		arr.add('a');
		arr.add('a');
		arr.add('a');

		JsonArray res = new JsonArray();
		res.add('a');
		res.add('b');
		res.add('c');
		res.add('d');
		res.add('e');
		res.add('f');

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr, "srcFrom", new JsonPrimitive(6), "length", new JsonPrimitive(6)))
				.setContext(Map.of()).setSteps(Map.of());

		StepVerifier.create(del.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("result").equals(res)).verifyComplete();
	}

	@Test
	void test2() {
		DeleteFrom del = new DeleteFrom();

		JsonArray arr = new JsonArray();
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('d');
		arr.add('e');
		arr.add('f');
		arr.add('g');
		arr.add('h');
		arr.add('i');
		arr.add('a');
		arr.add('a');
		arr.add('a');

		JsonArray res = new JsonArray();
		res.add('a');
		res.add('b');
		res.add('c');
		res.add('d');
		res.add('e');
		res.add('f');
		res.add('a');
		res.add('a');
		res.add('a');

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr, "srcFrom", new JsonPrimitive(6), "length", new JsonPrimitive(3)))
				.setContext(Map.of()).setSteps(Map.of());

		StepVerifier.create(del.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("result").equals(res)).verifyComplete();
	}

	@Test
	void test3() {

		var array1 = new JsonArray();

		array1.add("test");
		array1.add("Driven");
		array1.add("developement");
		array1.add("I");
		array1.add("am");
		array1.add("using");
		array1.add("eclipse");
		array1.add("I");
		array1.add("to");
		array1.add("test");
		array1.add("the");
		array1.add("changes");
		array1.add("with");
		array1.add("test");
		array1.add("Driven");
		array1.add("developement");

		var array2 = new JsonArray();

		array2.add("test");
		array2.add("Driven");
		array2.add("developement");
		array2.add("I");
		array2.add("am");
		array2.add("using");
		array2.add("eclipse");
		array2.add("I");
		array2.add("to");
		array2.add("test");
		array2.add("the");
		array2.add("changes");
		array2.add("with");

		var array3 = new JsonArray();

		array3.add("test");
		array3.add("Driven");
		array3.add("developement");
		array3.add("I");
		array3.add("am");
		array3.add("using");
		array3.add("eclipse");
		array3.add("I");
		array3.add("to");
		array3.add("test");
		array3.add("the");
		array3.add("changes");
		array3.add("with");
		array3.add("test");
		array3.add("Driven");
		array3.add("developement");

		var array4 = new JsonArray();

		array4.add("test");
		array4.add("Driven");
		array4.add("developement");
		array4.add("I");
		array4.add("am");
		array4.add("using");
		array4.add("eclipse");
		array4.add("I");
		array4.add("to");

		var obj = new JsonObject();

		obj.add("fname", new JsonPrimitive("surendhar"));

		obj.add("lname", new JsonPrimitive(" s"));

		obj.add("age", new JsonPrimitive(23));

		obj.add("company", new JsonPrimitive("Fincity corporation "));

		var arr = new JsonArray();
		arr.add(obj);
		arr.add(array2);
		arr.add(array4);
		arr.add(array1);
		arr.add(array1);
		arr.add(array3);
		arr.add(array2);
		arr.add(array4);
		arr.add(array1);
		arr.add(array1);
		arr.add(array4);

		var res = new JsonArray();

		res.add(obj);
		res.add(array2);
		res.add(array4);
		res.add(array1);
		res.add(array1);
		// res.add(array3);
		// res.add(array2);
		// res.add(array4);
		res.add(array1);
		res.add(array1);
		res.add(array4);

		ReactiveFunctionExecutionParameters fep =

				new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(
								Map.of("source", arr, "srcFrom", new JsonPrimitive(5), "length", new JsonPrimitive(3)))
						.setContext(Map.of()).setSteps(Map.of());

		DeleteFrom del = new DeleteFrom();

		StepVerifier.create(del.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("result").equals(res)).verifyComplete();
	}

	@Test
	void test4() {
		DeleteFrom del = new DeleteFrom();

		JsonArray arr = new JsonArray();
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('d');
		arr.add('e');
		arr.add('f');
		arr.add('g');
		arr.add('h');
		arr.add('i');
		arr.add('a');
		arr.add('a');
		arr.add('a');

		JsonArray res = new JsonArray();
		res.add('a');
		res.add('b');
		res.add('c');
		// res.add('d');
		// res.add('e');
		// res.add('f');
		// res.add('a');
		// res.add('a');
		// res.add('a');

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr, "srcFrom", new JsonPrimitive(3))).setContext(Map.of())
				.setSteps(Map.of());

		StepVerifier.create(del.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("result").equals(res)).verifyComplete();
	}
}

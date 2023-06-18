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

class MisMatchTest {

	@Test
	void test() {
		JsonArray arr = new JsonArray();
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('d');
		arr.add('l');
		arr.add('d');
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('e');
		arr.add('d');

		JsonArray res = new JsonArray();
		res.add('b');
		res.add('c');
		res.add('d');

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr, "findFrom", new JsonPrimitive(7), "secondSource", res,
						"secondSrcFrom", new JsonPrimitive(0), "length", new JsonPrimitive(3)));

		MisMatch mis = new MisMatch();

		StepVerifier.create(mis.execute(fep))
				.expectNextMatches(fo1 -> fo1.allResults().get(0).getResult().get("result").getAsInt() == 2)
				.verifyComplete();
	}

	@Test
	void test2() {
		JsonArray arr = new JsonArray();
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('d');
		arr.add('l');
		arr.add('d');
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('e');
		arr.add('d');

		JsonArray res = new JsonArray();
		res.add('b');
		res.add('c');
		res.add('d');

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr, "findFrom", new JsonPrimitive(0), "secondSource", res,
						"secondSrcFrom", new JsonPrimitive(2), "length", new JsonPrimitive(5)));

		MisMatch mis = new MisMatch();

		StepVerifier.create(mis.execute(fep)).expectError(KIRuntimeException.class).verify();
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

		var arr = new JsonArray();
		arr.add(array2);
		arr.add(array4);
		arr.add(array1);
		arr.add(array1);
		arr.add(array1);
		arr.add(array3);
		arr.add(array2);
		arr.add(array4);
		arr.add(array1);
		arr.add(array1);
		arr.add(array4);

		var res = new JsonArray();
		res.add(array1);
		res.add(array1);
		res.add(array4);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr, "findFrom", new JsonPrimitive(2), "secondSource", res,
						"secondSrcFrom", new JsonPrimitive(3), "length", new JsonPrimitive(3)));

		MisMatch mis = new MisMatch();

		StepVerifier.create(mis.execute(fep))
				.expectNextMatches(fo1 -> fo1.allResults().get(0).getResult().get("result").getAsInt() == 2)
				.verifyComplete();

	}

	@Test
	void test4() {
		var res = new JsonArray();
		res.add(1);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", JsonNull.INSTANCE, "findFrom", new JsonPrimitive(2), "secondSource", res,
						"secondSrcFrom", new JsonPrimitive(3), "length", new JsonPrimitive(3)));

		MisMatch mis = new MisMatch();

		StepVerifier.create(mis.execute(fep)).expectError(KIRuntimeException.class).verify();

		ReactiveFunctionExecutionParameters fep1 = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", res, "findFrom", new JsonPrimitive(2), "secondSource", JsonNull.INSTANCE,
						"secondSrcFrom", new JsonPrimitive(3), "length", new JsonPrimitive(3)));

		StepVerifier.create(mis.execute(fep1)).expectError(KIRuntimeException.class).verify();
	}

}

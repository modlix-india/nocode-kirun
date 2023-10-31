package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

public class JoinTest {
	@Test
	void test() {
		var array = new JsonArray();
		array.add("test");
		array.add("Driven");
		array.add("developement");
		array.add("I");
		array.add("am");
		array.add("using");
		array.add("eclipse");
		array.add("I");
		array.add("to");
		array.add("test");
		array.add("the");
		array.add("changes");
		array.add("with");
		array.add("test");
		array.add("Driven");
		array.add("developement");
		var del = new JsonPrimitive("-");

		Join join = new Join();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", array, "delimiter", del));

		var res = new JsonPrimitive("test-Driven-developement-I-am-using-eclipse-I-to-test-the-changes-with-test-Driven-developement");

		StepVerifier.create(join.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("result").equals(res))
				.verifyComplete();
	}
	
	@Test
	void testWithNull() {
		var array = new JsonArray();
		array.add("test");
		array.add("Driven");
		array.add("developement");
		array.add("I");
		array.add("am");
		array.add("using");
		array.add("eclipse");
		array.add("I");
		array.add("to");
		array.add("test");
		array.add("the");
		array.add(JsonNull.INSTANCE);
		array.add(JsonNull.INSTANCE);
		array.add("changes");
		array.add("with");
		array.add("test");
		array.add("Driven");
		array.add("developement");
		var del = new JsonPrimitive("-");

		Join join = new Join();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", array, "delimiter", del));

		var res = new JsonPrimitive("test-Driven-developement-I-am-using-eclipse-I-to-test-the-changes-with-test-Driven-developement");

		StepVerifier.create(join.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("result").equals(res))
				.verifyComplete();
	}
	
	@Test
	void testWithNoDelimiter() {
		var array = new JsonArray();
		array.add("test");
		array.add("Driven");
		array.add("developement");
		array.add("I");
		array.add("am");
		array.add("using");
		array.add("eclipse");
		array.add("I");
		array.add("to");
		array.add("test");
		array.add("the");
		array.add("changes");
		array.add("with");
		array.add("test");
		array.add("Driven");
		array.add("developement");
		var del = new JsonPrimitive("-");

		Join join = new Join();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", array));

		var res = new JsonPrimitive("testDrivendevelopementIamusingeclipseItotestthechangeswithtestDrivendevelopement");

		StepVerifier.create(join.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get("result").equals(res))
				.verifyComplete();
	}
}

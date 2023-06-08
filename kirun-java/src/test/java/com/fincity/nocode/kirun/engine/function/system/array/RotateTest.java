package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class RotateTest {

	@Test
	void test() {
		var array = new JsonArray();
		array.add("I");
		array.add("am");
		array.add("using");
		array.add("eclipse");
		array.add("to");
		array.add("test");
		array.add("the");
		array.add("changes");
		array.add("with");
		array.add("test");
		array.add("Driven");
		array.add("developement");

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

		fep.setArguments(Map.of("source", array, "rotateDistance", new JsonPrimitive(4))).setContext(Map.of())
				.setSteps(Map.of());

		var res = new JsonArray();

		res.add("to");
		res.add("test");
		res.add("the");
		res.add("changes");
		res.add("with");
		res.add("test");
		res.add("Driven");
		res.add("developement");

		res.add("I");
		res.add("am");
		res.add("using");
		res.add("eclipse");

		Rotate rotate = new Rotate();
		rotate.execute(fep).block();

		assertEquals(res, array);

		ReactiveFunctionExecutionParameters fep1 = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

		fep1.setArguments(Map.of("source", array, "rotateDistance", new JsonPrimitive(0))).setContext(Map.of())
				.setSteps(Map.of());

		StepVerifier.create(rotate.execute(fep1)).expectError(KIRuntimeException.class).verify();
	}

	@Test
	void test2() {
		var array = new JsonArray();
		array.add("I");
		array.add("am");
		array.add("using");
		array.add("eclipse");
		array.add("to");
		array.add("test");
		array.add("the");
		array.add("changes");
		array.add("with");
		array.add("test");
		array.add("Driven");
		array.add("developement");

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

		fep.setArguments(Map.of("source", array, "rotateDistance", new JsonPrimitive(4))).setContext(Map.of())
				.setSteps(Map.of());

		var res = new JsonArray();

		res.add("to");
		res.add("test");
		res.add("the");
		res.add("changes");
		res.add("with");
		res.add("test");
		res.add("Driven");
		res.add("developement");

		res.add("I");
		res.add("am");
		res.add("using");
		res.add("eclipse");

		Rotate rotate = new Rotate();
		rotate.execute(fep).block();

		assertEquals(res, array);

		ReactiveFunctionExecutionParameters fep1 = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

		fep1.setArguments(Map.of("source", array, "rotateDistance", new JsonPrimitive(-2))).setContext(Map.of())
				.setSteps(Map.of());

		StepVerifier.create(rotate.execute(fep1)).expectError(KIRuntimeException.class).verify();
	}

}

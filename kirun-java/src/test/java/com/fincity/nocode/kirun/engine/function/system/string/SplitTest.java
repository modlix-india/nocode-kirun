package com.fincity.nocode.kirun.engine.function.system.string;

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

class SplitTest {

	@Test
	void test1() {

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

		Split split = new Split();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Split.PARAMETER_STRING_NAME,
						new JsonPrimitive("I am using eclipse to test the changes with test Driven developement"),
						Split.PARAMETER_SPLIT_STRING_NAME, new JsonPrimitive(" ")));

		StepVerifier.create(split.execute(fep).map(fo -> fo.allResults().get(0).getResult().get("result")))
				.expectNext(array)
				.verifyComplete();
	}

	@Test
	void test2() {

		var array = new JsonArray();
		array.add("I am using ");
		array.add("clips");
		array.add(" to t");
		array.add("st th");
		array.add(" chang");
		array.add("s with t");
		array.add("st Driv");
		array.add("n d");
		array.add("v");
		array.add("lop");
		array.add("m");
		array.add("nt");

		Split split = new Split();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Split.PARAMETER_STRING_NAME,
						new JsonPrimitive("I am using eclipse to test the changes with test Driven developement"),
						Split.PARAMETER_SPLIT_STRING_NAME, new JsonPrimitive("e")));

		StepVerifier.create(split.execute(fep).map(fo -> fo.allResults().get(0).getResult().get("result")))
				.expectNext(array)
				.verifyComplete();
	}

	@Test
	void test3() {

		Split split = new Split();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Split.PARAMETER_STRING_NAME, JsonNull.INSTANCE, Split.PARAMETER_SPLIT_STRING_NAME,
						new JsonPrimitive("e")));

		StepVerifier.create(split.execute(fep))
				.verifyError(KIRuntimeException.class);
	}

}

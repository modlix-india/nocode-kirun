package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ReverseTest {

	@Test
	void test() {

		Reverse rev = new Reverse();

		StepVerifier.create(rev
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of("value", new JsonPrimitive("This is a no code pl\"atfo\"rm "))))
				.map(fo -> fo.allResults().get(0).getResult().get("value")))
				.expectNext(new JsonPrimitive(" mr\"ofta\"lp edoc on a si sihT"))
				.verifyComplete();
	}

	@Test
	void testEmptyString() {
		Reverse rev = new Reverse();

		StepVerifier.create(rev
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of("value", new JsonPrimitive(""))))
				.map(fo -> fo.allResults().get(0).getResult().get("value")))
				.expectNext(new JsonPrimitive(""))
				.verifyComplete();
	}

	@Test
	void testSingleCharacter() {
		Reverse rev = new Reverse();

		StepVerifier.create(rev
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of("value", new JsonPrimitive("a"))))
				.map(fo -> fo.allResults().get(0).getResult().get("value")))
				.expectNext(new JsonPrimitive("a"))
				.verifyComplete();
	}

	@Test
	void testPalindrome() {
		Reverse rev = new Reverse();

		StepVerifier.create(rev
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of("value", new JsonPrimitive("racecar"))))
				.map(fo -> fo.allResults().get(0).getResult().get("value")))
				.expectNext(new JsonPrimitive("racecar"))
				.verifyComplete();
	}

	@Test
	void testSpecialCharacters() {
		Reverse rev = new Reverse();

		StepVerifier.create(rev
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of("value", new JsonPrimitive("!@#$%"))))
				.map(fo -> fo.allResults().get(0).getResult().get("value")))
				.expectNext(new JsonPrimitive("%$#@!"))
				.verifyComplete();
	}

	@Test
	void testNumbersInString() {
		Reverse rev = new Reverse();

		StepVerifier.create(rev
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of("value", new JsonPrimitive("12345"))))
				.map(fo -> fo.allResults().get(0).getResult().get("value")))
				.expectNext(new JsonPrimitive("54321"))
				.verifyComplete();
	}

}

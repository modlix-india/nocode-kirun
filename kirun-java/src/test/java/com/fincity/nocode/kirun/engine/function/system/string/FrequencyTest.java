package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class FrequencyTest {

	@Test
	void test() {
		String s1 = " THIS IS A NOcoDE plATFNORM";
		String s2 = "";
		Frequency freq = new Frequency();

		StepVerifier.create(freq
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(Frequency.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								Frequency.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive(s2))))
				.map(fo -> fo.allResults().get(0).getResult().get(Frequency.EVENT_RESULT_NAME)
						.getAsNumber()))
				.expectNext(0)
				.verifyComplete();
	}

	@Test
	void test2() {
		String s1 = " THIS IS A NOcoDE plATFNORM";
		String s2 = "NO";
		Frequency freq = new Frequency();

		StepVerifier.create(freq
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(Frequency.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								Frequency.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive(s2))))
				.map(fo -> fo.allResults().get(0).getResult().get(Frequency.EVENT_RESULT_NAME)
						.getAsNumber()))
				.expectNext(2)
				.verifyComplete();
	}

	@Test
	void test3() {
		String s1 = "";
		String s2 = "sdf";
		Frequency freq = new Frequency();

		StepVerifier.create(freq
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(Frequency.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								Frequency.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive(s2))))
				.map(fo -> fo.allResults().get(0).getResult().get(Frequency.EVENT_RESULT_NAME)
						.getAsNumber()))
				.expectNext(0)
				.verifyComplete();
	}

	@Test
	void test4() {
		String s1 = "";
		String s2 = "";
		Frequency freq = new Frequency();

		StepVerifier.create(freq
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(Frequency.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								Frequency.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive(s2))))
				.map(fo -> fo.allResults().get(0).getResult().get(Frequency.EVENT_RESULT_NAME)
						.getAsNumber()))
				.expectNext(0)
				.verifyComplete();
	}
}

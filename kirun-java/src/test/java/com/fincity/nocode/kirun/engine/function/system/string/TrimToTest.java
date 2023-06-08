package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class TrimToTest {

	@Test
	void test() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";

		TrimTo trimed = new TrimTo();

		StepVerifier
				.create(trimed.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(TrimTo.PARAMETER_STRING_NAME,
										new JsonPrimitive(s1), TrimTo.PARAMETER_LENGTH_NAME, new JsonPrimitive(14)))))
				.expectNextMatches(fo -> fo.next().getResult().get(TrimTo.EVENT_RESULT_NAME).getAsString()
						.equals(" THIScompatY I"))
				.verifyComplete();
	}

	@Test
	void test2() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";

		TrimTo trimed = new TrimTo();

		StepVerifier
				.create(trimed.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(TrimTo.PARAMETER_STRING_NAME,
										new JsonPrimitive(s1), TrimTo.PARAMETER_LENGTH_NAME, new JsonPrimitive(0)))))
				.expectNextMatches(fo -> fo.next().getResult().get(TrimTo.EVENT_RESULT_NAME).getAsString()
						.equals(""))
				.verifyComplete();
	}
}

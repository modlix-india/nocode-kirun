package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class PrePadTest {

	@Test
	void test() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = "hiran";

		PrePad prepad = new PrePad();

		StepVerifier.create(prepad
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(PrePad.PARAMETER_STRING_NAME,
										new JsonPrimitive(s1), PrePad.PARAMETER_PREPAD_STRING_NAME,
										new JsonPrimitive(s2),

										PrePad.PARAMETER_LENGTH_NAME, new JsonPrimitive(12))))
				.map(fo -> fo.allResults().get(0).getResult().get(PrePad.EVENT_RESULT_NAME).getAsString()))
				.expectNext("hiranhiranhi THIScompatY IS A NOcoDE plATFNORM")
				.verifyComplete();
	}

	@Test
	void test2() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = " h ";

		PrePad prepad = new PrePad();

		StepVerifier.create(prepad
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(PrePad.PARAMETER_STRING_NAME,
										new JsonPrimitive(s1), PrePad.PARAMETER_PREPAD_STRING_NAME,
										new JsonPrimitive(s2),

										PrePad.PARAMETER_LENGTH_NAME, new JsonPrimitive(11))))
				.map(fo -> fo.allResults().get(0).getResult().get(PrePad.EVENT_RESULT_NAME).getAsString()))
				.expectNext(" h  h  h  h THIScompatY IS A NOcoDE plATFNORM")
				.verifyComplete();
	}

	@Test
	void test3() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = "hiran";

		PrePad prepad = new PrePad();

		StepVerifier.create(prepad
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(PrePad.PARAMETER_STRING_NAME,
										new JsonPrimitive(s1), PrePad.PARAMETER_PREPAD_STRING_NAME,
										new JsonPrimitive(s2),

										PrePad.PARAMETER_LENGTH_NAME, new JsonPrimitive(4))))
				.map(fo -> fo.allResults().get(0).getResult().get(PrePad.EVENT_RESULT_NAME).getAsString()))
				.expectNext("hira THIScompatY IS A NOcoDE plATFNORM")
				.verifyComplete();
	}
}

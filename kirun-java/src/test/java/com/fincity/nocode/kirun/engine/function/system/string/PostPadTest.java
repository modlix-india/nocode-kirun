package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class PostPadTest {

	@Test
	void test() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = "hiran";

		PostPad postpad = new PostPad();

		StepVerifier.create(postpad
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(PostPad.PARAMETER_STRING_NAME,
										new JsonPrimitive(s1), PostPad.PARAMETER_POSTPAD_STRING_NAME,
										new JsonPrimitive(s2),

										PostPad.PARAMETER_LENGTH_NAME, new JsonPrimitive(12))))
				.map(fo -> fo.allResults().get(0).getResult().get(PostPad.EVENT_RESULT_NAME).getAsString()))
				.expectNext(" THIScompatY IS A NOcoDE plATFNORMhiranhiranhi")
				.verifyComplete();
	}

	@Test
	void test2() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = " h ";

		PostPad prepad = new PostPad();

		StepVerifier.create(prepad
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(PostPad.PARAMETER_STRING_NAME,
										new JsonPrimitive(s1), PostPad.PARAMETER_POSTPAD_STRING_NAME,
										new JsonPrimitive(s2),

										PostPad.PARAMETER_LENGTH_NAME, new JsonPrimitive(11))))
				.map(fo -> fo.allResults().get(0).getResult().get(PostPad.EVENT_RESULT_NAME).getAsString()))
				.expectNext(" THIScompatY IS A NOcoDE plATFNORM h  h  h  h")
				.verifyComplete();
	}

	@Test
	void tes3() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = "hiran";

		PostPad postpad = new PostPad();

		StepVerifier.create(postpad
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(PostPad.PARAMETER_STRING_NAME,
										new JsonPrimitive(s1), PostPad.PARAMETER_POSTPAD_STRING_NAME,
										new JsonPrimitive(s2),

										PostPad.PARAMETER_LENGTH_NAME, new JsonPrimitive(3))))
				.map(fo -> fo.allResults().get(0).getResult().get(PostPad.EVENT_RESULT_NAME).getAsString()))
				.expectNext(" THIScompatY IS A NOcoDE plATFNORMhir")
				.verifyComplete();
	}

}

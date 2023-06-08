package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ReplaceAtGivenPositionTest {

	@Test
	void test() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = " fincitY compatY ";

		ReplaceAtGivenPosition insert = new ReplaceAtGivenPosition();

		StepVerifier.create(insert
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(ReplaceAtGivenPosition.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								ReplaceAtGivenPosition.PARAMETER_AT_START_NAME, new JsonPrimitive(13),
								ReplaceAtGivenPosition.PARAMETER_AT_LENGTH_NAME, new JsonPrimitive(s2.length()),
								ReplaceAtGivenPosition.PARAMETER_REPLACE_STRING_NAME, new JsonPrimitive(s2))))
				.map(fo -> fo.allResults().get(0).getResult().get(ReplaceAtGivenPosition.EVENT_RESULT_NAME)
						.getAsString()))
				.expectNext(" THIScompatY  fincitY compatY NORM").verifyComplete();
	}

	@Test
	void test2() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = "surendhar";

		ReplaceAtGivenPosition insert = new ReplaceAtGivenPosition();

		StepVerifier.create(insert
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(ReplaceAtGivenPosition.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								ReplaceAtGivenPosition.PARAMETER_AT_START_NAME, new JsonPrimitive(4),
								ReplaceAtGivenPosition.PARAMETER_AT_LENGTH_NAME, new JsonPrimitive(s2.length()),
								ReplaceAtGivenPosition.PARAMETER_REPLACE_STRING_NAME, new JsonPrimitive(s2))))
				.map(fo -> fo.allResults().get(0).getResult().get(ReplaceAtGivenPosition.EVENT_RESULT_NAME)
						.getAsString()))
				.expectNext(" THIsurendharIS A NOcoDE plATFNORM").verifyComplete();
	}

}

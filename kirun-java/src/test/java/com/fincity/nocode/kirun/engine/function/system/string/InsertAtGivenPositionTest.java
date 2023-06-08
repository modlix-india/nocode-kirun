package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class InsertAtGivenPositionTest {

	@Test
	void test() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = "surendhar";

		InsertAtGivenPosition insert = new InsertAtGivenPosition();

		StepVerifier.create(insert
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(InsertAtGivenPosition.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								InsertAtGivenPosition.PARAMETER_INSERT_STRING_NAME, new JsonPrimitive(s2),
								InsertAtGivenPosition.PARAMETER_AT_POSITION_NAME, new JsonPrimitive(6))))
				.map(fo -> fo.allResults().get(0).getResult().get(InsertAtGivenPosition.EVENT_RESULT_NAME)
						.getAsString()))
				.expectNext(" THIScsurendharompatY IS A NOcoDE plATFNORM")
				.verifyComplete();
	}

	@Test
	void test2() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = "surendhar";

		InsertAtGivenPosition insert = new InsertAtGivenPosition();

		StepVerifier.create(insert
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(InsertAtGivenPosition.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								InsertAtGivenPosition.PARAMETER_INSERT_STRING_NAME, new JsonPrimitive(s2),
								InsertAtGivenPosition.PARAMETER_AT_POSITION_NAME, new JsonPrimitive(22))))
				.map(fo -> fo.allResults().get(0).getResult().get(InsertAtGivenPosition.EVENT_RESULT_NAME)
						.getAsString()))
				.expectNext(" THIScompatY IS A NOcosurendharDE plATFNORM")
				.verifyComplete();
	}

}

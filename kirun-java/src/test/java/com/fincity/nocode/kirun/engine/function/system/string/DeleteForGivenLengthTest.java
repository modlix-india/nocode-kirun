package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class DeleteForGivenLengthTest {

	@Test
	void test() {
		String s1 = " THIScompatY IS A NOcoDE plATFNORM";

		DeleteForGivenLength delete = new DeleteForGivenLength();

		StepVerifier.create(delete
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(DeleteForGivenLength.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								DeleteForGivenLength.PARAMETER_AT_START_NAME, new JsonPrimitive(10),
								DeleteForGivenLength.PARAMETER_AT_END_NAME, new JsonPrimitive(18))))
				.map(fo -> fo.allResults().get(0).getResult().get(DeleteForGivenLength.EVENT_RESULT_NAME)
						.getAsString()))
				.expectNext(" THIScompaNOcoDE plATFNORM")
				.verifyComplete();
	}

	@Test
	void test2() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";

		DeleteForGivenLength insert = new DeleteForGivenLength();

		StepVerifier.create(insert
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(DeleteForGivenLength.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								DeleteForGivenLength.PARAMETER_AT_START_NAME, new JsonPrimitive(4),
								DeleteForGivenLength.PARAMETER_AT_END_NAME, new JsonPrimitive(10))))
				.map(fo -> fo.allResults().get(0).getResult().get(DeleteForGivenLength.EVENT_RESULT_NAME)
						.getAsString()))
				.expectNext(" THItY IS A NOcoDE plATFNORM")
				.verifyComplete();
	}
}

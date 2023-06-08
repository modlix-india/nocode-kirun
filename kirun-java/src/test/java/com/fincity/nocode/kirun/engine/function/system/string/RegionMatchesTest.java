package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class RegionMatchesTest {

	@Test
	void test() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = " fincitY compatY ";

		RegionMatches reg = new RegionMatches();

		StepVerifier.create(reg
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(RegionMatches.PARAMETER_STRING_NAME,
										new JsonPrimitive(s1), RegionMatches.PARAMETER_BOOLEAN_NAME,
										new JsonPrimitive(true),
										RegionMatches.PARAMETER_FIRST_OFFSET_NAME, new JsonPrimitive(5),
										RegionMatches.PARAMETER_OTHER_STRING_NAME, new JsonPrimitive(s2),
										RegionMatches.PARAMETER_SECOND_OFFSET_NAME, new JsonPrimitive(9),
										RegionMatches.PARAMETER_INTEGER_NAME, new JsonPrimitive(7))))
				.map(fo -> fo.allResults().get(0).getResult().get(RegionMatches.EVENT_RESULT_NAME).getAsBoolean()))
				.expectNext(true).verifyComplete();
	}

	@Test
	void test2() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = " fincitY compatY ";

		RegionMatches reg = new RegionMatches();

		StepVerifier.create(reg
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(RegionMatches.PARAMETER_STRING_NAME,
										new JsonPrimitive(s1), RegionMatches.PARAMETER_BOOLEAN_NAME,
										new JsonPrimitive(false),
										RegionMatches.PARAMETER_FIRST_OFFSET_NAME, new JsonPrimitive(5),
										RegionMatches.PARAMETER_OTHER_STRING_NAME, new JsonPrimitive(s2),
										RegionMatches.PARAMETER_SECOND_OFFSET_NAME, new JsonPrimitive(1),
										RegionMatches.PARAMETER_INTEGER_NAME, new JsonPrimitive(7))))
				.map(fo -> fo.allResults().get(0).getResult().get(RegionMatches.EVENT_RESULT_NAME).getAsBoolean()))
				.expectNext(false).verifyComplete();
	}

}

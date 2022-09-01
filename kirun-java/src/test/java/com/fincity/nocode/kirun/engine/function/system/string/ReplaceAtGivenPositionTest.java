package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class ReplaceAtGivenPositionTest {

	@Test
	void test() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = " fincitY compatY ";

		ReplaceAtGivenPosition insert = new ReplaceAtGivenPosition();

		assertEquals(new JsonPrimitive(" THIScompatY  fincitY compatY NORM"), insert
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
						.setArguments(Map.of(ReplaceAtGivenPosition.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								ReplaceAtGivenPosition.PARAMETER_AT_START_NAME, new JsonPrimitive(13),
								ReplaceAtGivenPosition.PARAMETER_AT_LENGTH_NAME, new JsonPrimitive(s2.length()),
								ReplaceAtGivenPosition.PARAMETER_REPLACE_STRING_NAME, new JsonPrimitive(s2))))
				.allResults().get(0).getResult().get(ReplaceAtGivenPosition.EVENT_RESULT_NAME));
	}

	@Test
	void test2() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = "surendhar";

		ReplaceAtGivenPosition insert = new ReplaceAtGivenPosition();

		assertEquals(new JsonPrimitive(" THIsurendharIS A NOcoDE plATFNORM"), insert
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
						.setArguments(Map.of(ReplaceAtGivenPosition.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								ReplaceAtGivenPosition.PARAMETER_AT_START_NAME, new JsonPrimitive(4),
								ReplaceAtGivenPosition.PARAMETER_AT_LENGTH_NAME, new JsonPrimitive(s2.length()),
								ReplaceAtGivenPosition.PARAMETER_REPLACE_STRING_NAME, new JsonPrimitive(s2))))
				.allResults().get(0).getResult().get(ReplaceAtGivenPosition.EVENT_RESULT_NAME));
	}

}

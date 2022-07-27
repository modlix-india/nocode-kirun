package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class InsertAtGivenPositionTest {

	@Test
	void test() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = "surendhar";

		InsertAtGivenPosition insert = new InsertAtGivenPosition();

		assertEquals(new JsonPrimitive(" THIScsurendharompatY IS A NOcoDE plATFNORM"), insert
				.execute(new FunctionExecutionParameters()
						.setArguments(Map.of(InsertAtGivenPosition.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								InsertAtGivenPosition.PARAMETER_INSERT_STRING_NAME, new JsonPrimitive(s2),
								InsertAtGivenPosition.PARAMETER_AT_POSITION_NAME, new JsonPrimitive(6))))
				.allResults().get(0).getResult().get(InsertAtGivenPosition.EVENT_RESULT_NAME));
	}

	@Test
	void test2() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = "surendhar";

		InsertAtGivenPosition insert = new InsertAtGivenPosition();

		assertEquals(new JsonPrimitive(" THIScompatY IS A NOcosurendharDE plATFNORM"), insert
				.execute(new FunctionExecutionParameters()
						.setArguments(Map.of(InsertAtGivenPosition.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								InsertAtGivenPosition.PARAMETER_INSERT_STRING_NAME, new JsonPrimitive(s2),
								InsertAtGivenPosition.PARAMETER_AT_POSITION_NAME, new JsonPrimitive(22))))
				.allResults().get(0).getResult().get(InsertAtGivenPosition.EVENT_RESULT_NAME));
	}

}

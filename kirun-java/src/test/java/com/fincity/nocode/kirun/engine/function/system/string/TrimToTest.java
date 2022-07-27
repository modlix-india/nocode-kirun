package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class TrimToTest {

	@Test
	void test() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";

		TrimTo trimed = new TrimTo();

		assertEquals(new JsonPrimitive(" THIScompatY I"),
				trimed.execute(new FunctionExecutionParameters().setArguments(Map.of(TrimTo.PARAMETER_STRING_NAME,
						new JsonPrimitive(s1), TrimTo.PARAMETER_LENGTH_NAME, new JsonPrimitive(14)))).allResults()
						.get(0).getResult().get(TrimTo.EVENT_RESULT_NAME));
	}

	@Test
	void test2() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";

		TrimTo trimed = new TrimTo();

		assertEquals(new JsonPrimitive(""),
				trimed.execute(new FunctionExecutionParameters().setArguments(Map.of(TrimTo.PARAMETER_STRING_NAME,
						new JsonPrimitive(s1), TrimTo.PARAMETER_LENGTH_NAME, new JsonPrimitive(0)))).allResults().get(0)
						.getResult().get(TrimTo.EVENT_RESULT_NAME));
	}
}

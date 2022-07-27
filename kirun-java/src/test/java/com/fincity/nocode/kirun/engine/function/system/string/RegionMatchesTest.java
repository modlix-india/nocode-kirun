package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class RegionMatchesTest {

	@Test
	void test() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = " fincitY compatY ";

		RegionMatches reg = new RegionMatches();

		assertEquals(new JsonPrimitive(true),
				reg.execute(new FunctionExecutionParameters().setArguments(Map.of(RegionMatches.PARAMETER_STRING_NAME,
						new JsonPrimitive(s1), RegionMatches.PARAMETER_BOOLEAN_NAME, new JsonPrimitive(true),
						RegionMatches.PARAMETER_FIRST_OFFSET_NAME, new JsonPrimitive(5),
						RegionMatches.PARAMETER_OTHER_STRING_NAME, new JsonPrimitive(s2),
						RegionMatches.PARAMETER_SECOND_OFFSET_NAME, new JsonPrimitive(9),
						RegionMatches.PARAMETER_INTEGER_NAME, new JsonPrimitive(7)))).allResults().get(0).getResult()
						.get(RegionMatches.EVENT_RESULT_NAME));
	}

	@Test
	void test2() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = " fincitY compatY ";

		RegionMatches reg = new RegionMatches();

		assertEquals(new JsonPrimitive(false),
				reg.execute(new FunctionExecutionParameters().setArguments(Map.of(RegionMatches.PARAMETER_STRING_NAME,
						new JsonPrimitive(s1), RegionMatches.PARAMETER_BOOLEAN_NAME, new JsonPrimitive(false),
						RegionMatches.PARAMETER_FIRST_OFFSET_NAME, new JsonPrimitive(5),
						RegionMatches.PARAMETER_OTHER_STRING_NAME, new JsonPrimitive(s2),
						RegionMatches.PARAMETER_SECOND_OFFSET_NAME, new JsonPrimitive(1),
						RegionMatches.PARAMETER_INTEGER_NAME, new JsonPrimitive(7)))).allResults().get(0).getResult()
						.get(RegionMatches.EVENT_RESULT_NAME));
	}

}

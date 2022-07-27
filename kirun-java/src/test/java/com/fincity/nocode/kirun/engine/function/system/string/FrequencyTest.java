package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class FrequencyTest {

	@Test
	void test() {
		String s1 = " THIS IS A NOcoDE plATFNORM";
		String s2 = "";
		Frequency freq = new Frequency();

		assertEquals(new JsonPrimitive(0),
				freq.execute(new FunctionExecutionParameters().setArguments(Map.of(Frequency.PARAMETER_STRING_NAME,
						new JsonPrimitive(s1), Frequency.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive(s2))))
						.allResults().get(0).getResult().get(Frequency.EVENT_RESULT_NAME));
	}

	@Test
	void test2() {
		String s1 = " THIS IS A NOcoDE plATFNORM";
		String s2 = "NO";
		Frequency freq = new Frequency();

		assertEquals(new JsonPrimitive(2),
				freq.execute(new FunctionExecutionParameters().setArguments(Map.of(Frequency.PARAMETER_STRING_NAME,
						new JsonPrimitive(s1), Frequency.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive(s2))))
						.allResults().get(0).getResult().get(Frequency.EVENT_RESULT_NAME));
	}

	@Test
	void test3() {
		String s1 = "";
		String s2 = "sdf";
		Frequency freq = new Frequency();

		assertEquals(new JsonPrimitive(0),
				freq.execute(new FunctionExecutionParameters().setArguments(Map.of(Frequency.PARAMETER_STRING_NAME,
						new JsonPrimitive(s1), Frequency.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive(s2))))
						.allResults().get(0).getResult().get(Frequency.EVENT_RESULT_NAME));
	}

	@Test
	void test4() {
		String s1 = "";
		String s2 = "";
		Frequency freq = new Frequency();

		assertEquals(new JsonPrimitive(0),
				freq.execute(new FunctionExecutionParameters().setArguments(Map.of(Frequency.PARAMETER_STRING_NAME,
						new JsonPrimitive(s1), Frequency.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive(s2))))
						.allResults().get(0).getResult().get(Frequency.EVENT_RESULT_NAME));
	}
}

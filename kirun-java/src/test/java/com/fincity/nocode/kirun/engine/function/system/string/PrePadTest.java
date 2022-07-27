package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class PrePadTest {

	@Test
	void test() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = "hiran";

		PrePad prepad = new PrePad();

		assertEquals(new JsonPrimitive("hiranhiranhi THIScompatY IS A NOcoDE plATFNORM"),
				prepad.execute(new FunctionExecutionParameters().setArguments(Map.of(PrePad.PARAMETER_STRING_NAME,
						new JsonPrimitive(s1), PrePad.PARAMETER_PREPAD_STRING_NAME, new JsonPrimitive(s2),

						PrePad.PARAMETER_LENGTH_NAME, new JsonPrimitive(12)))).allResults().get(0).getResult()
						.get(PrePad.EVENT_RESULT_NAME));
	}

	@Test
	void test2() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = " h ";

		PrePad prepad = new PrePad();

		assertEquals(new JsonPrimitive(" h  h  h  h THIScompatY IS A NOcoDE plATFNORM"),
				prepad.execute(new FunctionExecutionParameters().setArguments(Map.of(PrePad.PARAMETER_STRING_NAME,
						new JsonPrimitive(s1), PrePad.PARAMETER_PREPAD_STRING_NAME, new JsonPrimitive(s2),

						PrePad.PARAMETER_LENGTH_NAME, new JsonPrimitive(11)))).allResults().get(0).getResult()
						.get(PrePad.EVENT_RESULT_NAME));
	}

	@Test
	void test3() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = "hiran";

		PrePad prepad = new PrePad();

		assertEquals(new JsonPrimitive("hira THIScompatY IS A NOcoDE plATFNORM"),
				prepad.execute(new FunctionExecutionParameters().setArguments(Map.of(PrePad.PARAMETER_STRING_NAME,
						new JsonPrimitive(s1), PrePad.PARAMETER_PREPAD_STRING_NAME, new JsonPrimitive(s2),

						PrePad.PARAMETER_LENGTH_NAME, new JsonPrimitive(4)))).allResults().get(0).getResult()
						.get(PrePad.EVENT_RESULT_NAME));
	}
}

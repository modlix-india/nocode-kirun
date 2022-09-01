package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class PostPadTest {

	@Test
	void test() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = "hiran";

		PostPad postpad = new PostPad();

		assertEquals(new JsonPrimitive(" THIScompatY IS A NOcoDE plATFNORMhiranhiranhi"),
				postpad.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(PostPad.PARAMETER_STRING_NAME,
						new JsonPrimitive(s1), PostPad.PARAMETER_POSTPAD_STRING_NAME, new JsonPrimitive(s2),

						PostPad.PARAMETER_LENGTH_NAME, new JsonPrimitive(12)))).allResults().get(0).getResult()
						.get(PostPad.EVENT_RESULT_NAME));
	}

	@Test
	void test2() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = " h ";

		PostPad prepad = new PostPad();

		assertEquals(new JsonPrimitive(" THIScompatY IS A NOcoDE plATFNORM h  h  h  h"),
				prepad.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(PostPad.PARAMETER_STRING_NAME,
						new JsonPrimitive(s1), PostPad.PARAMETER_POSTPAD_STRING_NAME, new JsonPrimitive(s2),

						PostPad.PARAMETER_LENGTH_NAME, new JsonPrimitive(11)))).allResults().get(0).getResult()
						.get(PostPad.EVENT_RESULT_NAME));
	}

	@Test
	void tes3() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";
		String s2 = "hiran";

		PostPad postpad = new PostPad();

		assertEquals(new JsonPrimitive(" THIScompatY IS A NOcoDE plATFNORMhir"),
				postpad.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(PostPad.PARAMETER_STRING_NAME,
						new JsonPrimitive(s1), PostPad.PARAMETER_POSTPAD_STRING_NAME, new JsonPrimitive(s2),

						PostPad.PARAMETER_LENGTH_NAME, new JsonPrimitive(3)))).allResults().get(0).getResult()
						.get(PostPad.EVENT_RESULT_NAME));
	}

}

package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class DeleteForGivenLengthTest {

	@Test
	void test() {
		String s1 = " THIScompatY IS A NOcoDE plATFNORM";

		DeleteForGivenLength delete = new DeleteForGivenLength();

		assertEquals(new JsonPrimitive(" THIScompaNOcoDE plATFNORM"), delete
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
						.setArguments(Map.of(DeleteForGivenLength.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								DeleteForGivenLength.PARAMETER_AT_START_NAME, new JsonPrimitive(10),
								DeleteForGivenLength.PARAMETER_AT_END_NAME, new JsonPrimitive(18))))
				.allResults().get(0).getResult().get(DeleteForGivenLength.EVENT_RESULT_NAME));
	}

	@Test
	void test2() {

		String s1 = " THIScompatY IS A NOcoDE plATFNORM";

		DeleteForGivenLength insert = new DeleteForGivenLength();

		assertEquals(new JsonPrimitive(" THItY IS A NOcoDE plATFNORM"), insert
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
						.setArguments(Map.of(DeleteForGivenLength.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								DeleteForGivenLength.PARAMETER_AT_START_NAME, new JsonPrimitive(4),
								DeleteForGivenLength.PARAMETER_AT_END_NAME, new JsonPrimitive(10))))
				.allResults().get(0).getResult().get(DeleteForGivenLength.EVENT_RESULT_NAME));
	}
}

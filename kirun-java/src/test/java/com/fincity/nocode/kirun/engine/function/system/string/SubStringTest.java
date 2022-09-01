package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class SubStringTest {

	@Test
	void test() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		String s1 = " THIS IS A NOcoDE plATFNORM";
		String s2 = " fincitY compatY ";

		assertEquals(new JsonPrimitive("S IS A NOcoDE "),
				stringFunction.find(Namespaces.STRING, "SubString")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
								AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
								AbstractTertiaryStringFunction.PARAMETER_INDEX_NAME, new JsonPrimitive(4),
								AbstractTertiaryStringFunction.PARAMETER_SECOND_INDEX_NAME, new JsonPrimitive(18))))
						.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive("incitY"),
				stringFunction.find(Namespaces.STRING, "SubString")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
								AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s2),
								AbstractTertiaryStringFunction.PARAMETER_INDEX_NAME, new JsonPrimitive(2),
								AbstractTertiaryStringFunction.PARAMETER_SECOND_INDEX_NAME, new JsonPrimitive(8))))
						.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME));

//		assertThrows(SchemaValidationException.class, () -> {
//			stringFunction.find(Namespaces.STRING, "SubString")
//					.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("value1", new JsonPrimitive(s2),
//							"value2", new JsonPrimitive(-1), "value3", new JsonPrimitive(8))))
//					.allResults().get(0).getResult().get("value");
//		});

	}

}

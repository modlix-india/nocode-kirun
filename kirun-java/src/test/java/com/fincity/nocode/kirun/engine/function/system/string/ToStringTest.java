package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

class ToStringTest {

	@Test
	void test() {
		ToString stringFunction = new ToString();

		assertEquals(new JsonPrimitive("123124"), stringFunction
		        .execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
		                .setArguments(Map.of(ToString.PARAMETER_INPUT_ANYTYPE_NAME, new JsonPrimitive(123124))))
		        .allResults()
		        .get(0)
		        .getResult()
		        .get(ToString.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive("true"), stringFunction
		        .execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
		                .setArguments(Map.of(ToString.PARAMETER_INPUT_ANYTYPE_NAME, new JsonPrimitive(true))))
		        .allResults()
		        .get(0)
		        .getResult()
		        .get(ToString.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive("null"), stringFunction
		        .execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
		                .setArguments(Map.of(ToString.PARAMETER_INPUT_ANYTYPE_NAME, JsonNull.INSTANCE)))
		        .allResults()
		        .get(0)
		        .getResult()
		        .get(ToString.EVENT_RESULT_NAME));

		JsonArray ja = new JsonArray();
		ja.add(Boolean.TRUE);
		ja.add(55);
		ja.add("Kiran");
		JsonArray jb = new JsonArray();
		jb.add("Kumar");
		jb.add(Boolean.FALSE);
		ja.add(jb);

		assertEquals(new JsonPrimitive("[\n  true,\n  55,\n  \"Kiran\",\n  [\n    \"Kumar\",\n    false\n  ]\n]"), stringFunction
		        .execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
		                .setArguments(Map.of(ToString.PARAMETER_INPUT_ANYTYPE_NAME, ja)))
		        .allResults()
		        .get(0)
		        .getResult()
		        .get(ToString.EVENT_RESULT_NAME));
	}

	void test2() {

	}

}

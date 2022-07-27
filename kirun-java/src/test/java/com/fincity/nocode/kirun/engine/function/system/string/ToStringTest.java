package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

class ToStringTest {

	@Test
	void test() {
		ToString stringFunction = new ToString();

		assertEquals(new JsonPrimitive("123124"),
				stringFunction
						.execute(new FunctionExecutionParameters()
								.setArguments(Map.of(ToString.PARAMETER_INPUT_ANYTYPE_NAME, new JsonPrimitive(123124))))
						.allResults().get(0).getResult().get(ToString.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive("true"),
				stringFunction
						.execute(new FunctionExecutionParameters()
								.setArguments(Map.of(ToString.PARAMETER_INPUT_ANYTYPE_NAME, new JsonPrimitive(true))))
						.allResults().get(0).getResult().get(ToString.EVENT_RESULT_NAME));

		Assertions.assertThrows(SchemaValidationException.class,
				() -> stringFunction
						.execute(new FunctionExecutionParameters()
								.setArguments(Map.of(ToString.PARAMETER_INPUT_ANYTYPE_NAME, JsonNull.INSTANCE)))
						.allResults().get(0).getResult().get(ToString.EVENT_RESULT_NAME));
	}

}

package com.fincity.nocode.kirun.engine.function.math;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;
import com.fincity.nocode.kirun.engine.function.system.math.MathFunctionRepository;

class Log10Test {

	@Test
	void test() {
		var log = new MathFunctionRepository().find(Namespaces.MATH, "Log10");

		assertEquals(new JsonPrimitive(2.1583624920952498),
				log.execute(new FunctionExecutionParameters().setArguments(Map.of("value", new JsonPrimitive(144))))
						.next().getResult().get("value"));

		assertEquals(new JsonPrimitive(0.3010299956639812),
				log.execute(new FunctionExecutionParameters().setArguments(Map.of("value", new JsonPrimitive(2))))
						.next().getResult().get("value"));

		assertEquals(new JsonPrimitive(0.0),
				log.execute(new FunctionExecutionParameters().setArguments(Map.of("value", new JsonPrimitive(1))))
						.next().getResult().get("value"));
	}

	@Test
	void test3() {
		var log = new MathFunctionRepository().find(Namespaces.MATH, "Log10");

		var num = Double.POSITIVE_INFINITY - Double.POSITIVE_INFINITY;

		assertEquals(new JsonPrimitive(num),
				log.execute(new FunctionExecutionParameters().setArguments(Map.of("value", new JsonPrimitive(-123))))
						.next().getResult().get("value"));

		assertEquals(new JsonPrimitive(num),
				log.execute(new FunctionExecutionParameters().setArguments(Map.of("value", new JsonPrimitive(num))))
						.next().getResult().get("value"));

		assertEquals(new JsonPrimitive(Double.POSITIVE_INFINITY),
				log.execute(new FunctionExecutionParameters()
						.setArguments(Map.of("value", new JsonPrimitive(Double.POSITIVE_INFINITY)))).next().getResult()
						.get("value"));

		assertEquals(new JsonPrimitive(Double.NEGATIVE_INFINITY),
				log.execute(new FunctionExecutionParameters().setArguments(Map.of("value", new JsonPrimitive(-0.0))))
						.next().getResult().get("value"));

		assertThrows(SchemaValidationException.class,
				() -> log
						.execute(new FunctionExecutionParameters().setArguments(Map.of("value", new JsonPrimitive(""))))
						.next().getResult().get("value"));

		assertEquals(new JsonPrimitive(num),
				log.execute(new FunctionExecutionParameters().setArguments(Map.of("value", new JsonPrimitive(num))))
						.next().getResult().get("value"));

	}

}

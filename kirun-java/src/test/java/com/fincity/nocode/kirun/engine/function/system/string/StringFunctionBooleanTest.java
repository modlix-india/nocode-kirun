package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

public class StringFunctionBooleanTest {

	@Test
	void test() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		assertEquals(new JsonPrimitive(false),
				stringFunction.find(Namespaces.STRING, "IsBlank")
						.execute(new FunctionExecutionParameters().setArguments(
								Map.of("value", new JsonPrimitive("			no code  Kirun  PLATform		"))))
						.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive(true),
				stringFunction.find(Namespaces.STRING, "IsBlank")
						.execute(new FunctionExecutionParameters()
								.setArguments(Map.of("value", new JsonPrimitive("						"))))
						.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive(true),
				stringFunction.find(Namespaces.STRING, "IsBlank")
						.execute(new FunctionExecutionParameters().setArguments(Map.of("value", new JsonPrimitive(""))))
						.allResults().get(0).getResult().get("value"));

	}

	@Test
	void test2() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		assertEquals(new JsonPrimitive(false),
				stringFunction.find(Namespaces.STRING, "IsEmpty")
						.execute(new FunctionExecutionParameters().setArguments(
								Map.of("value", new JsonPrimitive("			no code  Kirun  PLATform		"))))
						.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive(false),
				stringFunction.find(Namespaces.STRING, "IsEmpty")
						.execute(new FunctionExecutionParameters()
								.setArguments(Map.of("value", new JsonPrimitive("						"))))
						.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive(true),
				stringFunction.find(Namespaces.STRING, "IsEmpty")
						.execute(new FunctionExecutionParameters().setArguments(Map.of("value", new JsonPrimitive(""))))
						.allResults().get(0).getResult().get("value"));

	}
}

package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class StringFunctionRepositoryTest {

	@Test
	void test3() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		assertEquals(new JsonPrimitive("no code  Kirun  PLATform"),
				stringFunction.find(Namespaces.STRING, "Trim")
						.execute(new FunctionExecutionParameters().setArguments(
								Map.of("value", new JsonPrimitive("			no code  Kirun  PLATform		"))))
						.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive(""),
				stringFunction.find(Namespaces.STRING, "Trim")
						.execute(new FunctionExecutionParameters()
								.setArguments(Map.of("value", new JsonPrimitive("						"))))
						.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive("{20934 123 1[[23 245-0 34\\\" 3434 \\\" 123]]}"), stringFunction
				.find(Namespaces.STRING, "Trim")
				.execute(new FunctionExecutionParameters().setArguments(
						Map.of("value", new JsonPrimitive("		{20934 123 1[[23 245-0 34\\\" 3434 \\\" 123]]}	"))))
				.allResults().get(0).getResult().get("value"));
	}

	@Test
	void test2() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		assertEquals(new JsonPrimitive(" this is a nocode platform	"),
				stringFunction.find(Namespaces.STRING, "LowerCase")
						.execute(new FunctionExecutionParameters()
								.setArguments(Map.of("value", new JsonPrimitive(" THIS IS A NOcoDE plATFORM	"))))
						.next().getResult().get("value"));

		assertEquals(new JsonPrimitive("				"),
				stringFunction.find(Namespaces.STRING, "LowerCase")
						.execute(new FunctionExecutionParameters()
								.setArguments(Map.of("value", new JsonPrimitive("				"))))
						.next().getResult().get("value"));

		assertEquals(new JsonPrimitive("		20934 123 123 245-0 34\" 3434 \" 123		"), stringFunction
				.find(Namespaces.STRING, "LowerCase")
				.execute(new FunctionExecutionParameters().setArguments(
						Map.of("value", new JsonPrimitive("		20934 123 123 245-0 34\" 3434 \" 123		"))))
				.next().getResult().get("value"));

		assertThrows(SchemaValidationException.class, () -> stringFunction.find(Namespaces.STRING, "LowerCase")
				.execute(new FunctionExecutionParameters().setArguments(Map.of("value", new JsonPrimitive(1231))))
				.next().getResult().get("value"));

	}

	@Test
	void test1() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		assertEquals(new JsonPrimitive(" THIS IS A NOCODE PLATFORM		"),
				stringFunction.find(Namespaces.STRING, "UpperCase")
						.execute(new FunctionExecutionParameters()
								.setArguments(Map.of("value", new JsonPrimitive(" THIS IS A NOcoDE plATFORM		"))))
						.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive("				"),
				stringFunction.find(Namespaces.STRING, "UpperCase")
						.execute(new FunctionExecutionParameters()
								.setArguments(Map.of("value", new JsonPrimitive("				"))))
						.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive("		20934 123 123 245-0 34\" 3434 \" 123		"), stringFunction
				.find(Namespaces.STRING, "UpperCase")
				.execute(new FunctionExecutionParameters().setArguments(
						Map.of("value", new JsonPrimitive("		20934 123 123 245-0 34\" 3434 \" 123		"))))
				.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive("			NO CODE  KIRUN  PLATFORM		"),
				stringFunction.find(Namespaces.STRING, "UpperCase")
						.execute(new FunctionExecutionParameters().setArguments(
								Map.of("value", new JsonPrimitive("			no code  Kirun  PLATform		"))))
						.allResults().get(0).getResult().get("value"));
	}

	@Test
	void test4() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		assertThrows(SchemaValidationException.class, () -> stringFunction.find(Namespaces.STRING, "LowerCase")
				.execute(new FunctionExecutionParameters().setArguments(Map.of("value", new JsonPrimitive(1231))))
				.next().getResult().get("value"));

	}

}

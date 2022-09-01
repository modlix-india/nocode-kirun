package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class StringFunctionRepositoryBinaryTest {

	@Test
	void test3() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		assertEquals(new JsonPrimitive(true), stringFunction.find(Namespaces.STRING, "Contains")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
						AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
						new JsonPrimitive("			no code  Kirun  PLATform		"),
						AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("no code"))))
				.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(false),
				stringFunction.find(Namespaces.STRING, "Contains")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
								AbstractBinaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive("			"),
								AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("  "))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(true),
				stringFunction.find(Namespaces.STRING, "Contains")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("{20934 123 1[[23 245-0 34\\\\\\\" 3434 \\\\\\\" 123]]}"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("4 123 1[[23 245-0 34"))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

	}

	@Test
	void test5() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		assertEquals(new JsonPrimitive(3), stringFunction.find(Namespaces.STRING, "IndexOf")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
						AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
						new JsonPrimitive("			no code  Kirun  PLATform		"),
						AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("no code"))))
				.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(-1),
				stringFunction.find(Namespaces.STRING, "IndexOf")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
								AbstractBinaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive("			"),
								AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("  "))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(5),
				stringFunction.find(Namespaces.STRING, "IndexOf")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("{20934 123 1[[23 245-0 34\" 3434\" 123]]}"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("4 123 1[[23 245-0 34"))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

	}

	@Test
	void test2() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		assertEquals(new JsonPrimitive(20),
				stringFunction.find(Namespaces.STRING, "LastIndexOf")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
								AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
								new JsonPrimitive("			no code  Kirun  PLATform		"),
								AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("LA"))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(5),
				stringFunction.find(Namespaces.STRING, "LastIndexOf")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
								AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
								new JsonPrimitive("this is a no code platform"),
								AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("is"))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(29),
				stringFunction.find(Namespaces.STRING, "LastIndexOf")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
								AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
								new JsonPrimitive("{20934 123 1[[23 245-0 34\" 3434  \" 123]]}"),
								AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("34"))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

	}

	@Test
	void test1() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		assertEquals(new JsonPrimitive(true),
				stringFunction.find(Namespaces.STRING, "EqualsIgnoreCase")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive(" THIS IS A NOcoDE plATFORM		"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive(" THIS IS A NOCODE PLATFORM		"))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(true),
				stringFunction.find(Namespaces.STRING, "EqualsIgnoreCase")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
								AbstractBinaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive("				"),
								AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
								new JsonPrimitive("				"))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(true),
				stringFunction.find(Namespaces.STRING, "EqualsIgnoreCase")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("		20934 123 123 245-0 34\" 3434 \" 123		"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("		20934 123 123 245-0 34\" 3434 \" 123		"))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(false),
				stringFunction.find(Namespaces.STRING, "EqualsIgnoreCase")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("			no code  Kirun  PLATform		"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("			NO CODE  KIRUN  PLATFORM	"))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));
	}

	@Test
	void test4() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		assertEquals(new JsonPrimitive(true),
				stringFunction.find(Namespaces.STRING, "EndsWith")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("			no code  Kirun  PLATform		"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("PLATform		"))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(true), stringFunction.find(Namespaces.STRING, "EndsWith")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
						AbstractBinaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive("this is a new job\t"),
						AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("job\t"))))
				.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(true), stringFunction.find(Namespaces.STRING, "EndsWith")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
						AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
						new JsonPrimitive("{20934 123 1[[23 245-0 34\\\\\\\" 3434 \\\\\\\" 123]]}"),
						AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("\" 123]]}"))))
				.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

	}

	@Test
	void test6() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		assertEquals(new JsonPrimitive(true), stringFunction.find(Namespaces.STRING, "Matches")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
						AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
						new JsonPrimitive("			no code  Kirun  PLATform		"),
						AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("(.*)no(.*)"))))
				.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(true),
				stringFunction.find(Namespaces.STRING, "Matches")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
								AbstractBinaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive("			"),
								AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("(.*)"))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(false), stringFunction.find(Namespaces.STRING, "Contains")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
						AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
						new JsonPrimitive("{20934 123 1[[23 245-0 34\\\\\\\" 3434 \\\\\\\" 123]]}"),
						AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("2093(.*)"))))
				.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

	}

}

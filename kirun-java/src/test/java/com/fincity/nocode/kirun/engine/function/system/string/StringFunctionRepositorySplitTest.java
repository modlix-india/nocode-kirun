package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

class StringFunctionRepositorySplitTest {

	@Test
	void test3() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		assertEquals(new JsonPrimitive(true), stringFunction.find(Namespaces.STRING, "Contains")
				.execute(new FunctionExecutionParameters().setArguments(Map.of(
						AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
						new JsonPrimitive("			no code  Kirun  PLATform		"),
						AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("no code"))))
				.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(false),
				stringFunction.find(Namespaces.STRING, "Contains")
						.execute(new FunctionExecutionParameters().setArguments(Map.of(
								AbstractBinaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive("			"),
								AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("  "))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(true),
				stringFunction.find(Namespaces.STRING, "Contains")
						.execute(new FunctionExecutionParameters()
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
				.execute(new FunctionExecutionParameters().setArguments(Map.of(
						AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
						new JsonPrimitive("			no code  Kirun  PLATform		"),
						AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("no code"))))
				.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(-1),
				stringFunction.find(Namespaces.STRING, "IndexOf")
						.execute(new FunctionExecutionParameters().setArguments(Map.of(
								AbstractBinaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive("			"),
								AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("  "))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(5),
				stringFunction.find(Namespaces.STRING, "IndexOf")
						.execute(new FunctionExecutionParameters()
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
						.execute(new FunctionExecutionParameters().setArguments(Map.of(
								AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
								new JsonPrimitive("			no code  Kirun  PLATform		"),
								AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("LA"))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(5),
				stringFunction.find(Namespaces.STRING, "LastIndexOf")
						.execute(new FunctionExecutionParameters().setArguments(Map.of(
								AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
								new JsonPrimitive("this is a no code platform"),
								AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("is"))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(29),
				stringFunction.find(Namespaces.STRING, "LastIndexOf")
						.execute(new FunctionExecutionParameters().setArguments(Map.of(
								AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
								new JsonPrimitive("{20934 123 1[[23 245-0 34\" 3434  \" 123]]}"),
								AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("34"))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

	}

	// @Test
	void test1() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		assertEquals(new JsonPrimitive(true),
				stringFunction.find(Namespaces.STRING, "EqualsIgnoreCase")
						.execute(new FunctionExecutionParameters()
								.setArguments(Map.of("value1", new JsonPrimitive(" THIS IS A NOcoDE plATFORM		"),
										"value2", new JsonPrimitive(" THIS IS A NOCODE PLATFORM		"))))
						.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive(true),
				stringFunction.find(Namespaces.STRING, "EqualsIgnoreCase")
						.execute(new FunctionExecutionParameters().setArguments(Map.of("value1",
								new JsonPrimitive("				"), "value2", new JsonPrimitive("				"))))
						.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive(true),
				stringFunction.find(Namespaces.STRING, "EqualsIgnoreCase")
						.execute(new FunctionExecutionParameters().setArguments(Map.of("value1",
								new JsonPrimitive("		20934 123 123 245-0 34\" 3434 \" 123		"), "value2",
								new JsonPrimitive("		20934 123 123 245-0 34\" 3434 \" 123		"))))
						.allResults().get(0).getResult().get("value"));

		assertEquals(new JsonPrimitive(false), stringFunction.find(Namespaces.STRING, "EqualsIgnoreCase")
				.execute(new FunctionExecutionParameters()
						.setArguments(Map.of("value1", new JsonPrimitive("			no code  Kirun  PLATform		"),
								"value2", new JsonPrimitive("			NO CODE  KIRUN  PLATFORM	"))))
				.allResults().get(0).getResult().get("value"));
	}

	@Test
	void test4() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		assertEquals(new JsonPrimitive(" NoCode PlatForm  NoCode PlatForm  NoCode PlatForm "), stringFunction
				.find(Namespaces.STRING, "Repeat")
				.execute(new FunctionExecutionParameters().setArguments(Map.of(
						AbstractBinaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(" NoCode PlatForm "),
						AbstractBinaryStringFunction.PARAMETER_INDEX_NAME, new JsonPrimitive(3))))
				.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(
				" fincity company  fincity company  fincity company  fincity company  fincity company  fincity company "),
				stringFunction.find(Namespaces.STRING, "Repeat")
						.execute(new FunctionExecutionParameters()
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive(" fincity company "),
										AbstractBinaryStringFunction.PARAMETER_INDEX_NAME, new JsonPrimitive(6))))
						.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));
	}

	@Test
	void test6() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		var array = new JsonArray();
		array.add("I");
		array.add("am");
		array.add("using");
		array.add("eclipse");
		array.add("to");
		array.add("test");
		array.add("the");
		array.add("changes");
		array.add("with");
		array.add("test");
		array.add("Driven");
		array.add("developement");
		assertEquals(array, stringFunction.find(Namespaces.STRING, "Split").execute(new FunctionExecutionParameters()
				.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
						new JsonPrimitive("I am using eclipse to test the changes with test Driven developement"),
						AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive(" "))))
				.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

	}

	@Test
	void test7() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		var array = new JsonArray();
		array.add("I am using ");
		array.add("clips");
		array.add(" to t");
		array.add("st th");
		array.add(" chang");
		array.add("s with t");
		array.add("st Driv");
		array.add("n d");
		array.add("v");
		array.add("lop");
		array.add("m");
		array.add("nt");
		assertEquals(array, stringFunction.find(Namespaces.STRING, "Split").execute(new FunctionExecutionParameters()
				.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
						new JsonPrimitive("I am using eclipse to test the changes with test Driven developement"),
						AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("e"))))
				.allResults().get(0).getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME));

	}

}

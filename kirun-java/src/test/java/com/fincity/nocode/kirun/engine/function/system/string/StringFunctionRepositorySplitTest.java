package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class StringFunctionRepositorySplitTest {

	@Test
	void test3() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "Contains")
				.flatMap(
						fun -> fun.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(
										AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("			no code  Kirun  PLATform		"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("no code")))))
				.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME).getAsBoolean()))
				.expectNext(true)
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "Contains")
				.flatMap(
						fun -> fun.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(
										AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("			"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("  ")))))
				.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME).getAsBoolean()))
				.expectNext(false)
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "Contains")
				.flatMap(
						fun -> fun.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(
										AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("{20934 123 1[[23 245-0 34\" 3434\" 123]]}"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("4 123 1[[23 245-0 34")))))
				.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME).getAsBoolean()))
				.expectNext(true)
				.verifyComplete();
	}

	@Test
	void test5() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "IndexOf")
				.flatMap(
						fun -> fun.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(
										AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("			no code  Kirun  PLATform		"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("no code")))))
				.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME).getAsInt()))
				.expectNext(3)
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "IndexOf")
				.flatMap(
						fun -> fun.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(
										AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("			"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("  ")))))
				.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME).getAsInt()))
				.expectNext(-1)
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "IndexOf")
				.flatMap(
						fun -> fun.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(
										AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("{20934 123 1[[23 245-0 34\" 3434\" 123]]}"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("4 123 1[[23 245-0 34")))))
				.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME).getAsInt()))
				.expectNext(5)
				.verifyComplete();

	}

	@Test
	void test2() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "LastIndexOf")
				.flatMap(
						fun -> fun.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(
										AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("			no code  Kirun  PLATform		"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("LA")))))
				.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME).getAsInt()))
				.expectNext(20)
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "LastIndexOf")
				.flatMap(
						fun -> fun.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(
										AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("this is a no code platform"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("is")))))
				.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME).getAsInt()))
				.expectNext(5)
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "LastIndexOf")
				.flatMap(
						fun -> fun.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(
										AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("{20934 123 1[[23 245-0 34\" 3434  \" 123]]}"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("34")))))
				.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME).getAsInt()))
				.expectNext(29)
				.verifyComplete();
	}

	@Test
	void test1() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "EqualsIgnoreCase")
				.flatMap(
						fun -> fun.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive(" THIS IS A NOcoDE plATFORM		"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive(" THIS IS A NOCODE PLATFORM		"))))))
				.expectNextMatches(fo -> fo.next().getResult().get("result").getAsBoolean())
				.verifyComplete();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "EqualsIgnoreCase")
						.flatMap(fun -> fun.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("				"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("				"))))))
				.expectNextMatches(fo -> fo.next().getResult().get("result").getAsBoolean())
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "EqualsIgnoreCase")
				.flatMap(
						fun -> fun.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("			no code  Kirun  PLATform		"),
										AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("			no code  Kirun  PLATform		"))))))
				.expectNextMatches(fo -> fo.next().getResult().get("result").getAsBoolean())
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "EqualsIgnoreCase")
				.flatMap(
						fun -> fun.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("		20934 123 123 245-0 34\" 3434 \" 123		"),
										AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("		20934 123 123 245-0 34\" 3434 \" 123		"))))))
				.expectNextMatches(fo -> fo.next().getResult().get("result").getAsBoolean())
				.verifyComplete();
	}

	@Test
	void test4() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "Repeat")
				.flatMap(
						fun -> fun.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive(" NoCode PlatForm "),
										AbstractBinaryStringFunction.PARAMETER_INDEX_NAME, new JsonPrimitive(3))))))
				.expectNextMatches(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME)
						.getAsString()
						.equals(" NoCode PlatForm  NoCode PlatForm  NoCode PlatForm "))
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "Repeat")
				.flatMap(
						fun -> fun.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive(" fincity company "),
										AbstractBinaryStringFunction.PARAMETER_INDEX_NAME, new JsonPrimitive(6))))))
				.expectNextMatches(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME)
						.getAsString()
						.equals(" fincity company  fincity company  fincity company  fincity company  fincity company  fincity company "))
				.verifyComplete();
	}

	@Test
	void test6() {

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
		var x = (new Split()).execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
						new JsonPrimitive("I am using eclipse to test the changes with test Driven developement"),
						AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive(" "))));

		StepVerifier.create(x)
				.expectNextMatches(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME)
						.equals(array))
				.verifyComplete();

	}

	@Test
	void test7() {

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

		StepVerifier.create((new Split())
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
								new JsonPrimitive(
										"I am using eclipse to test the changes with test Driven developement"),
								AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME, new JsonPrimitive("e")))))
				.expectNextMatches(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME)
						.equals(array))
				.verifyComplete();
	}

}

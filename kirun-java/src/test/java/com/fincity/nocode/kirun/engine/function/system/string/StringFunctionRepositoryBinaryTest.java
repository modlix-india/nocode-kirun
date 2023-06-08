package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class StringFunctionRepositoryBinaryTest {

	@Test
	void test3() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "Contains").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository()).setArguments(
										Map.of(
												AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
												new JsonPrimitive("			no code  Kirun  PLATform		"),
												AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
												new JsonPrimitive("no code"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME)
								.getAsBoolean())))
				.expectNext(true)
				.verifyComplete();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "Contains").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository()).setArguments(
										Map.of(
												AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
												new JsonPrimitive("			"),
												AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
												new JsonPrimitive("  "))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME)
								.getAsBoolean())))
				.expectNext(false)
				.verifyComplete();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "Contains").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository()).setArguments(
										Map.of(
												AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
												new JsonPrimitive(
														"{20934 123 1[[23 245-0 34\\\\\\\" 3434 \\\\\\\" 123]]}"),
												AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
												new JsonPrimitive("4 123 1[[23 245-0 34"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME)
								.getAsBoolean())))
				.expectNext(true)
				.verifyComplete();

	}

	@Test
	void test5() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "IndexOf").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository()).setArguments(
										Map.of(
												AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
												new JsonPrimitive("			no code  Kirun  PLATform		"),
												AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
												new JsonPrimitive("no code"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME)
								.getAsNumber())))
				.expectNext(3)
				.verifyComplete();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "IndexOf").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository()).setArguments(
										Map.of(
												AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
												new JsonPrimitive("			"),
												AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
												new JsonPrimitive("  "))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME)
								.getAsNumber())))
				.expectNext(-1)
				.verifyComplete();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "IndexOf").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository()).setArguments(
										Map.of(
												AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
												new JsonPrimitive(
														"{20934 123 1[[23 245-0 34\" 3434\" 123]]}"),
												AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
												new JsonPrimitive("4 123 1[[23 245-0 34"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME)
								.getAsNumber())))
				.expectNext(5)
				.verifyComplete();
	}

	@Test
	void test2() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "LastIndexOf").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository()).setArguments(
										Map.of(
												AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
												new JsonPrimitive("			no code  Kirun  PLATform		"),
												AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
												new JsonPrimitive("LA"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME)
								.getAsNumber())))
				.expectNext(20)
				.verifyComplete();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "LastIndexOf").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository()).setArguments(
										Map.of(
												AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
												new JsonPrimitive("this is a no code platform"),
												AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
												new JsonPrimitive("is"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME)
								.getAsNumber())))
				.expectNext(5)
				.verifyComplete();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "LastIndexOf").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository()).setArguments(
										Map.of(
												AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
												new JsonPrimitive("{20934 123 1[[23 245-0 34\" 3434  \" 123]]}"),
												AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
												new JsonPrimitive("34"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME)
								.getAsNumber())))
				.expectNext(29)
				.verifyComplete();
	}

	@Test
	void test1() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "EqualsIgnoreCase").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive(" THIS IS A NOcoDE plATFORM		"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive(" THIS IS A NOCODE PLATFORM		"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME))))
				.expectNext(new JsonPrimitive(true))
				.verifyComplete();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "EqualsIgnoreCase").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("				"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("				"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME))))
				.expectNext(new JsonPrimitive(true))
				.verifyComplete();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "EqualsIgnoreCase").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("		20934 123 123 245-0 34\" 3434 \" 123		"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("		20934 123 123 245-0 34\" 3434 \" 123		"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME))))
				.expectNext(new JsonPrimitive(true))
				.verifyComplete();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "EqualsIgnoreCase").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive("			no code  Kirun  PLATform		"),
										AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
										new JsonPrimitive("			NO CODE  KIRUN  PLATFORM	"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME))))
				.expectNext(new JsonPrimitive(false))
				.verifyComplete();
	}

	@Test
	void test4() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "EndsWith").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository()).setArguments(
										Map.of(
												AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
												new JsonPrimitive("			no code  Kirun  PLATform		"),
												AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
												new JsonPrimitive("PLATform		"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME))))
				.expectNext(new JsonPrimitive(true))
				.verifyComplete();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "EndsWith").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository()).setArguments(
										Map.of(
												AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
												new JsonPrimitive("this is a new job\t"),
												AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
												new JsonPrimitive("job\t"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME))))
				.expectNext(new JsonPrimitive(true))
				.verifyComplete();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "EndsWith").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository()).setArguments(
										Map.of(
												AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
												new JsonPrimitive(
														"{20934 123 1[[23 245-0 34\\\\\\\" 3434 \\\\\\\" 123]]}"),
												AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
												new JsonPrimitive("\" 123]]}"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME))))
				.expectNext(new JsonPrimitive(true))
				.verifyComplete();
	}

	@Test
	void test6() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "Matches").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository()).setArguments(
										Map.of(
												AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
												new JsonPrimitive("			no code  Kirun  PLATform		"),
												AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
												new JsonPrimitive("(.*)no(.*)"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME))))
				.expectNext(new JsonPrimitive(true))
				.verifyComplete();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "Matches").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository()).setArguments(
										Map.of(
												AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
												new JsonPrimitive("			"),
												AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
												new JsonPrimitive("(.*)"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME))))
				.expectNext(new JsonPrimitive(true))
				.verifyComplete();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "Matches").flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository()).setArguments(
										Map.of(
												AbstractBinaryStringFunction.PARAMETER_STRING_NAME,
												new JsonPrimitive(
														"{20934 123 1[[23 245-0 34\\\\\\\" 3434 \\\\\\\" 123]]}"),
												AbstractBinaryStringFunction.PARAMETER_SEARCH_STRING_NAME,
												new JsonPrimitive("2093(.*)"))))
						.map(fo -> fo.next().getResult().get(AbstractBinaryStringFunction.EVENT_RESULT_NAME))))
				.expectNext(new JsonPrimitive(false))
				.verifyComplete();

	}

}

package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class StringFunctionRepositoryTest {

	@Test
	void test3() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "Trim")
				.flatMap(fun -> fun.execute(new ReactiveFunctionExecutionParameters(
						new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository()).setArguments(
								Map.of("value", new JsonPrimitive("			no code  Kirun  PLATform		")))))
				.map(fo -> fo.next().getResult().get("value").getAsString()))
				.expectNext("no code  Kirun  PLATform")
				.verifyComplete();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "Trim")
						.flatMap(fun -> fun
								.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
										new KIRunReactiveSchemaRepository())
										.setArguments(Map.of("value", new JsonPrimitive("						")))))
						.map(fo -> fo.next().getResult().get("value").getAsString()))
				.expectNext("")
				.verifyComplete();

		StepVerifier
				.create(stringFunction.find(Namespaces.STRING, "Trim")
						.flatMap(fun -> fun.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value",
										new JsonPrimitive("		{20934 123 1[[23 245-0 34\\\" 3434 \\\" 123]]}	"))))
								.map(fo -> fo.next().getResult().get("value").getAsString())))
				.expectNext("{20934 123 1[[23 245-0 34\\\" 3434 \\\" 123]]}")
				.verifyComplete();
	}

	@Test
	void test2() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "LowerCase")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(" THIS IS A NOcoDE plATFORM	")))))
				.map(fo -> fo.next().getResult().get("value").getAsString()))
				.expectNext(" this is a nocode platform	")
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "LowerCase")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive("				")))))
				.map(fo -> fo.next().getResult().get("value").getAsString()))
				.expectNext("				")
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "LowerCase")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value",
										new JsonPrimitive("		20934 123 123 245-0 34\" 3434 \" 123		")))))
				.map(fo -> fo.next().getResult().get("value").getAsString()))
				.expectNext("		20934 123 123 245-0 34\" 3434 \" 123		")
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "LowerCase")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(1231)))))
				.map(fo -> fo.next().getResult().get("value").getAsString()))
				.verifyError(KIRuntimeException.class);
	}

	@Test
	void test1() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "UpperCase")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(" THIS IS A NOcoDE plATFORM		")))))
				.map(fo -> fo.next().getResult().get("value").getAsString()))
				.expectNext(" THIS IS A NOCODE PLATFORM		")
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "UpperCase")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive("				")))))
				.map(fo -> fo.next().getResult().get("value").getAsString()))
				.expectNext("				")
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "UpperCase")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value",
										new JsonPrimitive("		20934 123 123 245-0 34\" 3434 \" 123		")))))
				.map(fo -> fo.next().getResult().get("value").getAsString()))
				.expectNext("		20934 123 123 245-0 34\" 3434 \" 123		")
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "UpperCase")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value",
										new JsonPrimitive("			no code  Kirun  PLATform		")))))
				.map(fo -> fo.next().getResult().get("value").getAsString()))
				.expectNext("			NO CODE  KIRUN  PLATFORM		")
				.verifyComplete();
	}

	@Test
	void test4() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "LowerCase")
				.flatMap(fun -> fun
						.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
								new KIRunReactiveSchemaRepository())
								.setArguments(Map.of("value", new JsonPrimitive(1231)))
								.setArguments(Map.of("value", new JsonPrimitive(1231))))))
				.verifyError(KIRuntimeException.class);
	}

}

package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class AbstractTertiaryStringFunctionTest {

	@Test
	void test() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "IndexOfWithStartPoint")
				.flatMap(sf -> sf.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(
										AbstractTertiaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive(" THIS IS A NOcoDE plATFNORM		"),
										AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME,
										new JsonPrimitive("NO"),
										AbstractTertiaryStringFunction.PARAMETER_INDEX_NAME, new JsonPrimitive(10)))))
				.map(r -> r.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME)))
				.expectNext(new JsonPrimitive(11)).verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "IndexOfWithStartPoint")
				.flatMap(sf -> sf.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(
										AbstractTertiaryStringFunction.PARAMETER_STRING_NAME,
										new JsonPrimitive(" fincity compatY "),
										AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME,
										new JsonPrimitive("t"),
										AbstractTertiaryStringFunction.PARAMETER_INDEX_NAME, new JsonPrimitive(9)))))
				.map(r -> r.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME)))
				.expectNext(new JsonPrimitive(14)).verifyComplete();

	}

	@Test
	void test2() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		String s1 = " THIS IS A NOcoDE plATFNORM		";
		String s2 = " fincity compatY ";

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "LastIndexOfWithStartPoint")
				.flatMap(sf -> sf.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(
										AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
										AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME,
										new JsonPrimitive("IS"),
										AbstractTertiaryStringFunction.PARAMETER_INDEX_NAME,
										new JsonPrimitive(s1.length() - 2)))))
				.map(r -> r.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME)))
				.expectNext(new JsonPrimitive(6)).verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "LastIndexOfWithStartPoint")
				.flatMap(sf -> sf.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(
										AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s2),
										AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME,
										new JsonPrimitive("tY"),
										AbstractTertiaryStringFunction.PARAMETER_INDEX_NAME,
										new JsonPrimitive(s2.length() - 1)))))
				.map(r -> r.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME)))
				.expectNext(new JsonPrimitive(14)).verifyComplete();
	}

	@Test
	void test3() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		String s1 = " THIS IS A NOcoDE plATFNORM";
		String s2 = " fincitY compatY ";

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "ReplaceFirst")
				.flatMap(sf -> sf.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(
										AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
										AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME,
										new JsonPrimitive("NO"),
										AbstractTertiaryStringFunction.PARAMETER_THIRD_STRING_NAME,
										new JsonPrimitive(" REPLACED ")))))
				.map(r -> r.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME)))
				.expectNext(new JsonPrimitive(" THIS IS A  REPLACED coDE plATFNORM")).verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "ReplaceFirst")
				.flatMap(sf -> sf.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(
										AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s2),
										AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME,
										new JsonPrimitive("tY"),
										AbstractTertiaryStringFunction.PARAMETER_THIRD_STRING_NAME,
										new JsonPrimitive("new char")))))
				.map(r -> r.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME)))
				.expectNext(new JsonPrimitive(" fincinew char compatY ")).verifyComplete();

	}

	@Test
	void test4() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		String s1 = " THIS IS A NOcoDE plATFNORM";
		String s2 = " fincitY compatY ";

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "Replace")
				.flatMap(sf -> sf.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(
										AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
										AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME,
										new JsonPrimitive("NO"),
										AbstractTertiaryStringFunction.PARAMETER_THIRD_STRING_NAME,
										new JsonPrimitive(" REPLACED "))))
						.map(r -> r.allResults().get(0).getResult()
								.get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME))))
				.expectNext(new JsonPrimitive(" THIS IS A  REPLACED coDE plATF REPLACED RM")).verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "Replace")
				.flatMap(sf -> sf.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(
								Map.of(
										AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s2),
										AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME,
										new JsonPrimitive("tY"),
										AbstractTertiaryStringFunction.PARAMETER_THIRD_STRING_NAME,
										new JsonPrimitive("thankYou")))))
				.map(r -> r.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME)))
				.expectNext(new JsonPrimitive(" fincithankYou compathankYou ")).verifyComplete();

	}
}

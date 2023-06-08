package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class SubStringTest {

	@Test
	void test() {
		StringFunctionRepository stringFunction = new StringFunctionRepository();

		String s1 = " THIS IS A NOcoDE plATFNORM";
		String s2 = " fincitY compatY ";

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "SubString")
				.flatMap(function -> function
						.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(
										AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
										AbstractTertiaryStringFunction.PARAMETER_INDEX_NAME, new JsonPrimitive(4),
										AbstractTertiaryStringFunction.PARAMETER_SECOND_INDEX_NAME,
										new JsonPrimitive(18))))))
				.expectNextMatches(result -> result.next().getResult()
						.get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME).getAsString().equals("S IS A NOcoDE "))
				.verifyComplete();

		StepVerifier.create(stringFunction.find(Namespaces.STRING, "SubString")
				.flatMap(function -> function
						.execute(new ReactiveFunctionExecutionParameters(
								new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
								.setArguments(Map.of(
										AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s2),
										AbstractTertiaryStringFunction.PARAMETER_INDEX_NAME, new JsonPrimitive(2),
										AbstractTertiaryStringFunction.PARAMETER_SECOND_INDEX_NAME,
										new JsonPrimitive(8))))))
				.expectNextMatches(result -> result.next().getResult()
						.get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME).getAsString().equals("incitY"))
				.verifyComplete();
	}

}

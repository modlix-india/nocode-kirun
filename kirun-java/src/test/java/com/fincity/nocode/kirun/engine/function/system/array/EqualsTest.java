package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class EqualsTest {

	@Test
	void test() {

		Equals equals = new Equals();

		JsonArray srcArray = new JsonArray();
		srcArray.add(30);
		srcArray.add(31);
		srcArray.add(32);
		srcArray.add(33);
		srcArray.add(34);

		JsonArray findArray = new JsonArray();

		findArray.add(30);
		findArray.add(31);
		findArray.add(32);
		findArray.add(33);
		findArray.add(34);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());
		fep.setArguments(Map.of(Equals.PARAMETER_ARRAY_SOURCE.getParameterName(), srcArray,
				Equals.PARAMETER_ARRAY_FIND.getParameterName(), findArray));

		StepVerifier.create(equals.execute(fep))
				.expectNextMatches(fo1 -> fo1.allResults()
						.get(0)
						.getResult()
						.get(Equals.EVENT_RESULT_NAME)
						.getAsBoolean())
				.verifyComplete();

		findArray.set(1, new JsonPrimitive(41));

		StepVerifier.create(equals.execute(fep))
				.expectNextMatches(fo1 -> !fo1.allResults()
						.get(0)
						.getResult()
						.get(Equals.EVENT_RESULT_NAME)
						.getAsBoolean())
				.verifyComplete();

		fep = new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository());
		fep.setArguments(Map.of(Equals.PARAMETER_ARRAY_SOURCE.getParameterName(), srcArray,
				Equals.PARAMETER_ARRAY_FIND.getParameterName(), findArray,
				Equals.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(2),
				Equals.PARAMETER_INT_FIND_FROM.getParameterName(), new JsonPrimitive(2)));

		StepVerifier.create(equals.execute(fep))
				.expectNextMatches(fo1 -> fo1.allResults()
						.get(0)
						.getResult()
						.get(Equals.EVENT_RESULT_NAME)
						.getAsBoolean())
				.verifyComplete();

		srcArray = new JsonArray();
		srcArray.add(true);
		srcArray.add(true);
		srcArray.add(false);

		findArray = new JsonArray();
		findArray.add(true);
		findArray.add(true);
		findArray.add(false);

		fep = new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository());
		fep.setArguments(Map.of(Equals.PARAMETER_ARRAY_SOURCE.getParameterName(), srcArray,
				Equals.PARAMETER_ARRAY_FIND.getParameterName(), findArray));

		StepVerifier.create(equals.execute(fep))
				.expectNextMatches(fo1 -> fo1.allResults()
						.get(0)
						.getResult()
						.get(Equals.EVENT_RESULT_NAME)
						.getAsBoolean())
				.verifyComplete();

	}

}

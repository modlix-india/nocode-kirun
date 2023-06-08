package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class CopyTest {

	@Test
	void test() {
		Copy copy = new Copy();

		JsonArray source = new JsonArray();

		source.add(1);
		source.add(2);
		source.add(3);
		source.add(4);
		source.add(5);

		final ReactiveFunctionExecutionParameters fep1 = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Copy.PARAMETER_ARRAY_SOURCE.getParameterName(), source,
						Copy.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(2),
						Copy.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(4)));

		StepVerifier.create(copy.execute(fep1))
				.expectError(KIRuntimeException.class)
				.verify();

		source.add(6);

		JsonArray result = new JsonArray();
		result.add(3);
		result.add(4);
		result.add(5);
		result.add(6);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Copy.PARAMETER_ARRAY_SOURCE.getParameterName(), source,
						Copy.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(2),
						Copy.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(4)));

		final JsonArray finResult = result;
		StepVerifier.create(copy.execute(fep))
				.expectNextMatches(fo -> {
					return finResult.equals(fo.allResults()
							.get(0)
							.getResult()
							.get(Copy.EVENT_RESULT_NAME));
				}).verifyComplete();

		source = new JsonArray();

		JsonObject obj = new JsonObject();
		obj.addProperty("name", "Kiran");
		source.add(obj);

		obj = new JsonObject();
		obj.addProperty("name", "Kumar");
		source.add(obj);

		fep = new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Copy.PARAMETER_ARRAY_SOURCE.getParameterName(),
						source, Copy.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(2),
						Copy.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(4)));

		result = new JsonArray();

		obj = new JsonObject();
		obj.addProperty("name", "Kiran");
		result.add(obj);

		obj = new JsonObject();
		obj.addProperty("name", "Kumar");
		result.add(obj);

		fep = new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Copy.PARAMETER_ARRAY_SOURCE.getParameterName(), source));

		final JsonArray finResult1 = result;
		StepVerifier.create(copy.execute(fep))
				.expectNextMatches(fo -> {
					return finResult1.equals(fo.allResults()
							.get(0)
							.getResult()
							.get(Copy.EVENT_RESULT_NAME));
				}).verifyComplete();

		assertNotSame(source.get(0), result.get(0));

		fep = new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Copy.PARAMETER_ARRAY_SOURCE.getParameterName(),
						source, Copy.PARAMETER_BOOLEAN_DEEP_COPY.getParameterName(), new JsonPrimitive(false)));

		StepVerifier.create(copy.execute(fep))
				.expectNextMatches(fo -> {
					return finResult1.equals(fo.allResults()
							.get(0)
							.getResult()
							.get(Copy.EVENT_RESULT_NAME));
				}).verifyComplete();

		assertEquals(source.get(0), result.get(0));
	}

}

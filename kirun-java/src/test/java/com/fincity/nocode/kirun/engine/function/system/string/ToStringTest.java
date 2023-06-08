package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ToStringTest {

	@Test
	void test() {
		ToString stringFunction = new ToString();

		StepVerifier.create(stringFunction
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(ToString.PARAMETER_INPUT_ANYTYPE_NAME, new JsonPrimitive(123124)))))
				.expectNextMatches(
						fo -> fo.next().getResult().get(ToString.EVENT_RESULT_NAME).getAsString().equals("123124"))
				.verifyComplete();

		StepVerifier.create(stringFunction
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(ToString.PARAMETER_INPUT_ANYTYPE_NAME, new JsonPrimitive(123124.123124)))))
				.expectNextMatches(fo -> fo.next().getResult().get(ToString.EVENT_RESULT_NAME).getAsString()
						.equals("123124.123124"))
				.verifyComplete();

		StepVerifier.create(stringFunction
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(ToString.PARAMETER_INPUT_ANYTYPE_NAME, new JsonPrimitive(true)))))
				.expectNextMatches(
						fo -> fo.next().getResult().get(ToString.EVENT_RESULT_NAME).getAsString().equals("true"))
				.verifyComplete();

		StepVerifier.create(stringFunction
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(ToString.PARAMETER_INPUT_ANYTYPE_NAME, JsonNull.INSTANCE))))
				.expectNextMatches(
						fo -> fo.next().getResult().get(ToString.EVENT_RESULT_NAME).getAsString().equals("null"))
				.verifyComplete();

		JsonArray ja = new JsonArray();
		ja.add(Boolean.TRUE);
		ja.add(55);
		ja.add("Kiran");
		JsonArray jb = new JsonArray();
		jb.add("Kumar");
		jb.add(Boolean.FALSE);
		ja.add(jb);

		StepVerifier.create(stringFunction
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(ToString.PARAMETER_INPUT_ANYTYPE_NAME, ja))))
				.expectNextMatches(fo -> fo.next().getResult().get(ToString.EVENT_RESULT_NAME).getAsString()
						.equals("[\n  true,\n  55,\n  \"Kiran\",\n  [\n    \"Kumar\",\n    false\n  ]\n]"))
				.verifyComplete();
	}
}

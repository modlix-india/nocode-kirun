package com.fincity.nocode.kirun.engine.function.system.math;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;

import reactor.test.StepVerifier;

class HypotenuseTest {

	@Test
	void test() {

		Hypotenuse hyp = new Hypotenuse();
		var nums = new JsonArray();
		nums.add(3);
		nums.add(5);
		nums.add(6);

		StepVerifier
				.create(hyp.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(Map.of("value", nums))))
				.expectNextMatches(r -> r.next().getResult().get("value").getAsDouble() == 8.366600265340756d)
				.verifyComplete();
	}

	@Test
	void test2() {

		Hypotenuse hyp = new Hypotenuse();
		var nums = new JsonArray();

		StepVerifier
				.create(hyp.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(Map.of("value", nums))))
				.expectNextMatches(r -> r.next().getResult().get("value").getAsDouble() == 0d)
				.verifyComplete();
	}

}

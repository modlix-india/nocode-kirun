package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ReverseTest {

	@Test
	void test() {

		Reverse rev = new Reverse();

		StepVerifier.create(rev
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of("value", new JsonPrimitive("This is a no code pl\"atfo\"rm "))))
				.map(fo -> fo.allResults().get(0).getResult().get("value")))
				.expectNext(new JsonPrimitive(" mr\"ofta\"lp edoc on a si sihT"))
				.verifyComplete();
	}

}

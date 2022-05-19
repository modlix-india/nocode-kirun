package com.fincity.nocode.kirun.engine.function.math;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.model.Argument;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class AbsTest {

	private static final String VALUE = "value";

	@Test
	void testExecute() {

		var abs = new Abs();

		StepVerifier.create(abs.execute(List.of(Argument.of(VALUE, new JsonPrimitive(-10)))))
		        .expectNext(EventResult.outputResult(Map.of(VALUE, new JsonPrimitive(10))))
		        .expectComplete()
		        .verify();

		StepVerifier.create(abs.execute(List.of(Argument.of(VALUE, new JsonPrimitive(-10.099)))))
		        .expectNext(EventResult.outputResult(Map.of(VALUE, new JsonPrimitive(10.099))))
		        .expectComplete()
		        .verify();

		StepVerifier.create(abs.execute(List.of(Argument.of(VALUE, new JsonPrimitive(-10l)))))
		        .expectNext(EventResult.outputResult(Map.of(VALUE, new JsonPrimitive(10l))))
		        .expectComplete()
		        .verify();

		StepVerifier.create(abs.execute(List.of(Argument.of(VALUE, new JsonPrimitive(-101.9999d)))))
		        .expectNext(EventResult.outputResult(Map.of(VALUE, new JsonPrimitive(101.9999d))))
		        .expectComplete()
		        .verify();
	}

}

package com.fincity.nocode.kirun.engine.function.system.loop;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class RangeLoopTest {

	@Test
	void test() {

		var loop = new RangeLoop();

		// StepVerifier
		// .create(loop.execute(new ReactiveFunctionExecutionParameters(new
		// KIRunReactiveFunctionRepository(), new
		// KIRunReactiveSchemaRepository()).setArguments(
		// Map.of(RangeLoop.FROM, new JsonPrimitive(2), RangeLoop.TO, new
		// JsonPrimitive(5)))))
		// .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new
		// JsonPrimitive(2))))
		// .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new
		// JsonPrimitive(3))))
		// .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new
		// JsonPrimitive(4))))
		// .expectNext(EventResult.outputOf(Map.of(RangeLoop.VALUE, new
		// JsonPrimitive(5))))
		// .expectComplete()
		// .verify();

		// StepVerifier
		// .create(loop.execute(
		// new ReactiveFunctionExecutionParameters(new
		// KIRunReactiveFunctionRepository(), new
		// KIRunReactiveSchemaRepository()).setArguments(Map.of(RangeLoop.FROM, new
		// JsonPrimitive(201.56),
		// RangeLoop.TO, new JsonPrimitive(201.88), RangeLoop.STEP, new
		// JsonPrimitive(0.08)))))
		// .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new
		// JsonPrimitive(201.56))))
		// .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new
		// JsonPrimitive(201.56 + 0.08))))
		// .expectNext(EventResult.of(Event.ITERATION,
		// Map.of(RangeLoop.INDEX, new JsonPrimitive(201.56 + 0.08 + 0.08))))
		// .expectNext(EventResult.of(Event.ITERATION,
		// Map.of(RangeLoop.INDEX, new JsonPrimitive(201.56 + 0.08 + 0.08 + 0.08))))
		// .expectNext(EventResult.outputOf(Map.of(RangeLoop.VALUE, new
		// JsonPrimitive(201.88))))
		// .expectComplete()
		// .verify();

		var params = new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(RangeLoop.FROM, new JsonPrimitive("2"), RangeLoop.TO, new JsonPrimitive(5)));

		StepVerifier.create(loop.execute(params))
				.expectError(KIRuntimeException.class)
				.verify();
	}
}

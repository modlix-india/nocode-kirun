package com.fincity.nocode.kirun.engine.function.system.loop;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class RangeLoopTest {

	@Test
	void test() {

		var loop = new RangeLoop();

		StepVerifier
		        .create(loop.execute(Map.of(),
		                Map.of(RangeLoop.FROM, new JsonPrimitive(2), RangeLoop.TO, new JsonPrimitive(5))))
		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(2))))
		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(3))))
		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(4))))
		        .expectNext(EventResult.outputOf(Map.of(RangeLoop.VALUE, new JsonPrimitive(5))))
		        .expectComplete()
		        .verify();

		StepVerifier
		        .create(loop.execute(Map.of(),
		                Map.of(RangeLoop.FROM, new JsonPrimitive(201.56), RangeLoop.TO, new JsonPrimitive(201.88),
		                        RangeLoop.STEP, new JsonPrimitive(0.08))))
		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(201.56))))
		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(201.56 + 0.08))))
		        .expectNext(EventResult.of(Event.ITERATION,
		                Map.of(RangeLoop.INDEX, new JsonPrimitive(201.56 + 0.08 + 0.08))))
		        .expectNext(EventResult.of(Event.ITERATION,
		                Map.of(RangeLoop.INDEX, new JsonPrimitive(201.56 + 0.08 + 0.08 + 0.08))))
		        .expectNext(EventResult.outputOf(Map.of(RangeLoop.VALUE, new JsonPrimitive(201.88))))
		        .expectComplete()
		        .verify();

		StepVerifier
		        .create(loop.execute(Map.of(),
		                Map.of(RangeLoop.FROM, new JsonPrimitive("2"), RangeLoop.TO, new JsonPrimitive(5))))
		        .expectErrorMessage("from - Value \"2\" is not of valid type(s)")
		        .verify();

	}
}

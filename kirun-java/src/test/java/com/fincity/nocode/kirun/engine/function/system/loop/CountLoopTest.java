package com.fincity.nocode.kirun.engine.function.system.loop;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class CountLoopTest {

	@Test
	void test() {

		var loop = new CountLoop();

		StepVerifier.create(loop.execute(Map.of(), Map.of()))
		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(1))))
		        .expectNext(EventResult.outputOf(Map.of(RangeLoop.VALUE, new JsonPrimitive(1))))
		        .expectComplete()
		        .verify();

		StepVerifier.create(loop.execute(Map.of(), Map.of(CountLoop.COUNT, new JsonPrimitive(6))))
		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(1))))
		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(2))))
		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(3))))
		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(4))))
		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(5))))
		        .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new JsonPrimitive(6))))
		        .expectNext(EventResult.outputOf(Map.of(RangeLoop.VALUE, new JsonPrimitive(6))))
		        .expectComplete()
		        .verify();
	}

}

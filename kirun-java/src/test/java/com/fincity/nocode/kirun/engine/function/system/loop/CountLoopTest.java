package com.fincity.nocode.kirun.engine.function.system.loop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class CountLoopTest {

	@Test
	void test() {

		var loop = new CountLoop();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository()).setArguments(Map.of());

		StepVerifier.create(loop.execute(fep)).expectError(KIRuntimeException.class).verify();

		ReactiveFunctionExecutionParameters fep1 = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("count", new JsonPrimitive(10)));

		StepVerifier.create(loop.execute(fep1))
				.expectNextMatches(r -> {
					EventResult er1;
					int i1 = 0;
					for (er1 = r.next(); er1.getName().equals("iteration");) {
						assertEquals(i1++, er1.getResult().get("index").getAsInt());
						er1 = r.next();
					}

					assertEquals("output", er1.getName());
					assertEquals(10, er1.getResult().get("value").getAsInt());

					return true;
				})
				.verifyComplete();

		fep1 = new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository()).setArguments(Map.of("count", new JsonPrimitive(0)));

		StepVerifier.create(loop.execute(fep1))
				.expectNextMatches(r -> {
					EventResult er1;
					int i1 = 0;
					for (er1 = r.next(); er1.getName().equals("iteration");) {
						assertEquals(i1++, er1.getResult().get("index").getAsInt());
						er1 = r.next();
					}

					assertEquals("output", er1.getName());
					assertEquals(0, er1.getResult().get("value").getAsInt());

					return true;
				})
				.verifyComplete();

		fep1 = new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository()).setArguments(Map.of("count", new JsonPrimitive(-1)));

		StepVerifier.create(loop.execute(
				fep1))
				.expectNextMatches(r -> {
					EventResult er1;
					int i1 = 0;
					for (er1 = r.next(); er1.getName().equals("iteration");) {
						assertEquals(i1++, er1.getResult().get("index").getAsInt());
						er1 = r.next();
					}

					assertEquals("output", er1.getName());
					assertEquals(-1, er1.getResult().get("value").getAsInt());

					return true;
				})
				.verifyComplete();

		// StepVerifier
		// .create(loop.execute(
		// new ReactiveFunctionExecutionParameters(new
		// KIRunReactiveFunctionRepository(), new
		// KIRunReactiveSchemaRepository()).setArguments(Map.of(CountLoop.COUNT, new
		// JsonPrimitive(6)))))
		// .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new
		// JsonPrimitive(1))))
		// .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new
		// JsonPrimitive(2))))
		// .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new
		// JsonPrimitive(3))))
		// .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new
		// JsonPrimitive(4))))
		// .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new
		// JsonPrimitive(5))))
		// .expectNext(EventResult.of(Event.ITERATION, Map.of(RangeLoop.INDEX, new
		// JsonPrimitive(6))))
		// .expectNext(EventResult.outputOf(Map.of(RangeLoop.VALUE, new
		// JsonPrimitive(6))))
		// .expectComplete()
		// .verify();
	}

}

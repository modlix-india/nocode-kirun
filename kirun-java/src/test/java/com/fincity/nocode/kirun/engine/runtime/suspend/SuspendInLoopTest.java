package com.fincity.nocode.kirun.engine.runtime.suspend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.ParameterReference;
import com.fincity.nocode.kirun.engine.model.Statement;
import com.fincity.nocode.kirun.engine.reactive.ReactiveHybridRepository;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveKIRuntime;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

/**
 * Stopping part-way through a loop body and carrying on from that same iteration.
 *
 * This is the case that forced loop positions out of their closures and into the execution context:
 * a snapshot taken mid-loop has to remember which item was in flight.
 */
class SuspendInLoopTest {

	private final ReactiveRepository<Schema> sRepo = new KIRunReactiveSchemaRepository();

	/**
	 * A ForEachLoop over four names. Each pass records the item, then waits. So the first run
	 * records "a" and stops, and each resume should record exactly one more.
	 */
	private FunctionDefinition loopThatRecordsThenWaits() {

		JsonArray source = new JsonArray();
		source.add("a");
		source.add("b");
		source.add("c");
		source.add("d");

		Statement loop = new Statement("loop").setNamespace("System.Loop")
		        .setName("ForEachLoop")
		        .setParameterMap(Map.of("source", Map.ofEntries(ParameterReference.of(source))));

		Statement record = new Statement("record").setNamespace(RecordingFunction.NAMESPACE)
		        .setName(RecordingFunction.NAME)
		        .setParameterMap(Map.of("value", Map.ofEntries(ParameterReference.of("Steps.loop.iteration.each"))))
		        .setDependentStatements(Map.of("Steps.loop.iteration", true));

		Statement wait = SuspendTestDefinitions.waitUntil("wait", 60_000)
		        .setDependentStatements(Map.of("Steps.record.output", true));

		Statement done = SuspendTestDefinitions.generateOutputValue("done", "finished", "Steps.loop.output");

		return SuspendTestDefinitions.definition("LoopWithWait", loop, record, wait, done);
	}

	@Test
	void stopsInsideTheLoopBodyAndRemembersTheIteration() {

		RecordingFunction recorder = new RecordingFunction();
		ReactiveRepository<ReactiveFunction> fRepo = new ReactiveHybridRepository<>(recorder.asRepository(),
		        new KIRunReactiveFunctionRepository());

		ReactiveFunctionExecutionParameters params = new ReactiveFunctionExecutionParameters(fRepo, sRepo);

		new ReactiveKIRuntime(loopThatRecordsThenWaits()).execute(params)
		        .block();

		SuspendedExecution state = params.getSuspension();

		assertNotNull(state, "stopping inside a loop body should still produce a snapshot");
		assertEquals("wait", state.getSuspendedStepName());
		assertEquals(List.of("a"), recorder.calls(), "only the first item should have been processed");

		// One frame for the function's own graph and one for the loop body it stopped inside.
		assertEquals(2, state.getGraphFrames()
		        .size(), "expected a frame for the top-level graph and one for the loop body");
		assertEquals("loop", state.getGraphFrames()
		        .get(1)
		        .getOwnerStatementName());
		assertEquals(Event.ITERATION, state.getGraphFrames()
		        .get(1)
		        .getEventName());
	}

	@Test
	void resumesTheSameIterationWithoutRedoingEarlierItems() {

		RecordingFunction recorder = new RecordingFunction();
		ReactiveRepository<ReactiveFunction> fRepo = new ReactiveHybridRepository<>(recorder.asRepository(),
		        new KIRunReactiveFunctionRepository());

		ReactiveFunctionExecutionParameters params = new ReactiveFunctionExecutionParameters(fRepo, sRepo);

		new ReactiveKIRuntime(loopThatRecordsThenWaits()).execute(params)
		        .block();

		assertEquals(List.of("a"), recorder.calls());

		SuspendedExecution state = params.getSuspension();

		// Each resume runs one more pass and stops again at the wait.
		for (String expected : List.of("b", "c", "d")) {

			state = resumeOnce(state, fRepo, recorder);

			assertTrue(recorder.calls()
			        .contains(expected), "expected " + expected + " to have been processed, got " + recorder.calls());
		}

		assertEquals(List.of("a", "b", "c", "d"), recorder.calls(),
		        "every item should be processed exactly once across the resumes");
	}

	@Test
	void finishesTheLoopAndCompletesAfterTheLastItem() {

		RecordingFunction recorder = new RecordingFunction();
		ReactiveRepository<ReactiveFunction> fRepo = new ReactiveHybridRepository<>(recorder.asRepository(),
		        new KIRunReactiveFunctionRepository());

		ReactiveFunctionExecutionParameters params = new ReactiveFunctionExecutionParameters(fRepo, sRepo);

		new ReactiveKIRuntime(loopThatRecordsThenWaits()).execute(params)
		        .block();

		SuspendedExecution state = params.getSuspension();
		FunctionOutput output = null;

		// Resume until it stops stopping. Bounded so a bug cannot hang the suite.
		for (int i = 0; i < 10 && state != null; i++) {

			ReactiveFunctionExecutionParameters resumeParams = new ReactiveFunctionExecutionParameters(fRepo, sRepo);
			output = resumeWith(state, fRepo, resumeParams);
			state = resumeParams.getSuspension();
		}

		assertEquals(List.of("a", "b", "c", "d"), recorder.calls());
		assertNotNull(SuspendTestDefinitions.eventNamed(output, Event.OUTPUT),
		        "once the loop is exhausted the function should produce its output");
	}

	private SuspendedExecution resumeOnce(SuspendedExecution state, ReactiveRepository<ReactiveFunction> fRepo,
	        RecordingFunction recorder) {

		ReactiveFunctionExecutionParameters params = new ReactiveFunctionExecutionParameters(fRepo, sRepo);
		resumeWith(state, fRepo, params);
		return params.getSuspension();
	}

	/**
	 * Resumes through a serialisation round trip, so nothing can quietly carry over in memory.
	 */
	private FunctionOutput resumeWith(SuspendedExecution state, ReactiveRepository<ReactiveFunction> fRepo,
	        ReactiveFunctionExecutionParameters params) {

		SuspendedExecution reloaded = SuspendedExecutionSerializer
		        .deserialize(SuspendedExecutionSerializer.serialize(state));

		params.setResumeState(reloaded)
		        .setResumePayload(Map.of("ok", new JsonPrimitive(true)));

		return new ReactiveKIRuntime(loopThatRecordsThenWaits()).execute(params)
		        .block();
	}
}

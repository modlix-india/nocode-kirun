package com.fincity.nocode.kirun.engine.runtime.suspend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import com.google.gson.JsonPrimitive;

/**
 * Stopping inside an If branch.
 *
 * Worth its own test because an If produces a fixed list of events rather than generating them, so
 * rebuilding its branch on resume means winding that list back to where it was - the one case where
 * replaying {@code next()} is both necessary and correct. Get it wrong and the branch either reopens
 * from the start or never releases the steps waiting on the If's output.
 */
class SuspendInIfBranchTest {

	private final ReactiveRepository<Schema> sRepo = new KIRunReactiveSchemaRepository();

	private FunctionDefinition ifBranchWithAWaitInside() {

		Statement check = new Statement("check").setNamespace("System")
		        .setName("If")
		        .setParameterMap(Map.of("condition", Map.ofEntries(ParameterReference.of("1 = 1"))));

		Statement inBranch = new Statement("inBranch").setNamespace(RecordingFunction.NAMESPACE)
		        .setName(RecordingFunction.NAME)
		        .setParameterMap(Map.of("value", Map.ofEntries(ParameterReference.of(new JsonPrimitive("entered")))))
		        .setDependentStatements(Map.of("Steps.check.true", true));

		Statement wait = SuspendTestDefinitions.waitUntil("wait", 60_000)
		        .setDependentStatements(Map.of("Steps.inBranch.output", true));

		Statement afterWait = new Statement("afterWait").setNamespace(RecordingFunction.NAMESPACE)
		        .setName(RecordingFunction.NAME)
		        .setParameterMap(Map.of("value", Map.ofEntries(ParameterReference.of(new JsonPrimitive("resumed")))))
		        .setDependentStatements(Map.of("Steps.wait.output", true));

		Statement done = SuspendTestDefinitions.generateOutputValue("done", "finished", "Steps.check.output");

		return SuspendTestDefinitions.definition("IfWithWaitInside", check, inBranch, wait, afterWait, done);
	}

	@Test
	void stopsInsideTheBranchAndCarriesOnInTheSameBranch() {

		RecordingFunction recorder = new RecordingFunction();
		ReactiveRepository<ReactiveFunction> fRepo = new ReactiveHybridRepository<>(recorder.asRepository(),
		        new KIRunReactiveFunctionRepository());

		ReactiveFunctionExecutionParameters params = new ReactiveFunctionExecutionParameters(fRepo, sRepo);

		new ReactiveKIRuntime(ifBranchWithAWaitInside()).execute(params)
		        .block();

		SuspendedExecution state = params.getSuspension();

		assertNotNull(state);
		assertEquals("wait", state.getSuspendedStepName());
		assertEquals(List.of("entered"), recorder.calls());

		// The frame for the branch names the If and the event that opened it, and records that one
		// event ("true") had already been taken from the If's output.
		assertEquals(2, state.getGraphFrames()
		        .size());
		assertEquals("check", state.getGraphFrames()
		        .get(1)
		        .getOwnerStatementName());
		assertEquals(Event.TRUE, state.getGraphFrames()
		        .get(1)
		        .getEventName());
		assertEquals(1, state.getGraphFrames()
		        .get(1)
		        .getOwnerConsumedEvents());

		FunctionOutput output = new ReactiveKIRuntime(ifBranchWithAWaitInside())
		        .resume(SuspendedExecutionSerializer
		                .deserialize(SuspendedExecutionSerializer.serialize(state)), Map.of(), false, fRepo, sRepo,
		                Map.of())
		        .block();

		assertEquals(List.of("entered", "resumed"), recorder.calls(),
		        "the branch should have carried on, not started again");

		assertNotNull(SuspendTestDefinitions.eventNamed(output, Event.OUTPUT),
		        "the If's output event should have been released once the branch finished");
	}
}

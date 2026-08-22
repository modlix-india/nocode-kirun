package com.fincity.nocode.kirun.engine.runtime.suspend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveKIRuntime;
import com.fincity.nocode.kirun.engine.runtime.suspend.store.InMemoryExecutionStateStore;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

/**
 * End-to-end stop and go: an execution that stops at a wait, is snapshotted, and carries on
 * afterwards.
 */
class SuspendResumeTest {

	private final ReactiveRepository<ReactiveFunction> fRepo = new KIRunReactiveFunctionRepository();
	private final ReactiveRepository<Schema> sRepo = new KIRunReactiveSchemaRepository();

	private ReactiveFunctionExecutionParameters params() {
		return new ReactiveFunctionExecutionParameters(fRepo, sRepo);
	}

	// -------------------------------------------------------------------------------------------
	// Stopping
	// -------------------------------------------------------------------------------------------

	@Test
	void stopsAtATimerWaitAndReportsIt() {

		ReactiveKIRuntime runtime = new ReactiveKIRuntime(SuspendTestDefinitions.threeStepsWithWaitInTheMiddle());
		ReactiveFunctionExecutionParameters params = params();

		FunctionOutput output = runtime.execute(params)
		        .block();

		EventResult suspended = SuspendTestDefinitions.eventNamed(output, Event.SUSPENDED);

		assertNotNull(suspended, "the execution should have raised a suspended event");
		assertEquals("wait", suspended.getResult()
		        .get("stepName")
		        .getAsString());
		assertTrue(suspended.getResult()
		        .containsKey("wakeCondition"));

		SuspendedExecution state = params.getSuspension();

		assertNotNull(state, "a snapshot should have been left on the parameters");
		assertEquals("wait", state.getSuspendedStepName());
		assertTrue(state.getWakeCondition() instanceof WakeCondition.TimerWake);
		assertNull(state.getChild(), "nothing was nested here");

		// The step before the wait ran and its output is in the snapshot; the one after it did not.
		assertTrue(state.getSteps()
		        .containsKey("first"), "the step before the wait should have run");
		assertFalse(state.getSteps()
		        .containsKey("afterWait"), "the step after the wait must not have run");
	}

	@Test
	void doesNotRunStepsThatDependOnTheWait() {

		ReactiveKIRuntime runtime = new ReactiveKIRuntime(SuspendTestDefinitions.threeStepsWithWaitInTheMiddle());

		FunctionOutput output = runtime.execute(params())
		        .block();

		assertNull(SuspendTestDefinitions.eventNamed(output, Event.OUTPUT),
		        "a stopped execution has not produced its output yet");
	}

	// -------------------------------------------------------------------------------------------
	// Resuming
	// -------------------------------------------------------------------------------------------

	@Test
	void resumesInAFreshRuntimeAfterASerializationRoundTrip() {

		ReactiveFunctionExecutionParameters params = params();

		new ReactiveKIRuntime(SuspendTestDefinitions.threeStepsWithWaitInTheMiddle()).execute(params)
		        .block();

		// Round-trip through a store, so nothing can survive in memory between the two halves.
		InMemoryExecutionStateStore store = new InMemoryExecutionStateStore();
		store.save(params.getSuspension())
		        .block();

		SuspendedExecution reloaded = store.load(params.getSuspension()
		        .getExecutionId())
		        .block();

		assertNotNull(reloaded);
		assertEquals("wait", reloaded.getSuspendedStepName());

		// A brand new runtime instance, as a host resuming days later would have.
		FunctionOutput output = new ReactiveKIRuntime(SuspendTestDefinitions.threeStepsWithWaitInTheMiddle())
		        .resume(reloaded, Map.of("resumedWith", new JsonPrimitive("signal-payload")), false, fRepo, sRepo,
		                Map.of())
		        .block();

		EventResult result = SuspendTestDefinitions.eventNamed(output, Event.OUTPUT);

		assertNotNull(result, "the resumed execution should have completed");
		assertEquals("first-then-after", result.getResult()
		        .get("value")
		        .getAsString());
	}

	@Test
	void makesTheResumePayloadAvailableToLaterSteps() {

		ReactiveFunctionExecutionParameters params = params();

		new ReactiveKIRuntime(SuspendTestDefinitions.waitThenEchoThePayload()).execute(params)
		        .block();

		FunctionOutput output = new ReactiveKIRuntime(SuspendTestDefinitions.waitThenEchoThePayload())
		        .resume(params.getSuspension(), Map.of("token", new JsonPrimitive("abc123")), false, fRepo, sRepo,
		                Map.of())
		        .block();

		EventResult result = SuspendTestDefinitions.eventNamed(output, Event.OUTPUT);

		assertNotNull(result);
		assertEquals("abc123", result.getResult()
		        .get("value")
		        .getAsString());
	}

	// -------------------------------------------------------------------------------------------
	// Signals and approvals
	// -------------------------------------------------------------------------------------------

	@Test
	void stopsOnASignalWaitWithItsNameAndDeadline() {

		ReactiveFunctionExecutionParameters params = params();

		new ReactiveKIRuntime(SuspendTestDefinitions.signalWaitWithTimeoutBranch()).execute(params)
		        .block();

		SuspendedExecution state = params.getSuspension();

		assertNotNull(state);
		assertTrue(state.getWakeCondition() instanceof WakeCondition.SignalWake);

		WakeCondition.SignalWake wake = (WakeCondition.SignalWake) state.getWakeCondition();

		assertEquals("manager-approved", wake.signalName());
		assertNotNull(wake.timeoutAt(), "a timeout was configured, so a deadline should be recorded");
		assertTrue(wake.timeoutAt()
		        .isAfter(Instant.now()
		                .minusSeconds(5)));
	}

	@Test
	void takesTheTimeoutBranchWhenResumedAsTimedOut() {

		ReactiveFunctionExecutionParameters params = params();

		new ReactiveKIRuntime(SuspendTestDefinitions.signalWaitWithTimeoutBranch()).execute(params)
		        .block();

		FunctionOutput output = new ReactiveKIRuntime(SuspendTestDefinitions.signalWaitWithTimeoutBranch())
		        .resume(params.getSuspension(), Map.of(), true, fRepo, sRepo, Map.of())
		        .block();

		EventResult result = SuspendTestDefinitions.eventNamed(output, Event.OUTPUT);

		assertNotNull(result);
		assertEquals("expired", result.getResult()
		        .get("value")
		        .getAsString());
	}

	@Test
	void takesTheApprovedBranchWhenResumedWithTheSignal() {

		ReactiveFunctionExecutionParameters params = params();

		new ReactiveKIRuntime(SuspendTestDefinitions.signalWaitWithTimeoutBranch()).execute(params)
		        .block();

		FunctionOutput output = new ReactiveKIRuntime(SuspendTestDefinitions.signalWaitWithTimeoutBranch())
		        .resume(params.getSuspension(), Map.of(), false, fRepo, sRepo, Map.of())
		        .block();

		EventResult result = SuspendTestDefinitions.eventNamed(output, Event.OUTPUT);

		assertNotNull(result);
		assertEquals("approved", result.getResult()
		        .get("value")
		        .getAsString());
	}

	// -------------------------------------------------------------------------------------------
	// Guards
	// -------------------------------------------------------------------------------------------

	@Test
	void refusesTwoStepsStoppingInTheSamePass() {

		ReactiveKIRuntime runtime = new ReactiveKIRuntime(SuspendTestDefinitions.twoIndependentWaits());

		KIRuntimeException thrown = assertThrows(KIRuntimeException.class, () -> runtime.execute(params())
		        .block());

		assertTrue(thrown.getMessage()
		        .contains("both stopped the execution"), "got : " + thrown.getMessage());
	}

	@Test
	void resumingWithoutASnapshotIsAnError() {

		StepVerifier.create(new ReactiveKIRuntime(SuspendTestDefinitions.threeStepsWithWaitInTheMiddle())
		        .resume(null, Map.of(), false, fRepo, sRepo, Map.of()))
		        .expectError(KIRuntimeException.class)
		        .verify();
	}

	// -------------------------------------------------------------------------------------------
	// The count guard must not accumulate across resumptions
	// -------------------------------------------------------------------------------------------

	@Test
	void startsTheIterationCountAgainOnResume() {

		ReactiveFunctionExecutionParameters params = params();

		new ReactiveKIRuntime(SuspendTestDefinitions.threeStepsWithWaitInTheMiddle()).execute(params)
		        .block();

		SuspendedExecution state = params.getSuspension();

		// Pretend the snapshot came back after a great many earlier resumptions.
		state.setCount(Integer.MAX_VALUE - 1);

		FunctionOutput output = new ReactiveKIRuntime(SuspendTestDefinitions.threeStepsWithWaitInTheMiddle())
		        .resume(state, Map.of(), false, fRepo, sRepo, Map.of())
		        .block();

		assertNotNull(SuspendTestDefinitions.eventNamed(output, Event.OUTPUT),
		        "a stored count must not make a resumed execution trip the infinite-loop guard");
	}

	@Test
	void resumesAFunctionThatHasRequiredParameters() {

		// Arguments are validated against the signature before the runtime gets a chance to
		// restore anything, so a resumed function with a required parameter must have its
		// arguments back in place before that validation runs.
		ReactiveFunctionExecutionParameters params = params()
		        .setArguments(Map.of("who", new JsonPrimitive("kiran")));

		new ReactiveKIRuntime(SuspendTestDefinitions.waitThenEchoAnArgument()).execute(params)
		        .block();

		SuspendedExecution state = params.getSuspension();

		assertNotNull(state);
		assertEquals("kiran", state.getArguments()
		        .get("who")
		        .getAsString(), "the arguments should be part of the snapshot");

		FunctionOutput output = new ReactiveKIRuntime(SuspendTestDefinitions.waitThenEchoAnArgument())
		        .resume(state, Map.of(), false, fRepo, sRepo, Map.of())
		        .block();

		EventResult result = SuspendTestDefinitions.eventNamed(output, Event.OUTPUT);

		assertNotNull(result, "the resumed execution should have completed");
		assertEquals("kiran", result.getResult()
		        .get("value")
		        .getAsString());
	}

	@Test
	void keepsEventsRaisedBeforeTheStopAndDropsTheSuspendedOne() {

		ReactiveFunctionExecutionParameters params = params();

		new ReactiveKIRuntime(SuspendTestDefinitions.raisesAProgressEventThenWaits()).execute(params)
		        .block();

		SuspendedExecution state = params.getSuspension();

		assertTrue(state.getEvents()
		        .containsKey("progress"), "the progress event should be in the snapshot");
		assertTrue(state.getEvents()
		        .containsKey(Event.SUSPENDED));

		FunctionOutput output = new ReactiveKIRuntime(SuspendTestDefinitions.raisesAProgressEventThenWaits())
		        .resume(state, Map.of(), false, fRepo, sRepo, Map.of())
		        .block();

		// Drain everything so both events can be inspected.
		java.util.List<String> names = new java.util.ArrayList<>();
		EventResult er;
		while ((er = output.next()) != null)
			names.add(er.getName());

		assertTrue(names.contains("progress"), "the earlier event should survive the resume, got " + names);
		assertTrue(names.contains(Event.OUTPUT), "got " + names);
		assertFalse(names.contains(Event.SUSPENDED),
		        "the stop we just came back from should not still be reported, got " + names);
	}

	@Test
	void serializedSnapshotSurvivesARoundTripUnchanged() {

		ReactiveFunctionExecutionParameters params = params();

		new ReactiveKIRuntime(SuspendTestDefinitions.threeStepsWithWaitInTheMiddle()).execute(params)
		        .block();

		String json = SuspendedExecutionSerializer.serialize(params.getSuspension());
		SuspendedExecution back = SuspendedExecutionSerializer.deserialize(json);

		assertEquals(params.getSuspension()
		        .getExecutionId(), back.getExecutionId());
		assertEquals("wait", back.getSuspendedStepName());
		assertEquals(SuspendedExecution.VERSION, back.getVersion());
		assertEquals(json, SuspendedExecutionSerializer.serialize(back), "a second round trip should be stable");
	}
}

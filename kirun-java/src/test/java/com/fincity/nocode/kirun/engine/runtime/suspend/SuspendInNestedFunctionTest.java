package com.fincity.nocode.kirun.engine.runtime.suspend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Stopping inside a function that another function called.
 *
 * This matters more than it might look: on the server every application-defined function is a
 * definition-based function, so a step that calls another function always takes the isolated-child
 * path. A reusable "notify and wait for a reply" building block only works if a stop can travel out
 * through the caller and a resume can travel back in.
 */
class SuspendInNestedFunctionTest {

	private static final String INNER_NAMESPACE = "Test";
	private static final String INNER_NAME = "WaitInside";

	private final ReactiveRepository<Schema> sRepo = new KIRunReactiveSchemaRepository();

	/** The inner function: wait, then report what the wait came back with. */
	private FunctionDefinition innerDefinition() {

		Statement wait = SuspendTestDefinitions.waitUntil("innerWait", 60_000);

		return (FunctionDefinition) SuspendTestDefinitions
		        .definition(INNER_NAME,
		                wait,
		                SuspendTestDefinitions.generateOutput("innerOutput", "Steps.innerWait.output.token",
		                        "Steps.innerWait.output"))
		        .setNamespace(INNER_NAMESPACE);
	}

	/** The outer function: record, call the inner function, record again, finish. */
	private FunctionDefinition outerDefinition() {

		Statement before = new Statement("before").setNamespace(RecordingFunction.NAMESPACE)
		        .setName(RecordingFunction.NAME)
		        .setParameterMap(Map.of("value", Map.ofEntries(ParameterReference.of(new JsonPrimitive("before")))));

		Statement callInner = new Statement("callInner").setNamespace(INNER_NAMESPACE)
		        .setName(INNER_NAME)
		        .setDependentStatements(Map.of("Steps.before.output", true));

		Statement after = new Statement("after").setNamespace(RecordingFunction.NAMESPACE)
		        .setName(RecordingFunction.NAME)
		        .setParameterMap(Map.of("value", Map.ofEntries(ParameterReference.of("Steps.callInner.output.value"))))
		        .setDependentStatements(Map.of("Steps.callInner.output", true));

		return SuspendTestDefinitions.definition("CallsAWaitingFunction", before, callInner, after,
		        SuspendTestDefinitions.generateOutput("outerOutput", "Steps.after.output.value",
		                "Steps.after.output"));
	}

	private ReactiveRepository<ReactiveFunction> repository(RecordingFunction recorder) {

		ReactiveKIRuntime inner = new ReactiveKIRuntime(innerDefinition());

		ReactiveRepository<ReactiveFunction> innerRepo = new ReactiveRepository<>() {

			@Override
			public Mono<ReactiveFunction> find(String namespace, String name) {
				return INNER_NAMESPACE.equals(namespace) && INNER_NAME.equals(name) ? Mono.just(inner) : Mono.empty();
			}

			@Override
			public Flux<String> filter(String name) {
				return Flux.just(INNER_NAMESPACE + "." + INNER_NAME);
			}
		};

		return new ReactiveHybridRepository<>(recorder.asRepository(), innerRepo,
		        new KIRunReactiveFunctionRepository());
	}

	@Test
	void aStopInsideTheCalledFunctionTravelsOutToTheCaller() {

		RecordingFunction recorder = new RecordingFunction();
		ReactiveRepository<ReactiveFunction> fRepo = repository(recorder);

		ReactiveFunctionExecutionParameters params = new ReactiveFunctionExecutionParameters(fRepo, sRepo);

		new ReactiveKIRuntime(outerDefinition()).execute(params)
		        .block();

		SuspendedExecution state = params.getSuspension();

		assertNotNull(state, "the caller should have produced a snapshot");
		assertEquals("CallsAWaitingFunction", state.getName());
		assertEquals("callInner", state.getSuspendedStepName(),
		        "the caller stopped at the step that calls the inner function");

		SuspendedExecution child = state.getChild();

		assertNotNull(child, "the inner function's own state should hang off the caller's");
		assertEquals(INNER_NAME, child.getName());
		assertEquals("innerWait", child.getSuspendedStepName());
		assertNull(child.getChild(), "there was only one level of nesting");

		// The wake condition belongs to the innermost activation, and is reachable from the top.
		assertNull(state.getWakeCondition(), "the caller itself did not choose the wake condition");
		assertNotNull(state.effectiveWakeCondition());
		assertEquals(List.of("before"), recorder.calls());
	}

	@Test
	void resumingReEntersTheCalledFunctionAndThenCarriesOn() {

		RecordingFunction recorder = new RecordingFunction();
		ReactiveRepository<ReactiveFunction> fRepo = repository(recorder);

		ReactiveFunctionExecutionParameters params = new ReactiveFunctionExecutionParameters(fRepo, sRepo);

		new ReactiveKIRuntime(outerDefinition()).execute(params)
		        .block();

		SuspendedExecution reloaded = SuspendedExecutionSerializer
		        .deserialize(SuspendedExecutionSerializer.serialize(params.getSuspension()));

		FunctionOutput output = new ReactiveKIRuntime(outerDefinition())
		        .resume(reloaded, Map.of("token", new JsonPrimitive("from-the-signal")), false, fRepo, sRepo, Map.of())
		        .block();

		assertEquals(List.of("before", "from-the-signal"), recorder.calls(),
		        "the caller should have carried on with what the inner function returned");

		var result = SuspendTestDefinitions.eventNamed(output, Event.OUTPUT);

		assertNotNull(result, "the whole thing should have completed");
		assertEquals("from-the-signal", result.getResult()
		        .get("value")
		        .getAsString());
	}
}

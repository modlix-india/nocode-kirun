package com.fincity.nocode.kirun.engine.runtime;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.reactive.ReactiveHybridRepository;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveKIRuntime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Regression test for the 2026-07-30 dev core CPU runaway.
 *
 * A step whose function completes empty (the real case was a REST connection whose
 * OAuth token could not be minted, so RestAuthService returned an empty Mono) must
 * terminate the graph, not get re-executed. In the incident a single request drove
 * ~90 re-executions/second for ~22 minutes at 452% CPU with nothing logged as an error.
 */
class KIRuntimeEmptyStepOutputTest {

	/** Stands in for a platform function that completes empty instead of raising an event. */
	static class ReturnsEmpty extends AbstractReactiveFunction {

		private final AtomicInteger invocations = new AtomicInteger();

		private static final FunctionSignature SIGNATURE = new FunctionSignature()
				.setName("returnsEmpty")
				.setNamespace("stub")
				.setParameters(Map.of())
				.setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of())));

		@Override
		public FunctionSignature getSignature() {
			return SIGNATURE;
		}

		int getInvocations() {
			return this.invocations.get();
		}

		@Override
		protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
			this.invocations.incrementAndGet();
			return Mono.empty();
		}
	}

	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Type.class, new SchemaTypeAdapter())
			.create();

	private static Mono<FunctionOutput> run(String definitionJson, ReturnsEmpty stub) {

		var def = GSON.fromJson(definitionJson, FunctionDefinition.class);

		class InternalRepository implements ReactiveRepository<ReactiveFunction> {

			@Override
			public Mono<ReactiveFunction> find(String namespace, String name) {
				if ("stub".equals(namespace))
					return Mono.just(stub);
				return Mono.empty();
			}

			@Override
			public Flux<String> filter(String name) {
				return Flux.empty();
			}
		}

		var repo = new ReactiveHybridRepository<>(new KIRunReactiveFunctionRepository(), new InternalRepository());

		return new ReactiveKIRuntime(def)
				.execute(new ReactiveFunctionExecutionParameters(repo, new KIRunReactiveSchemaRepository())
						.setArguments(Map.of()));
	}

	@Test
	void emptyStepMustNotBeReExecuted() {

		ReturnsEmpty stub = new ReturnsEmpty();

		String def = """
				{
					"name": "callsEmptyStep",
					"namespace": "test",
					"steps": {
						"emptyCall": {
							"statementName": "emptyCall",
							"namespace": "stub",
							"name": "returnsEmpty"
						}
					}
				}""";

		// Bounded so a spin fails the test instead of hanging the build.
		try {
			run(def, stub).block(Duration.ofSeconds(10));
		} catch (Exception e) {
			System.out.println("[emptyStep] terminated with: " + e.getClass().getSimpleName() + " -> "
					+ e.getMessage());
		}

		System.out.println("[emptyStep] stub invocations = " + stub.getInvocations());

		org.junit.jupiter.api.Assertions.assertTrue(
				stub.getInvocations() <= 1,
				"A step that completes empty must be executed at most once, but was executed "
						+ stub.getInvocations() + " times");
	}

	@Test
	void emptyStepWithDependentStepMustNotBeReExecuted() {

		ReturnsEmpty stub = new ReturnsEmpty();

		// Closer to cxapp.sendAadharOTP: a later step depends on the empty step's output,
		// so its dependencies can never resolve.
		String def = """
				{
					"name": "callsEmptyStepWithDependent",
					"namespace": "test",
					"steps": {
						"emptyCall": {
							"statementName": "emptyCall",
							"namespace": "stub",
							"name": "returnsEmpty"
						},
						"afterEmpty": {
							"statementName": "afterEmpty",
							"namespace": "System",
							"name": "GenerateEvent",
							"dependentStatements": {
								"Steps.emptyCall.output": true
							},
							"parameterMap": {
								"eventName": {
									"one": {
										"key": "one",
										"type": "VALUE",
										"value": "output"
									}
								}
							}
						}
					}
				}""";

		try {
			run(def, stub).block(Duration.ofSeconds(10));
		} catch (Exception e) {
			System.out.println("[dependent] terminated with: " + e.getClass().getSimpleName() + " -> "
					+ e.getMessage());
		}

		System.out.println("[dependent] stub invocations = " + stub.getInvocations());

		org.junit.jupiter.api.Assertions.assertTrue(
				stub.getInvocations() <= 1,
				"A step that completes empty must be executed at most once, but was executed "
						+ stub.getInvocations() + " times");
	}
}

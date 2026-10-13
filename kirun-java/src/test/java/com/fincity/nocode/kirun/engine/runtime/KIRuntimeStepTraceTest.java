package com.fincity.nocode.kirun.engine.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeStepTrace;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.reactive.ReactiveHybridRepository;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveKIRuntime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * A step's error carries which step of which function raised it and the arguments the step got,
 * as a suppressed KIRuntimeStepTrace, and is otherwise the error the function raised.
 */
class KIRuntimeStepTraceTest {

	private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
			.create();

	/** Throws straight out of internalExecute rather than returning Mono.error, the harder case. */
	private static final ReactiveFunction EXPLODE = new AbstractReactiveFunction() {

		@Override
		public FunctionSignature getSignature() {
			return new FunctionSignature().setName("Explode")
					.setNamespace("Test")
					.setParameters(Map.of(
							"items", new Parameter().setParameterName("items").setSchema(Schema.ofAny("items")),
							"password",
							new Parameter().setParameterName("password").setSchema(Schema.ofString("password"))))
					.setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of())));
		}

		@Override
		protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
			throw new ConcurrentModificationException();
		}
	};

	private static ReactiveFunctionExecutionParameters params() {
		var repo = new ReactiveHybridRepository<>(new KIRunReactiveFunctionRepository(),
				new ReactiveRepository<ReactiveFunction>() {

					@Override
					public Flux<String> filter(String name) {
						return Flux.empty();
					}

					@Override
					public Mono<ReactiveFunction> find(String namespace, String name) {
						return "Test".equals(namespace) && "Explode".equals(name) ? Mono.just(EXPLODE) : Mono.empty();
					}
				});
		return new ReactiveFunctionExecutionParameters(repo, new KIRunReactiveSchemaRepository(), "Testing");
	}

	private static FunctionDefinition definition(String itemsParameter) {
		return GSON.fromJson("""
				{
					"name": "syncAll",
					"namespace": "Leads",
					"steps": {
						"sortLeads": {
							"statementName": "sortLeads",
							"namespace": "Test",
							"name": "Explode",
							"parameterMap": {
								"items": { "one": %s },
								"password": { "two": { "key": "two", "type": "VALUE", "value": "hunter2" } }
							}
						}
					}
				}
				""".formatted(itemsParameter), FunctionDefinition.class);
	}

	private static List<KIRuntimeStepTrace> traces(Throwable error) {
		return Arrays.stream(error.getSuppressed())
				.filter(KIRuntimeStepTrace.class::isInstance)
				.map(KIRuntimeStepTrace.class::cast)
				.toList();
	}

	@Test
	void theFailingStepAndItsArgumentsRideOnTheError() {

		var run = new ReactiveKIRuntime(definition("{ \"key\": \"one\", \"type\": \"VALUE\", \"value\": [3, 1, 2] }"),
				false).execute(params());

		StepVerifier.create(run).expectErrorSatisfies(e -> {
			// The function's own error, not a wrapper: callers see the same type and message as before.
			assertInstanceOf(ConcurrentModificationException.class, e);
			assertNull(e.getMessage());

			List<KIRuntimeStepTrace> traces = traces(e);
			assertEquals(1, traces.size());
			KIRuntimeStepTrace t = traces.get(0);
			assertEquals("Leads.syncAll", t.getFunctionName());
			assertEquals("sortLeads", t.getStatementName());
			assertEquals("Test.Explode", t.getStepFunction());
			assertTrue(t.getArguments().contains("\"items\": [3, 1, 2]"), t.getArguments());
			assertTrue(t.getArguments().contains("\"password\": \"***\""), t.getArguments());
			assertTrue(!t.getArguments().contains("hunter2"), t.getArguments());
			assertTrue(t.getMessage().startsWith("at step sortLeads (Test.Explode) of Leads.syncAll\narguments {"),
					t.getMessage());
		}).verify();
	}

	@Test
	void anArgumentThatCannotBeEvaluatedNamesTheStepWithoutArguments() {

		var run = new ReactiveKIRuntime(
				definition("{ \"key\": \"one\", \"type\": \"EXPRESSION\", \"expression\": \"Arguments.a +\" }"), false)
				.execute(params());

		StepVerifier.create(run).expectErrorSatisfies(e -> {
			List<KIRuntimeStepTrace> traces = traces(e);
			assertEquals(1, traces.size(), Arrays.toString(e.getSuppressed()));
			assertEquals("sortLeads", traces.get(0).getStatementName());
			assertNull(traces.get(0).getArguments());
		}).verify();
	}

	@Test
	void argumentsAreBoundedAndOneErrorCollectsAtMostTwentyTraces() {

		JsonArray many = new JsonArray();
		for (int i = 0; i < 50; i++)
			many.add(i);
		Map<String, JsonElement> args = new LinkedHashMap<>();
		args.put("list", many);
		args.put("text", new JsonPrimitive("x".repeat(500)));

		RuntimeException shared = new RuntimeException("reused");
		for (int i = 0; i < 30; i++)
			KIRuntimeStepTrace.attach(shared, "F", "s" + i, "N.f", args);

		List<KIRuntimeStepTrace> traces = traces(shared);
		assertEquals(20, traces.size());
		String a = traces.get(0).getArguments();
		assertTrue(a.contains("9, … 40 more]"), a);
		assertTrue(a.contains("… (500 chars)"), a);
	}
}

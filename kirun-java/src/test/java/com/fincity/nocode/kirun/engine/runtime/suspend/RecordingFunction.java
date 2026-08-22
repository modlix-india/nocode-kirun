package com.fincity.nocode.kirun.engine.runtime.suspend;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@code Test.Record}, a step that notes the value it was called with.
 *
 * Whether a resumed loop re-runs work it had already done cannot be seen from a function's output -
 * the output would look the same either way. Recording each call is the only way to tell "carried on
 * from item three" from "started again at item one".
 */
class RecordingFunction extends AbstractReactiveFunction {

	static final String NAMESPACE = "Test";
	static final String NAME = "Record";

	private static final String VALUE = "value";

	private final List<String> calls = new CopyOnWriteArrayList<>();

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName(NAME)
	        .setNamespace(NAMESPACE)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(VALUE, Schema.ofAny(VALUE))))
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.ofAny(VALUE)))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters context) {

		JsonElement value = context.getArguments()
		        .get(VALUE);

		this.calls.add(value == null || value.isJsonNull() ? "null" : value.getAsString());

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, value)))));
	}

	/** Every value this step has been called with, in order. */
	List<String> calls() {
		return Collections.unmodifiableList(this.calls);
	}

	/** A repository serving this one instance, so the recorded calls survive across a resume. */
	ReactiveRepository<ReactiveFunction> asRepository() {

		RecordingFunction self = this;

		return new ReactiveRepository<>() {

			@Override
			public Mono<ReactiveFunction> find(String namespace, String name) {
				return NAMESPACE.equals(namespace) && NAME.equals(name) ? Mono.just(self) : Mono.empty();
			}

			@Override
			public Flux<String> filter(String name) {
				return Flux.just(NAMESPACE + "." + NAME)
				        .filter(e -> name == null || e.toLowerCase()
				                .contains(name.toLowerCase()));
			}
		};
	}
}

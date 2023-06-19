package com.fincity.nocode.kirun.engine.function.system.loop;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class ForEachLoop extends AbstractReactiveFunction {

	private static final String SOURCE = "source";
	private static final String EACH = "each";
	private static final String INDEX = "index";
	private static final String VALUE = "value";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("ForEachLoop")
	        .setNamespace(Namespaces.SYSTEM_LOOP)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(SOURCE, Schema.ofArray(SOURCE, Schema.ofAny(SOURCE)))))
	        .setEvents(Map.ofEntries(
	                Event.eventMapEntry(Event.ITERATION,
	                        Map.of(INDEX, Schema.of(INDEX, SchemaType.INTEGER), EACH, Schema.ofAny(EACH))),
	                Event.outputEventMapEntry(Map.of(VALUE, Schema.of(VALUE, SchemaType.INTEGER)))));

	@Override
	public FunctionSignature getSignature() {

		return SIGNATURE;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
		JsonArray source = context.getArguments()
		        .get(SOURCE)
		        .getAsJsonArray();

		AtomicInteger current = new AtomicInteger(0);

		String statementName = context.getStatementExecution() == null ? null
		        : context.getStatementExecution()
		                .getStatement()
		                .getStatementName();

		return Mono.just(new FunctionOutput(() -> {

			if (current.get() >= source.size() || (statementName != null && context.getExecutionContext()
			        .getOrDefault(statementName, new JsonPrimitive(false))
			        .getAsBoolean())) {
				if (statementName != null)
					context.getExecutionContext()
					        .remove(statementName);
				return EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(current.get())));
			}

			var eve = EventResult.of(Event.ITERATION,
			        Map.of(INDEX, new JsonPrimitive(current.get()), EACH, source.get(current.get())));

			current.getAndIncrement();

			return eve;
		}));
	}

}

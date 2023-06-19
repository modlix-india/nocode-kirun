package com.fincity.nocode.kirun.engine.function.system.loop;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM_LOOP;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionOutputGenerator;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class CountLoop extends AbstractReactiveFunction {

	static final String COUNT = "count";

	static final String VALUE = "value";

	static final String INDEX = "index";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("CountLoop")
	        .setNamespace(SYSTEM_LOOP)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(COUNT, Schema.of(COUNT, SchemaType.INTEGER))))
	        .setEvents(Map.ofEntries(
	                Event.eventMapEntry(Event.ITERATION, Map.of(INDEX, Schema.of(INDEX, SchemaType.INTEGER))),
	                Event.outputEventMapEntry(Map.of(VALUE, Schema.of(VALUE, SchemaType.INTEGER)))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		int count = context.getArguments()
		        .get(COUNT)
		        .getAsInt();

		AtomicInteger current = new AtomicInteger(0);

		String statementName = context.getStatementExecution() == null ? null
		        : context.getStatementExecution()
		                .getStatement()
		                .getStatementName();

		FunctionOutputGenerator generator = () -> {

			if (current.intValue() >= count || (statementName != null && context.getExecutionContext()
			        .getOrDefault(statementName, new JsonPrimitive(false))
			        .getAsBoolean())) {
				if (statementName != null)
					context.getExecutionContext()
					        .remove(statementName);
				return EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(count)));
			}

			EventResult er = EventResult.of(Event.ITERATION, Map.of(INDEX, new JsonPrimitive(current.get())));
			current.incrementAndGet();
			return er;
		};

		return Mono.just(generator)
		        .map(FunctionOutput::new);
	}
}

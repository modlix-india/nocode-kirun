package com.fincity.nocode.kirun.engine.function.system.loop;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM_LOOP;

import java.util.Map;

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
import com.fincity.nocode.kirun.engine.runtime.suspend.LoopCursor;
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

		String statementName = context.getStatementExecution() == null ? null
		        : context.getStatementExecution()
		                .getStatement()
		                .getStatementName();

		// The position lives in the execution context rather than in this closure, so that a
		// suspension inside the loop body can be snapshotted and this loop can be re-entered on
		// resume at the iteration it stopped on.
		LoopCursor.Cursor cursor = LoopCursor.of(context, statementName);

		FunctionOutputGenerator generator = () -> {

			int current = cursor.getAsInt();

			if (current >= count || (statementName != null && context.getExecutionContext()
			        .getOrDefault(statementName, new JsonPrimitive(false))
			        .getAsBoolean())) {
				if (statementName != null)
					context.getExecutionContext()
					        .remove(statementName);
				cursor.clear();
				return EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(count)));
			}

			EventResult er = EventResult.of(Event.ITERATION, Map.of(INDEX, new JsonPrimitive(current)));
			cursor.set(current + 1d);
			return er;
		};

		return Mono.just(generator)
		        .map(FunctionOutput::new);
	}
}

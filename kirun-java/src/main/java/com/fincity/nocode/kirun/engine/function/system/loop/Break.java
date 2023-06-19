package com.fincity.nocode.kirun.engine.function.system.loop;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM_LOOP;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class Break extends AbstractReactiveFunction {

	private static final String STEP_NAME = "stepName";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Break")
	        .setNamespace(SYSTEM_LOOP)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(STEP_NAME, Schema.of(STEP_NAME, SchemaType.STRING))))
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of())));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		String stepName = context.getArguments()
		        .get(STEP_NAME)
		        .getAsString();

		context.getExecutionContext()
		        .put(stepName, new JsonPrimitive(true));

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of()))));
	}
}

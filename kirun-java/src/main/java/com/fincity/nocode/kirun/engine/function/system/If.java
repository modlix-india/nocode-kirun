package com.fincity.nocode.kirun.engine.function.system;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM;

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
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class If extends AbstractReactiveFunction {

	static final String CONDITION = "condition";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("If")
			.setNamespace(SYSTEM)
			.setParameters(Map.ofEntries(Parameter.ofEntry(CONDITION, Schema.ofAny(CONDITION))))
			.setEvents(Map.ofEntries(Event.eventMapEntry(Event.TRUE, Map.of()),
					Event.eventMapEntry(Event.FALSE, Map.of()), Event.outputEventMapEntry(Map.of())));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonElement condition = context.getArguments()
				.get(CONDITION);

		boolean condBoolean = !(condition == null || condition.isJsonNull());
		if (condBoolean && condition.isJsonPrimitive()) {
			JsonPrimitive jp = condition.getAsJsonPrimitive();
			if (jp.isBoolean())
				condBoolean = jp.getAsBoolean();
			else if (jp.isNumber())
				condBoolean = jp.getAsDouble() != 0.0d;
		}

		return Mono.just(new FunctionOutput(
				List.of(EventResult.of(condBoolean ? Event.TRUE : Event.FALSE, Map.of()),
						EventResult.outputOf(Map.of()))));
	}
}

package com.fincity.nocode.kirun.engine.function.system;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class Wait extends AbstractReactiveFunction {

	static final String MILLIS = "millis";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Wait")
			.setNamespace(SYSTEM)
			.setParameters(Map.ofEntries(Parameter.ofEntry(MILLIS,
					Schema.ofNumber(MILLIS).setMinimum(0).setDefaultValue(new JsonPrimitive(0)))))
			.setEvents(Map.ofEntries(Event.eventMapEntry(Event.TRUE, Map.of())));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		var millis = context.getArguments()
				.get(MILLIS).getAsLong();

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of()))))
				.delayElement(Duration.ofMillis(millis));
	}
}

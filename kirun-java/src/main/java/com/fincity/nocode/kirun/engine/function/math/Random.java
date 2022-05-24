package com.fincity.nocode.kirun.engine.function.math;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.MATH;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Argument;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;

public class Random extends AbstractFunction {

	private static final String VALUE = "value";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Random")
	        .setNamespace(MATH)
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.DOUBLE))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, List<Argument>> args) {

		return Flux.just(EventResult.outputResult(Map.of(VALUE, new JsonPrimitive(Math.random()))));
	}
}

package com.fincity.nocode.kirun.engine.function.system;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Argument;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;

public class RangeLoop extends AbstractFunction {

	private static final String FROM = "from";

	private static final String TO = "to";

	private static final String STEP = "step";

	private static final String VALUE = "value";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("RangeLoop")
	        .setNamespace(SYSTEM)
	        .setParameters(Map.ofEntries(
	                Parameter.ofEntry(FROM,
	                        Schema.of(FROM, SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE)
	                                .setDefaultValue(new JsonPrimitive(0))),
	                Parameter.ofEntry(TO,
	                        Schema.of(TO, SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE)
	                                .setDefaultValue(new JsonPrimitive(1))),
	                Parameter.ofEntry(STEP,
	                        Schema.of(STEP, SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE)
	                                .setDefaultValue(new JsonPrimitive(1)))))
	        .setEvents(Map.ofEntries(
	        		Event.eventMapEntry(Event.ITERATION, Map.of(VALUE, Schema.of(STEP, SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE))),
	        		Event.outputEventMapEntry(Map.of(VALUE, Schema.of(STEP, SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE)))
	        ));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, List<Argument>> args) {

		return Flux.just(EventResult.outputResult(Map.of(VALUE, new JsonPrimitive(Math.random()))));
	}

}

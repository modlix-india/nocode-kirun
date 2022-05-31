package com.fincity.nocode.kirun.engine.function.math;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.MATH;

import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.function.util.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.ContextElement;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Abs extends AbstractFunction {

	static final String VALUE = "value";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Abs")
	        .setNamespace(MATH)
	        .setParameters(Map.of(VALUE, new Parameter().setParameterName(VALUE)
	                .setSchema(Schema.NUMBER)))
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.NUMBER))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, ContextElement> context,
	        Map<String, Mono<JsonElement>> args) {

		return Flux.from(args.get(VALUE))
		        .map(pValue ->
			        {
				        SchemaType type = PrimitiveUtil.findPrimitiveType(pValue.getAsJsonPrimitive());
				        JsonPrimitive rValue = null;

				        switch (type) {
				        case DOUBLE:
					        rValue = new JsonPrimitive(Math.abs(pValue.getAsDouble()));
					        break;
				        case FLOAT:
					        rValue = new JsonPrimitive(Math.abs(pValue.getAsFloat()));
					        break;
				        case LONG:
					        rValue = new JsonPrimitive(Math.abs(pValue.getAsLong()));
					        break;
				        default:
					        rValue = new JsonPrimitive(Math.abs(pValue.getAsInt()));
				        }

				        return rValue;
			        })
		        .map(e -> Map.of(VALUE, (JsonElement) e))
		        .map(EventResult::outputOf);
	}
}

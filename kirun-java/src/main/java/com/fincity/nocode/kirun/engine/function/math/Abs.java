package com.fincity.nocode.kirun.engine.function.math;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.MATH;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.function.util.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;

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
	protected Flux<EventResult> internalExecute(FunctionExecutionParameters context) {

		return Flux.just(context.getArguments().get(VALUE))
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
	
	@Override
	public Map<String, Event> getProbableEventSignature(Map<String, List<Schema>> probableParameters) {
		
		Schema s = probableParameters.get(VALUE).get(0);
		return Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, s)));
	}
}

package com.fincity.nocode.kirun.engine.function.math;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.MATH;

import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.function.util.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Argument;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Add extends AbstractFunction {

	private static final String VALUE = "value";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Add")
	        .setNamespace(MATH)
	        .setParameters(List.of(new Parameter().setSchema(Schema.NUMBER)
	                .setVariableArgument(true)))
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.NUMBER))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, List<Argument>> args) {

		Mono<Number> sum = Flux.fromIterable(args.get(VALUE))
		        .map(Argument::getValue)
		        .map(JsonPrimitive.class::cast)
		        .map(e ->
			        {
				        SchemaType type = PrimitiveUtil.findPrimitiveType(e);

				        if (type == SchemaType.INTEGER)
					        return e.getAsInt();
				        if (type == SchemaType.LONG)
					        return e.getAsLong();
				        if (type == SchemaType.FLOAT)
					        return e.getAsFloat();

				        return e.getAsDouble();
			        })
		        .reduce((a, b) ->
			        {
				        if (a instanceof Double || b instanceof Double)
					        return a.doubleValue() + b.doubleValue();
				        if (a instanceof Float || b instanceof Float)
					        return a.floatValue() + b.floatValue();
				        if (a instanceof Long || b instanceof Long)
					        return a.longValue() + b.longValue();
				        return (int) a + (int) b;
			        })
		        .map(Number.class::cast);

		return Flux.merge((Publisher<? extends EventResult>) sum.map(PrimitiveUtil::toPrimitiveType)
		        .map(e -> Map.of(VALUE, (JsonElement) e))
		        .map(EventResult::outputResult));
	}
}

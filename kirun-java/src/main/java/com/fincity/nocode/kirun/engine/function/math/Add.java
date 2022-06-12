package com.fincity.nocode.kirun.engine.function.math;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.MATH;

import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.function.util.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Add extends AbstractFunction {

	static final String VALUE = "value";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Add")
	        .setNamespace(MATH)
	        .setParameters(Map.of(VALUE, new Parameter().setSchema(Schema.NUMBER)
	                .setVariableArgument(true)))
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.NUMBER))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, ContextElement> context,
	        Map<String, JsonElement> args) {

		Mono<Number> sum = Mono.just(args.get(VALUE))
		        .map(JsonArray.class::cast)
		        .flatMapMany(Flux::fromIterable)
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
		        .map(EventResult::outputOf));
	}

	@Override
	public Map<String, Event> getProbableEventSignature(Map<String, List<Schema>> probableParameters) {

		Schema schema = probableParameters.get(VALUE)
		        .stream()
		        .flatMap(e -> e.getType()
		                .getAllowedSchemaTypes()
		                .stream())
		        .reduce((a, b) -> a.ordinal() < b.ordinal() ? b : a)
		        .map(e -> new Schema().setType(Type.of(e)))
		        .orElse(Schema.NUMBER);

		return Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, schema)));
	}
}

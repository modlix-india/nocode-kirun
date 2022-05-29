package com.fincity.nocode.kirun.engine.function.string;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.STRING;

import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.SingleType;
import com.fincity.nocode.kirun.engine.model.Argument;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Concatenate extends AbstractFunction {

	private static final String VALUE = "value";

	private static final Schema SCHEMA = new Schema().setName(VALUE)
	        .setTitle(VALUE)
	        .setType(new SingleType().setType(SchemaType.STRING));

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Concatenate")
	        .setNamespace(STRING)
	        .setParameters(Map.of(VALUE, new Parameter().setSchema(SCHEMA)
	                .setVariableArgument(true)))
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.STRING))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, Mono<JsonElement>> context,
	        Map<String, List<Argument>> args) {

		Mono<String> concatenatedString = Flux.fromIterable(args.get(VALUE))
		        .map(Argument::getValue)
		        .map(JsonElement::getAsString)
		        .defaultIfEmpty("")
		        .reduce((a, b) -> a + b);

		return Flux.merge((Publisher<? extends EventResult>) concatenatedString.map(JsonPrimitive::new)
		        .map(e -> Map.of(VALUE, (JsonElement) e))
		        .map(EventResult::outputResult));
	}
}

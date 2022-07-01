package com.fincity.nocode.kirun.engine.function.string;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.STRING;

import java.util.Map;
import java.util.Objects;

import org.reactivestreams.Publisher;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.SingleType;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Concatenate extends AbstractFunction {

	static final String VALUE = "value";

	private static final Schema SCHEMA = new Schema().setName(VALUE)
	        .setType(new SingleType(SchemaType.STRING));

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Concatenate")
	        .setNamespace(STRING)
	        .setParameters(Map.of(VALUE, new Parameter().setSchema(SCHEMA)
	                .setVariableArgument(true)))
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.ofString(VALUE)))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Flux<EventResult> internalExecute(FunctionExecutionParameters context) {

		Mono<String> concatenatedString = Mono.just(context.getArguments().get(VALUE))
		        .map(JsonArray.class::cast)
		        .flatMapMany(Flux::fromIterable)
		        .filter(Objects::nonNull)
		        .map(JsonElement::getAsString)
		        .defaultIfEmpty("")
		        .reduce((a, b) -> a + b);

		return Flux.merge((Publisher<? extends EventResult>) concatenatedString.map(JsonPrimitive::new)
		        .map(e -> Map.of(VALUE, (JsonElement) e))
		        .map(EventResult::outputOf));
	}
}

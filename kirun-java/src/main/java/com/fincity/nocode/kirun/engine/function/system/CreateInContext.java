package com.fincity.nocode.kirun.engine.function.system;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM;

import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.string.StringFormat;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.model.ContextElement;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.ParameterType;
import com.fincity.nocode.kirun.engine.runtime.util.string.StringFormatter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CreateInContext extends AbstractFunction {

	static final String NAME = "name";

	static final String SCHEMA = "schema";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("CreateContext")
	        .setNamespace(SYSTEM)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(NAME, new Schema().setName(NAME)
	                .setType(Type.of(SchemaType.STRING))
	                .setMinLength(1)
	                .setFormat(StringFormat.REGEX)
	                .setPattern("^[a-zA-Z_$][a-zA-Z_$0-9]*$"), ParameterType.CONSTANT),
	                Parameter.ofEntry(SCHEMA, Schema.SCHEMA, ParameterType.CONSTANT)))
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of())));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, ContextElement> context,
	        Map<String, Mono<JsonElement>> args) {

		String name = args.get(NAME)
		        .map(JsonElement::getAsString)
		        .blockOptional()
		        .orElse("");

		if (context.containsKey(name))
			throw new KIRuntimeException(StringFormatter.format("Context already has an element for '$' ", name));

		JsonElement schema = args.get(SCHEMA)
		        .block();
		Schema s = new Gson().fromJson(schema, Schema.class);

		context.put(name, new ContextElement(s,
		        s.getDefaultValue() == null ? Mono.just(JsonNull.INSTANCE) : Mono.just(s.getDefaultValue())));

		return Flux.just(EventResult.outputOf(Map.of()));
	}

}

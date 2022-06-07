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
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SetInContext extends AbstractFunction {

	static final String NAME = "name";

	static final String VALUE = "value";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("SetInContext")
	        .setNamespace(SYSTEM)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(NAME, new Schema().setName(NAME)
	                .setType(Type.of(SchemaType.STRING))
	                .setMinLength(1)
	                .setFormat(StringFormat.REGEX)
	                .setPattern("^[a-zA-Z_$][a-zA-Z_$0-9]*$"), ParameterType.CONSTANT),
	                Parameter.ofEntry(VALUE, Schema.ANY)))
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of())));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, ContextElement> context,
	        Map<String, Mono<JsonElement>> args) {

		Mono<String> key = args.get(NAME)
		        .map(JsonElement::getAsString)
		        .defaultIfEmpty("");

		return Flux.from(key.map(e -> {

			if (e.isBlank()) {
				throw new KIRuntimeException("Empty string is not a valid name for the context element");
			}

			if (!context.containsKey(e)) {
				throw new KIRuntimeException(
				        StringFormatter.format("Context doesn't have any element with name '$' ", e));
			}

			//TODO: Here I need to validate the schema of the value I have to put in the context.

			ContextElement ctxe = context.get(e);
			ctxe.setElement(ctxe.getElement()
			        .flatMap(x -> args.get(VALUE)));

			return e;
		}))
		        .map(e -> EventResult.outputOf(Map.of()));
	}

}

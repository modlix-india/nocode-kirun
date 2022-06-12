package com.fincity.nocode.kirun.engine.function.system.context;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM_CTX;

import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.string.StringFormat;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.ParameterType;
import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;

public class Set extends AbstractFunction {

	static final String NAME = "name";

	static final String VALUE = "value";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Set")
	        .setNamespace(SYSTEM_CTX)
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
	        Map<String, JsonElement> args) {

		String key = args.get(NAME).getAsString();
		
		if (key.isBlank()) {
			throw new KIRuntimeException("Empty string is not a valid name for the context element");
		}

		if (!context.containsKey(key)) {
			throw new KIRuntimeException(
			        StringFormatter.format("Context doesn't have any element with name '$' ", key));
		}
		
		//TODO: Here I need to validate the schema of the value I have to put in the context.

		context.get(key).setElement(args.get(VALUE));

		return Flux.just(EventResult.outputOf(Map.of()));
	}

}

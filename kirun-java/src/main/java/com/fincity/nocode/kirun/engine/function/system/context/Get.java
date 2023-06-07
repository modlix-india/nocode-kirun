package com.fincity.nocode.kirun.engine.function.system.context;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM_CTX;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.string.StringFormat;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.ParameterType;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;

import reactor.core.publisher.Mono;

public class Get extends AbstractReactiveFunction {

	static final String NAME = "name";

	static final String VALUE = "value";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Get")
	        .setNamespace(SYSTEM_CTX)
	        .setParameters(Map.ofEntries(Parameter.ofEntry(NAME, new Schema().setName(NAME)
	                .setType(Type.of(SchemaType.STRING))
	                .setMinLength(1)
	                .setFormat(StringFormat.REGEX)
	                .setPattern("^[a-zA-Z_$][a-zA-Z_$0-9]*$"), ParameterType.CONSTANT)))
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.ofAny(VALUE)))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		String name = context.getArguments()
		        .get(NAME)
		        .getAsString();

		if (!context.getContext()
		        .containsKey(name))
			throw new KIRuntimeException(StringFormatter.format("Context don't have an element for '$' ", name));

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, context.getContext()
		        .get(name)
		        .getElement())))));
	}

}
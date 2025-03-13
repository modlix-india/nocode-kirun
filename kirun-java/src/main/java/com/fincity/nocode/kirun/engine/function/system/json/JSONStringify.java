package com.fincity.nocode.kirun.engine.function.system.json;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class JSONStringify extends AbstractReactiveFunction {

	private static final String SOURCE = "source";
	private static final String VALUE = "value";

	private FunctionSignature signature;

	public JSONStringify() {
		this.signature = new FunctionSignature().setName("JSONStringify")
				.setNamespace(Namespaces.SYSTEM_JSON)
				.setParameters(Map.of(SOURCE,
						new Parameter().setParameterName(SOURCE)
								.setSchema(Schema.ofAny(SOURCE))))
				.setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.ofString(VALUE)))));
	}

	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonElement source = context.getArguments()
				.get(SOURCE);

		source = source == null || source.isJsonNull() ? JsonNull.INSTANCE : source;

		Gson gson = new Gson();
		String jsonString = gson.toJson(source);

		return Mono
				.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(jsonString))))));
	}

	@Override
	public FunctionSignature getSignature() {
		return signature;
	}

}
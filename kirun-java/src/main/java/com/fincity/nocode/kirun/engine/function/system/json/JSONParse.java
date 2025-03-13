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
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class JSONParse extends AbstractReactiveFunction {

	private static final String SOURCE = "source";
	private static final String VALUE = "value";
	private static final String ERROR = "error";
	private static final String ERROR_MESSAGE = "errorMessage";

	private FunctionSignature signature;

	public JSONParse() {
		this.signature = new FunctionSignature().setName("JSONParse")
				.setNamespace(Namespaces.SYSTEM_JSON)
				.setParameters(Map.of(SOURCE,
						new Parameter().setParameterName(SOURCE)
								.setSchema(Schema.ofString(SOURCE))))
				.setEvents(Map.ofEntries(
						Event.eventMapEntry(ERROR, Map.of(ERROR_MESSAGE, Schema.ofString(ERROR_MESSAGE))),
						Event.outputEventMapEntry(Map.of(VALUE, Schema.ofAny(VALUE)))));
	}

	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		String source = context.getArguments()
				.get(SOURCE).getAsString();

		JsonElement jsonElement;
		try {
			jsonElement = JsonParser.parseString(source);
		} catch (Exception e) {
			return Mono.just(new FunctionOutput(List.of(
					EventResult.of(ERROR, Map.of(ERROR_MESSAGE, new JsonPrimitive(e.getMessage()))),
					EventResult.outputOf(Map.of(VALUE, JsonNull.INSTANCE)))));
		}

		return Mono
				.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, jsonElement)))));
	}

	@Override
	public FunctionSignature getSignature() {
		return signature;
	}

}
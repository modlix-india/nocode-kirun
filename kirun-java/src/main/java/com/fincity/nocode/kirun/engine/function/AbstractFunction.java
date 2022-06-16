package com.fincity.nocode.kirun.engine.function;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;

public abstract class AbstractFunction implements Function {

	protected Map<String, JsonElement> validateArguments(final Map<String, JsonElement> arguments) {

		return this.getSignature()
		        .getParameters()
		        .entrySet()
		        .stream()
		        .map(e ->
			        {
				        Parameter param = e.getValue();
				        JsonElement jsonElement = arguments.get(e.getKey());

				        if (jsonElement == null || jsonElement.isJsonNull()) {
					        return Map.entry(e.getKey(), SchemaValidator.validate(null, param.getSchema(), null, null));
				        }

				        if (!param.isVariableArgument())
					        return Map.entry(e.getKey(),
					                SchemaValidator.validate(null, param.getSchema(), null, jsonElement));

				        JsonArray array = null;

				        if (jsonElement.isJsonArray())
					        array = jsonElement.getAsJsonArray();
				        else {
					        array = new JsonArray();
					        array.add(jsonElement);
				        }

				        for (JsonElement je : array) {
					        SchemaValidator.validate(null, param.getSchema(), null, je);
				        }

				        return Map.entry(e.getKey(), jsonElement);
			        })
		        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public Flux<EventResult> execute(FunctionExecutionParameters context) {
		
		context.setArguments(this.validateArguments(context.getArguments()));

		return this.internalExecute(context);
	}

	protected abstract Flux<EventResult> internalExecute(FunctionExecutionParameters context);

	@Override
	public Map<String, Event> getProbableEventSignature(Map<String, List<Schema>> probableParameters) {
		return this.getSignature()
		        .getEvents();
	}
}

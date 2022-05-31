package com.fincity.nocode.kirun.engine.function;

import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.fincity.nocode.kirun.engine.model.ContextElement;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class AbstractFunction implements Function {

	protected Mono<Map<String, Mono<JsonElement>>> validateArguments(final Map<String, Mono<JsonElement>> arguments) {

		return Flux.fromIterable(this.getSignature()
		        .getParameters()
		        .entrySet())
		        .flatMap(e ->
			        {
				        Parameter param = e.getValue();
				        Mono<JsonElement> argList = arguments.get(e.getKey());

				        if (argList == null) {
					        return Mono.just(Map.entry(e.getKey(),
					                Mono.just(SchemaValidator.validate(null, param.getSchema(), null, null))));
				        }

				        return argList.defaultIfEmpty(JsonNull.INSTANCE)
				                .map(jsonElement ->
					                {
						                if (!param.isVariableArgument())
							                return SchemaValidator.validate(null, param.getSchema(), null, jsonElement);

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

						                return array;
					                })
				                .map(jsonElement -> Map.entry(e.getKey(), Mono.just(jsonElement)));
			        })
		        .collectMap(Map.Entry::getKey, Map.Entry::getValue);
	}

	@Override
	public Flux<EventResult> execute(Map<String, ContextElement> context, Map<String, Mono<JsonElement>> arguments) {

		return Flux.from(this.validateArguments(arguments))
		        .flatMap(args -> this.internalExecute(context, args));
	}

	protected abstract Flux<EventResult> internalExecute(Map<String, ContextElement> context,
	        Map<String, Mono<JsonElement>> args);
}

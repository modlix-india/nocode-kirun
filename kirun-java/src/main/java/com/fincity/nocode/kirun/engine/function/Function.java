package com.fincity.nocode.kirun.engine.function;

import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Function {

	public FunctionSignature getSignature();

	public Flux<EventResult> execute(Map<String, Mono<JsonElement>> context, Map<String, Mono<JsonElement>> arguments);
}
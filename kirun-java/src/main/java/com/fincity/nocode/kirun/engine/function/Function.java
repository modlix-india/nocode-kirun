package com.fincity.nocode.kirun.engine.function;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;

public interface Function {

	public FunctionSignature getSignature();
	
	public Map<String, Event> getProbableEventSignature(Map<String, List<Schema>> probableParameters);

	public Flux<EventResult> execute(Map<String, ContextElement> context, Map<String, JsonElement> arguments);
}
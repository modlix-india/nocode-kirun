package com.fincity.nocode.kirun.engine.function.reactive;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;

import reactor.core.publisher.Mono;

public interface ReactiveFunction {

	public FunctionSignature getSignature();
	
	public Map<String, Event> getProbableEventSignature(Map<String, List<Schema>> probableParameters);

	public Mono<FunctionOutput> execute(ReactiveFunctionExecutionParameters context);
}
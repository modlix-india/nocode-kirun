package com.fincity.nocode.kirun.engine.function;

import java.util.List;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Argument;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;

import reactor.core.publisher.Flux;

public interface Function {

	public FunctionSignature getSignature();
	
	public Schema getProperties();

	public Flux<EventResult> execute(List<Argument> arguments);
}
package com.fincity.nocode.kirun.engine.function;

import java.util.List;

import com.fincity.nocode.kirun.engine.model.Argument;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;

import reactor.core.publisher.Flux;

import com.fincity.nocode.kirun.engine.model.EventResult;

public interface Function {

	public FunctionSignature getSignature();

	public Flux<EventResult> execute(List<Argument> arguments);
}
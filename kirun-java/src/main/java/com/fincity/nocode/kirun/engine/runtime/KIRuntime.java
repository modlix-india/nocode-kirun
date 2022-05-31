package com.fincity.nocode.kirun.engine.runtime;

import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Statement;
import com.fincity.nocode.kirun.engine.runtime.util.graph.DiGraph;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class KIRuntime extends AbstractFunction {

	private FunctionDefinition fd;

	public KIRuntime(FunctionDefinition fd) {

		this.fd = fd;
	}

	@Override
	public FunctionSignature getSignature() {

		return this.fd;
	}

	public DiGraph<String, Statement> getExecutionPlan() {

		DiGraph<String, Statement> executionGraph = new DiGraph<>();

//		this.fd.getSteps()

		return executionGraph;
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, Mono<JsonElement>> context,
	        Map<String, Mono<JsonElement>> args) {

		DiGraph<String, Statement> eGraph = this.getExecutionPlan();

		return Flux.empty();
	}
}

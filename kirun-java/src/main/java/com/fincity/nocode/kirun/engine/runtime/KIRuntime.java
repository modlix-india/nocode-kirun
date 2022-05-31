package com.fincity.nocode.kirun.engine.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.model.ContextElement;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
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

	private DiGraph<String, StatementExecution> getExecutionPlan(Map<String, ContextElement> context,
	        Map<String, Mono<JsonElement>> args) {

		DiGraph<String, StatementExecution> executionGraph = new DiGraph<>();

		this.fd.getSteps()
		        .values()
		        .stream()
		        .map(e -> new StatementExecution().setStatement(e))
		        .forEach(executionGraph::addVertex);
		
					

		return executionGraph;
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, ContextElement> context,
	        Map<String, Mono<JsonElement>> args) {

		DiGraph<String, StatementExecution> eGraph = this.getExecutionPlan(context, args);

		if (context == null)
			context = new ConcurrentHashMap<>();

		return Flux.empty();
	}
}

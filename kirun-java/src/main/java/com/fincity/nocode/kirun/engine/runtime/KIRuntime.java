package com.fincity.nocode.kirun.engine.runtime;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.model.Argument;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Statement;
import com.fincity.nocode.kirun.engine.runtime.util.graph.DiGraph;

import reactor.core.publisher.Flux;

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

		return executionGraph;
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, List<Argument>> args) {

		DiGraph<String, Statement> eGraph = this.getExecutionPlan();

		return Flux.empty();
	}
}

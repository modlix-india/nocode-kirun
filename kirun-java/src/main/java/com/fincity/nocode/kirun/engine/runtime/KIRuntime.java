package com.fincity.nocode.kirun.engine.runtime;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.model.Argument;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Statement;

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
	
	public void executionPlan()  {
		
	}

	@Override
	protected Flux<EventResult> internalExecute(Map<String, List<Argument>> args) {
		
		List<Statement> start = this.fd.getSteps().values().stream().filter(e -> {
			if (e.getParameterMap() == null || e.getParameterMap().isEmpty()) return true;
			return e.getParameterMap().values().stream().filter(v -> {
				if (v.getValue() != null || v.getReferences() == null) return true;
				return v.getReferences().stream().filter(s -> !s.startsWith("_parameter.")).findFirst().isEmpty();
			}).findFirst().isEmpty();
		}).collect(Collectors.toList());
	}
}

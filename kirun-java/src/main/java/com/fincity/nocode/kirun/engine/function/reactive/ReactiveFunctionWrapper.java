package com.fincity.nocode.kirun.engine.function.reactive;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;

import reactor.core.publisher.Mono;

public class ReactiveFunctionWrapper implements ReactiveFunction {

	private Function actualFunction;
	private Repository<Function> actualRepo;

	public ReactiveFunctionWrapper(Function actualFunction, Repository<Function> actualRepo) {

		this.actualFunction = actualFunction;
		this.actualRepo = actualRepo;
	}

	@Override
	public FunctionSignature getSignature() {
		return this.actualFunction.getSignature();
	}

	@Override
	public Map<String, Event> getProbableEventSignature(Map<String, List<Schema>> probableParameters) {
		return this.actualFunction.getProbableEventSignature(probableParameters);
	}

	@Override
	public Mono<FunctionOutput> execute(ReactiveFunctionExecutionParameters context) {
		return Mono.just(this.actualFunction.execute(context.makeRegularFunctionExecutionParameters(this.actualRepo)));
	}

}

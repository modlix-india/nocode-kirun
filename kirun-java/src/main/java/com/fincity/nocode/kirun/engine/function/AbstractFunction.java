package com.fincity.nocode.kirun.engine.function;

import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class AbstractFunction implements Function {

	protected Mono<Map<String, Flux<JsonElement>>> validateArguments(Map<String, Flux<JsonElement>> arguments) {

//		for (Entry<String, Parameter> paramEntry : this.getSignature()
//		        .getParameters()
//		        .entrySet()) {
//
//			Parameter param = paramEntry.getValue();
//
//			List<Argument> argList = arguments.get(paramEntry.getKey());
//
//			if (!param.isVariableArgument() && (argList == null || argList.size() != 1))
//				throw new ExecutionException("Expects one argument with name " + param.getSchema()
//				        .getName());
//
//			if (argList != null)
//				for (Argument arg : argList)
//					SchemaValidator.validate(null, param.getSchema(), null, arg.getValue());
//		}

		return Mono.empty();
	}

	@Override
	public Flux<EventResult> execute(Map<String, Flux<JsonElement>> context, Map<String, Flux<JsonElement>> arguments) {
		
		Mono<Map<String, Flux<JsonElement>>> validArgs = this.validateArguments(arguments);
		

		Mono<Flux<EventResult>> eventResult = validArgs.map(args -> this.internalExecute(context, args));
		
		eventResult.map(null)
		
	}

	protected abstract Flux<EventResult> internalExecute(Map<String, Flux<JsonElement>> context,
	        Map<String, Flux<JsonElement>> args);
}

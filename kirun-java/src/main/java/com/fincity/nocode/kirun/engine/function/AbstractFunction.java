package com.fincity.nocode.kirun.engine.function;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.fincity.nocode.kirun.engine.model.Argument;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.Parameter;

import reactor.core.publisher.Flux;

public abstract class AbstractFunction implements Function {

	protected Map<String, List<Argument>> validateArguments(List<Argument> arguments) {

		for (int i = 0; i < arguments.size(); i++) {
			arguments.get(i)
			        .setArgumentIndex(i);
		}
		Map<String, List<Argument>> args = arguments.stream()
		        .collect(Collectors.groupingBy(Argument::getName));

		for (Entry<String, Parameter> paramEntry : this.getSignature()
		        .getParameters()
		        .entrySet()) {

			Parameter param = paramEntry.getValue();

			List<Argument> argList = args.get(paramEntry.getKey());

			if (!param.isVariableArgument() && (argList == null || argList.size() != 1))
				throw new ExecutionException("Expects one argument with name " + param.getSchema()
				        .getName());

			if (argList != null)
				for (Argument arg : argList)
					SchemaValidator.validate(null, param.getSchema(), null, arg.getValue());
		}

		return args;
	}

	@Override
	public Flux<EventResult> execute(List<Argument> arguments) {

		return this.internalExecute(this.validateArguments(arguments));
	}

	protected abstract Flux<EventResult> internalExecute(Map<String, List<Argument>> args);

}

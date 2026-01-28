package com.fincity.nocode.kirun.engine.function.reactive;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.runtime.StatementExecution;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.ErrorMessageFormatter;
import com.fincity.nocode.kirun.engine.util.arguments.ReactiveArgumentsValidationUtil;

import reactor.core.publisher.Mono;

public abstract class AbstractReactiveFunction implements ReactiveFunction {

	@Override
	public Map<String, Event> getProbableEventSignature(Map<String, List<Schema>> probableParameters) {
		return this.getSignature()
		        .getEvents();
	}

	@Override
	public Mono<FunctionOutput> execute(ReactiveFunctionExecutionParameters context) {

		try {
			return ReactiveArgumentsValidationUtil
			        .validateArguments(this.getSignature(), context.getArguments(), context.getSchemaRepository(),
			                context.getStatementExecution())
			        .map(context::setArguments)
			        .flatMap(this::internalExecute);
		} catch (Exception sve) {
			FunctionSignature signature = this.getSignature();

			String statementName = null;
			StatementExecution statementExecution = context.getStatementExecution();
			if (statementExecution != null && statementExecution.getStatement() != null)
				statementName = statementExecution.getStatement()
				        .getStatementName();

			String functionName = ErrorMessageFormatter.formatFunctionName(
			        signature.getNamespace(), signature.getName());
			String formattedStatementName = ErrorMessageFormatter.formatStatementName(statementName);
			String errorMessage = ErrorMessageFormatter.buildFunctionExecutionError(
			        functionName,
			        formattedStatementName,
			        ErrorMessageFormatter.formatErrorMessage(sve));

			throw new KIRuntimeException(errorMessage, sve);
		}
	}

	protected abstract Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context);
}

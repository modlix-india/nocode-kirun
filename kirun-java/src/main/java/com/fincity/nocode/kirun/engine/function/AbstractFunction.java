package com.fincity.nocode.kirun.engine.function;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.StatementExecution;
import com.fincity.nocode.kirun.engine.util.arguments.ArgumentsValidationUtil;

public abstract class AbstractFunction implements Function {

	
	@Override
	public Map<String, Event> getProbableEventSignature(Map<String, List<Schema>> probableParameters) {
		return this.getSignature()
		        .getEvents();
	}
	
	@Override
	public FunctionOutput execute(FunctionExecutionParameters context) {

		context.setArguments(ArgumentsValidationUtil.validateArguments(this.getSignature(), context.getArguments(), context.getSchemaRepository(),
		        context.getStatementExecution()));

		try {
			return this.internalExecute(context);
		} catch (Exception sve) {
			FunctionSignature signature = this.getSignature();

			String statementName = "Unknown Step";
			StatementExecution statementExecution = context.getStatementExecution();
			if (statementExecution != null && statementExecution.getStatement() != null)
				statementName = statementExecution.getStatement()
				        .getStatementName();

			throw new KIRuntimeException("Error while executing the function " + signature.getNamespace() + "."
			        + signature.getName() + " with step name '" + statementName + "' with error : " + sve.getMessage(),
			        sve);
		}
	}

	protected abstract FunctionOutput internalExecute(FunctionExecutionParameters context);
}

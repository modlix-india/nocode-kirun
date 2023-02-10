package com.fincity.nocode.kirun.engine.function;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.StatementExecution;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public abstract class AbstractFunction implements Function {

	protected Map<String, JsonElement> validateArguments(final Map<String, JsonElement> arguments,
	        Repository<Schema> repository, StatementExecution statementExecution) {

		return this.getSignature()
		        .getParameters()
		        .entrySet()
		        .stream()
		        .map(e ->
				{
			        Parameter param = e.getValue();
			        try {
				        return validateArgument(arguments, repository, e, param);
			        } catch (Exception sve) {

				        FunctionSignature signature = this.getSignature();
				        String statementName = "Unknown Step";
				        if (statementExecution != null && statementExecution.getStatement() != null)
					        statementName = statementExecution.getStatement()
					                .getStatementName();

				        throw new KIRuntimeException(
				                "Error while executing the function " + signature.getNamespace() + "."
				                        + signature.getName() + "'s parameter " + param.getParameterName()
				                        + " with step name '" + statementName + "' with error : " + sve.getMessage(),
				                sve);
			        }

		        })
		        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Entry<String, ? extends JsonElement> validateArgument(final Map<String, JsonElement> arguments,
	        Repository<Schema> repository, Entry<String, Parameter> e, Parameter param) {

		JsonElement jsonElement = arguments.get(e.getKey());

		if ((jsonElement == null || jsonElement.isJsonNull()) && !param.isVariableArgument()) {
			return Map.entry(e.getKey(), SchemaValidator.validate(null, param.getSchema(), repository, jsonElement));
		}

		if (!param.isVariableArgument())
			return Map.entry(e.getKey(), SchemaValidator.validate(null, param.getSchema(), repository, jsonElement));

		JsonArray array = null;

		if (jsonElement != null && jsonElement.isJsonArray())
			array = jsonElement.getAsJsonArray();
		else {
			array = new JsonArray();
			if (jsonElement != null && !jsonElement.isJsonNull())
				array.add(jsonElement);
			else if (param.getSchema()
			        .getDefaultValue() != null)
				array.add(param.getSchema()
				        .getDefaultValue());
		}

		for (int i = 0; i < array.size(); i++) {
			JsonElement je = SchemaValidator.validate(null, param.getSchema(), repository, array.get(i));
			array.set(i, je);
		}

		return Map.entry(e.getKey(), array);
	}

	@Override
	public FunctionOutput execute(FunctionExecutionParameters context) {

		context.setArguments(this.validateArguments(context.getArguments(), context.getSchemaRepository(),
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

	@Override
	public Map<String, Event> getProbableEventSignature(Map<String, List<Schema>> probableParameters) {
		return this.getSignature()
		        .getEvents();
	}
}

package com.fincity.nocode.kirun.engine.util.arguments;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.StatementExecution;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class ArgumentsValidationUtil {

	public static Map<String, JsonElement> validateArguments(FunctionSignature signature,
	        Map<String, JsonElement> arguments, Repository<Schema> repository, StatementExecution statementExecution) {

		return signature.getParameters()
		        .entrySet()
		        .stream()
		        .map(e ->
				{
			        Parameter param = e.getValue();
			        try {
				        return validateArgument(arguments, repository, e, param);
			        } catch (Exception sve) {

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

	private static Entry<String, JsonElement> validateArgument(final Map<String, JsonElement> arguments,
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

	private ArgumentsValidationUtil() {
	}
}
package com.fincity.nocode.kirun.engine.util.arguments;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.runtime.StatementExecution;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class ReactiveArgumentsValidationUtil {

	public static Mono<Map<String, JsonElement>> validateArguments(FunctionSignature signature,
	        final Map<String, JsonElement> arguments, ReactiveRepository<Schema> repository,
	        StatementExecution statementExecution) {

		return Flux.fromIterable(signature.getParameters()
		        .entrySet())
		        .flatMap(e -> validateArgument(arguments, repository, e, e.getValue())

		                .onErrorMap(sve ->
						{
			                String statementName = "Unknown Step";
			                if (statementExecution != null && statementExecution.getStatement() != null)
				                statementName = statementExecution.getStatement()
				                        .getStatementName();

			                throw new KIRuntimeException(
			                        "Error while executing the function " + signature.getNamespace() + "."
			                                + signature.getName() + "'s parameter " + e.getValue()
			                                        .getParameterName()
			                                + " with step name '" + statementName + "' with error : "
			                                + sve.getMessage(),
			                        sve);
		                }))
		        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private static Mono<Entry<String, JsonElement>> validateArgument(final Map<String, JsonElement> arguments,
	        ReactiveRepository<Schema> repository, Entry<String, Parameter> e, Parameter param) {

		JsonElement jsonElement = arguments.get(e.getKey());

		if ((jsonElement == null || jsonElement.isJsonNull()) && !param.isVariableArgument()) {
			return ReactiveSchemaValidator.validate(null, param.getSchema(), repository, jsonElement)
			        .map(element -> Map.entry(e.getKey(), element));
		}

		if (!param.isVariableArgument())
			return ReactiveSchemaValidator.validate(null, param.getSchema(), repository, jsonElement)
			        .map(element -> Map.entry(e.getKey(), element));

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

		JsonArray actualArray = array;

		return Flux.<Tuple2<Integer, JsonElement>>create(sink -> {

			for (int i = 0; i < actualArray.size(); i++)
				sink.next(Tuples.of(i, actualArray.get(i)));

			sink.complete();
		})
		        .flatMap(tup -> ReactiveSchemaValidator.validate(null, param.getSchema(), repository, tup.getT2())
		                .map(v -> tup.mapT2(x -> v)))
		        .collectList()
		        .map(tups ->
				{
			        JsonArray arr = new JsonArray(tups.size());

			        for (var tup : tups) {
				        arr.set(tup.getT1(), tup.getT2());
			        }

			        return Map.entry(e.getKey(), arr);
		        });

	}

	private ReactiveArgumentsValidationUtil() {
	}
}
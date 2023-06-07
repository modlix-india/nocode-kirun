package com.fincity.nocode.kirun.engine.runtime.reactive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.StatementExecution;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.ContextTokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.OutputMapTokenValueExtractor;
import com.google.gson.JsonElement;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
@ToString
public class ReactiveFunctionExecutionParameters {

	private Map<String, ContextElement> context;
	private Map<String, JsonElement> arguments;
	private Map<String, List<Map<String, JsonElement>>> events;
	private StatementExecution statementExecution;
	private Map<String, Map<String, Map<String, JsonElement>>> steps;
	private int count;
	private final ReactiveRepository<ReactiveFunction> functionRepository;
	private final ReactiveRepository<Schema> schemaRepository;
	private final String executionId;

	private HashMap<String, TokenValueExtractor> valueExtractors = new HashMap<>();

	public ReactiveFunctionExecutionParameters(ReactiveRepository<ReactiveFunction> functionRepository,
	        ReactiveRepository<Schema> schemaRepository) {
		this(functionRepository, schemaRepository, UUID.randomUUID()
		        .toString());
	}

	public ReactiveFunctionExecutionParameters setContext(Map<String, ContextElement> context) {

		this.context = context;
		var x = new ContextTokenValueExtractor(context);
		valueExtractors.put(x.getPrefix(), x);

		return this;
	}

	public ReactiveFunctionExecutionParameters setSteps(Map<String, Map<String, Map<String, JsonElement>>> steps) {

		this.steps = steps;
		var x = new OutputMapTokenValueExtractor(steps);
		valueExtractors.put(x.getPrefix(), x);

		return this;
	}

	public ReactiveFunctionExecutionParameters setArguments(Map<String, JsonElement> arguments) {

		this.arguments = arguments;
		return this;
	}

	public Map<String, TokenValueExtractor> getValuesMap() {
		return this.valueExtractors;
	}

	public ReactiveFunctionExecutionParameters addTokenValueExtractor(TokenValueExtractor... extractors) {

		for (TokenValueExtractor tve : extractors)
			this.valueExtractors.put(tve.getPrefix(), tve);
		return this;
	}

	public ReactiveFunctionExecutionParameters setValuesMap(Map<String, TokenValueExtractor> valuesMap) {
		this.valueExtractors.putAll(valuesMap);
		return this;
	}

	public Map<String, JsonElement> getArguments() {
		if (this.arguments == null)
			return Map.of();
		return this.arguments;
	}

	public FunctionExecutionParameters makeRegularFunctionExecutionParameters(Repository<Function> actualRepo,
	        Repository<Schema> actualSchemaRepo) {

		return new FunctionExecutionParameters(actualRepo, actualSchemaRepo, this.executionId).setContext(context)
		        .setArguments(arguments)
		        .setEvents(events)
		        .setStatementExecution(statementExecution)
		        .setSteps(steps)
		        .setCount(count)
		        .setValueExtractors(this.valueExtractors);
	}
}

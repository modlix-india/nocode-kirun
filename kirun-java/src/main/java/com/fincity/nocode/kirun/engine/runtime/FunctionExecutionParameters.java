package com.fincity.nocode.kirun.engine.runtime;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FunctionExecutionParameters {

	private Map<String, ContextElement> context;
	private Map<String, JsonElement> arguments;
	private Map<String, List<Map<String, JsonElement>>> events;
	private StatementExecution statementExecution;
	private Map<String, Map<String, Map<String, JsonElement>>> output;
	
	private int count;
}

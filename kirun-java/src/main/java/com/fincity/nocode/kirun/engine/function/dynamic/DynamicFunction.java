package com.fincity.nocode.kirun.engine.function.dynamic;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.model.Argument;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Result;
import com.google.gson.JsonElement;

public class DynamicFunction implements Function {

	private FunctionDefinition def;
	private Map<String, JsonElement> context;

	public DynamicFunction(FunctionDefinition def) {
		this.def = def;
	}

	@Override
	public FunctionSignature getSignature() {
		return def;
	}

	public void debug(List<Argument> arguments) {
		// to implement in future.
	}
	
	public void checkErrors() {
		
	}

	@Override
	public Result execute(List<Argument> arguments) {
		
		return null;
	}

}

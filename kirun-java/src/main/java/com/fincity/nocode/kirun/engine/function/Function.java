package com.fincity.nocode.kirun.engine.function;

import java.util.List;

import com.fincity.nocode.kirun.engine.model.Argument;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Result;

public interface Function {

	public FunctionSignature getSignature();
	public Result execute(List<Argument> arguments);
}
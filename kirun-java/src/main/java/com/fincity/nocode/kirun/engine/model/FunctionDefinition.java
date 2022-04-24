package com.fincity.nocode.kirun.engine.model;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FunctionDefinition extends FunctionSignature{

	private static final long serialVersionUID = 4891316472479078149L;

	private Map<String, Statement> steps;
	private Map<String, StatementFlow> flow;
}

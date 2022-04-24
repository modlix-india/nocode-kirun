package com.fincity.nocode.kirun.engine.model;

import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Statement {

	private String name;
	private String expression;
	private StatementType type;
	private Map<String, Object> properties;
}

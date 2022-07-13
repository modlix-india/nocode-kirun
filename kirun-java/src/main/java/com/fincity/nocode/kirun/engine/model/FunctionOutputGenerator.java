package com.fincity.nocode.kirun.engine.model;

@FunctionalInterface
public interface FunctionOutputGenerator {

	public EventResult next();
}

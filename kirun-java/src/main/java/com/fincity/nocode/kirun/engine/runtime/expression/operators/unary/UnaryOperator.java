package com.fincity.nocode.kirun.engine.runtime.expression.operators.unary;

import java.util.function.Function;

import com.google.gson.JsonElement;

@FunctionalInterface
public interface UnaryOperator extends Function<JsonElement, JsonElement> {

}

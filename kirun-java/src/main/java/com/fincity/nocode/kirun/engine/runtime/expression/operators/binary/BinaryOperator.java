package com.fincity.nocode.kirun.engine.runtime.expression.operators.binary;

import java.util.function.BiFunction;

import com.google.gson.JsonElement;

@FunctionalInterface
public interface BinaryOperator extends BiFunction<JsonElement, JsonElement, JsonElement> {

}

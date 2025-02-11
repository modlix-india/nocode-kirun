package com.fincity.nocode.kirun.engine.runtime.expression.operators.binary;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ArrayRangeOperator implements BinaryOperator {

    @Override
    public JsonElement apply(JsonElement t, JsonElement u) {

        if (t == null || t.isJsonNull()) {
            throw new ExecutionException("Cannot apply array range operator on a null value");
        }

        if (u == null || u.isJsonNull()) {
            throw new ExecutionException("Cannot apply array range operator with a null value");
        }

        String tStr = t.isJsonPrimitive() ? t.getAsString() : "";
        String uStr = u.isJsonPrimitive() ? u.getAsString() : "";

        
        return new JsonPrimitive(tStr + ".." + uStr);
    }
}
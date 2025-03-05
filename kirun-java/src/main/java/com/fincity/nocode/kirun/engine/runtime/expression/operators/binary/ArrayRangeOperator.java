package com.fincity.nocode.kirun.engine.runtime.expression.operators.binary;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ArrayRangeOperator implements BinaryOperator {

    @Override
    public JsonElement apply(JsonElement t, JsonElement u) {

        String tStr = t.isJsonPrimitive() ? t.getAsString() : "";
        String uStr = u.isJsonPrimitive() ? u.getAsString() : "";

        return new JsonPrimitive(tStr + ".." + uStr);
    }
}
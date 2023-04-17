package com.fincity.nocode.kirun.engine.function.system.object;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class ObjectKeys extends AbstractObjectFunction {

    private static final String SOURCE = "source";

    private static final String VALUE = "value";

    protected ObjectKeys() {
        super("ObjectKeys");
    }

    @Override
    protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

        var source = context.getArguments().get(SOURCE);
        JsonArray arr = new JsonArray();

        if (source == null || source.isJsonNull()
                || (source.isJsonPrimitive() && !((JsonPrimitive) source).isString()))

            return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, new JsonArray()))));

        else if (source.isJsonPrimitive() && ((JsonPrimitive) source).isString()) {
            String[] outputString = source.getAsString().split("");

            arr.addAll(this.generateObjectKeys(outputString.length));

            return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, arr))));
        }

        else if (source.isJsonArray()) {
            JsonArray inputArray = source.getAsJsonArray(); // taking input as array

            arr.addAll(this.generateObjectKeys(inputArray.size()));

            return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, arr))));

        }

        JsonObject jsonObject = source.getAsJsonObject();

        JsonObject parsed = JsonParser.parseString(jsonObject.toString()).getAsJsonObject();

        parsed.keySet().stream().forEach(arr::add);

        return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, arr))));

    }

    private JsonArray generateObjectKeys(int limit) {
        JsonArray keys = new JsonArray();
        int count = 0;
        while (count < limit) {
            keys.add(String.valueOf(count));
            count++;
        }
        return keys.deepCopy();
    }
}

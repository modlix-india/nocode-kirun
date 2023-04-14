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

public class ObjectValues extends AbstractObjectFunction {

    private static final String SOURCE = "source";

    private static final String VALUE = "value";

    public ObjectValues() {
        super("ObjectValues");
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
            for (int i = 0; i < outputString.length; i++) {
                arr.add(outputString[i]);
            }

            return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, arr))));
        }

        else if (source.isJsonArray()) {
            JsonArray inputArray = source.getAsJsonArray(); // taking input as array
            for (int i = 0; i < inputArray.size(); i++) {
                arr.add(inputArray.get(i).deepCopy());
            }
            return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, arr))));

        }

        JsonObject jsonObject = source.getAsJsonObject();

        JsonObject parsed = JsonParser.parseString(jsonObject.toString()).getAsJsonObject();

        parsed.entrySet().stream().forEach(e -> arr.add(e.getValue()));

        return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, arr))));

    }

}

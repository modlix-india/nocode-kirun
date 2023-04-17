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

public class ObjectEntries extends AbstractObjectFunction {

    private static final String SOURCE = "source";

    private static final String VALUE = "value";

    public ObjectEntries() {
        super("ObjectEntries");
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
                JsonArray tempArr = new JsonArray();
                tempArr.add(String.valueOf(i));
                tempArr.add(outputString[i]);
                arr.add(tempArr);
            }

            return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, arr))));
        }

        else if (source.isJsonArray()) {
            JsonArray inputArray = source.getAsJsonArray(); // taking input as array
            for (int i = 0; i < inputArray.size(); i++) {
                JsonArray tempArr = new JsonArray();
                tempArr.add(String.valueOf(i));
                tempArr.add(inputArray.get(i).deepCopy());
                arr.add(tempArr);
            }
            return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, arr))));

        }

        JsonObject jsonObject = source.getAsJsonObject();

        JsonObject parsed = JsonParser.parseString(jsonObject.toString()).getAsJsonObject();

        parsed.entrySet().stream().forEach(e -> {
            JsonArray tempArr = new JsonArray();
            tempArr.add(e.getKey());
            tempArr.add(e.getValue());
            arr.add(tempArr);
        });

        return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, arr))));
    }

}

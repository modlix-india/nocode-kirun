package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import reactor.core.publisher.Mono;

public class ArrayToArrayOfObjects extends AbstractArrayFunction {

    private static final String VALUE = "value";

    public static final String KEY_NAME = "keyName";

    public static final String ARRAY_TO_OBJECTS = "ArrayToArrayOfObjects";

    protected ArrayToArrayOfObjects() {
        super(
                ARRAY_TO_OBJECTS,

                List.of(PARAMETER_ARRAY_SOURCE, Parameter.of(KEY_NAME, Schema.ofString(KEY_NAME), true)),

                EVENT_RESULT_ARRAY);
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        var source = context.getArguments().get(PARAMETER_ARRAY_SOURCE.getParameterName()).getAsJsonArray();

        var keys = context.getArguments().get(KEY_NAME).getAsJsonArray();

        JsonArray arr = new JsonArray();

        if (source.size() == 0)

            return Mono.just(new FunctionOutput(
                    List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonArray())))));

        for (int i = 0; i < source.size(); i++) {
            JsonObject obj = new JsonObject();
            if (source.get(i).isJsonArray()) {
                extractForNestedArray(source, keys, i, obj);
            } else {
                obj.add(keys.size() > 0 ? keys.get(0).getAsString() : VALUE, source.get(i));
            }
            arr.add(obj);
        }

        return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, arr)))));
    }

    private void extractForNestedArray(JsonArray source, JsonArray keys, int i, JsonObject obj) {
        JsonArray innerArr = source.get(i).getAsJsonArray();
        if (keys.size() > 0) {
            for (int j = 0; j < keys.size() && j < innerArr.size(); j++) {
                obj.add(keys.get(j).getAsString(), innerArr.get(j));
            }
        } else {
            for (int j = 0; j < innerArr.size(); j++) {
                obj.add(VALUE + (j + 1), innerArr.get(j));
            }
        }
    }

}

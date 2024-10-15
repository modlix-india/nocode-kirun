package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.ObjectValueSetterExtractor;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class ArrayToObjects extends AbstractArrayFunction {

    protected static String KEY_PATH = "keyPath";
    protected static String VALUE_PATH = "valuePath";
    protected static String IGNORE_NULL_VALUES = "ignoreNullValues";
    protected static String IGNORE_NULL_KEYS = "ignoreNullKeys";
    protected static String IGNORE_DUPLICATE_KEYS = "ignoreDuplicateKeys";
    private static final String DATA = "Data.";

    public ArrayToObjects() {
        super("ArrayToObjects",

                List.of(

                        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                        Parameter.of(KEY_PATH, Schema.ofString(KEY_PATH)),
                        Parameter.of(VALUE_PATH, Schema.of(VALUE_PATH, SchemaType.STRING, SchemaType.NULL)),
                        Parameter.of(IGNORE_NULL_KEYS,
                                Schema.ofBoolean(IGNORE_NULL_KEYS).setDefaultValue(new JsonPrimitive(true))),
                        Parameter.of(IGNORE_NULL_VALUES,
                                Schema.ofBoolean(IGNORE_NULL_VALUES).setDefaultValue(new JsonPrimitive(false))),
                        Parameter.of(IGNORE_DUPLICATE_KEYS,
                                Schema.ofBoolean(IGNORE_DUPLICATE_KEYS).setDefaultValue(new JsonPrimitive(false)))),

                AbstractArrayFunction.EVENT_RESULT_ANY);
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        var source = context.getArguments().get(AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.getParameterName());

        var keyPath = context.getArguments().get(KEY_PATH).getAsString();
        var valuePath = context.getArguments().get(VALUE_PATH).isJsonNull() ? ""
                : context.getArguments().get(VALUE_PATH).getAsString();

        var ignoreNullValues = context.getArguments().get(IGNORE_NULL_VALUES).getAsBoolean();
        var ignoreNullKeys = context.getArguments().get(IGNORE_NULL_KEYS).getAsBoolean();
        var ignoreDuplicateKeys = context.getArguments().get(IGNORE_DUPLICATE_KEYS).getAsBoolean();

        if (source == null || source.isJsonNull()
                || (source.isJsonPrimitive() && !(source.getAsJsonPrimitive()).isString()))
            return Mono.just(
                    new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonArray())))));

        JsonArray sourceArr = source.getAsJsonArray();

        ObjectValueSetterExtractor ove = new ObjectValueSetterExtractor(new JsonObject(), "Data.");

        JsonObject result = updateArray(keyPath, valuePath, ignoreNullValues, ignoreNullKeys, ignoreDuplicateKeys,
                sourceArr, ove);

        return Mono.just(new FunctionOutput(
                List.of(EventResult.outputOf(Map.of(AbstractArrayFunction.EVENT_RESULT_NAME, result)))));
    }

    private JsonObject updateArray(String keyPath, String valuePath, boolean ignoreNullValues, boolean ignoreNullKeys,
            boolean ignoreDuplicateKeys, JsonArray sourceArr, ObjectValueSetterExtractor ove) {

        return StreamSupport.stream(sourceArr.spliterator(), false)
                .reduce(new JsonObject(), (a, curr) -> {
                    ove.setStore(curr);

                    JsonElement key = ove.getValue(DATA + keyPath);

                    if (ignoreNullKeys && (key == null || key.isJsonNull()))
                        return a;

                    JsonElement value = valuePath != null ? ove.getValue(DATA + valuePath) : curr;

                    if (ignoreNullValues && (value == null || value.isJsonNull()))
                        return a;

                    var result = a.getAsJsonObject();
                    if (ignoreDuplicateKeys && result.has(key.getAsString()))
                        return a;

                    result.add(key.getAsString(), value == null ? JsonNull.INSTANCE : value);

                    return a;
                }).getAsJsonObject();
    }

}

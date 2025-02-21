package com.fincity.nocode.kirun.engine.function.system;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.model.*;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.hash.HashUtil;
import com.google.gson.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class HashData extends AbstractReactiveFunction {

    private static final String DEFAULT_ALGORITHM = "SHA-256";
    public static final String PARAMETER_DATA = "data";
    public static final String PARAMETER_ALGORITHM = "algorithm";
    public static final String PARAMETER_PRIMITIVE_LEVEL = "primitiveLevel";
    public static final String EVENT_RESULT_NAME = "result";

    private final FunctionSignature signature;

    public HashData() {
        this.signature = createSignature();
    }

    private static FunctionSignature createSignature() {
        return new FunctionSignature()
                .setNamespace(Namespaces.SYSTEM)
                .setName("HashData")
                .setParameters(Map.of(
                        PARAMETER_DATA, new Parameter()
                                .setSchema(Schema.ofAny(PARAMETER_DATA)),
                        PARAMETER_ALGORITHM, new Parameter()
                                .setSchema(Schema.ofString(PARAMETER_ALGORITHM).setEnums(List.of(
                                                new JsonPrimitive("SHA-256"),
                                                new JsonPrimitive("SHA-384"),
                                                new JsonPrimitive("SHA-512"),
                                                new JsonPrimitive("MD5"),
                                                new JsonPrimitive("MD2"),
                                                new JsonPrimitive("MD4"),
                                                new JsonPrimitive("SHA-1")))
                                        .setDefaultValue(new JsonPrimitive(DEFAULT_ALGORITHM))),
                        PARAMETER_PRIMITIVE_LEVEL, new Parameter()
                                .setSchema(Schema.ofBoolean(PARAMETER_PRIMITIVE_LEVEL)
                                        .setDefaultValue(new JsonPrimitive(false)))
                ))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of())));
    }

    @Override
    public FunctionSignature getSignature() {
        return this.signature;
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        Map<String, JsonElement> args = context.getArguments();

        String algorithm = args.get(PARAMETER_ALGORITHM).getAsString();

        JsonElement data = args.get(PARAMETER_DATA);

        boolean primitiveLevel = args.get(PARAMETER_PRIMITIVE_LEVEL).getAsBoolean();

        JsonElement hashValue = processElement(data, algorithm, primitiveLevel);

        return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, hashValue)))));
    }

    private JsonElement processElement(JsonElement element, String algorithm, boolean primitiveLevel) {

        if (element.isJsonNull()) {
            return new JsonPrimitive("null");
        }
        if (!primitiveLevel) {
            return HashUtil.createHash(element.toString(), algorithm);
        }

        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                Number number = primitive.getAsNumber();
                return HashUtil.createHash(number.toString(), algorithm);
            } else if (primitive.isBoolean()) {
                return HashUtil.createHash(primitive.getAsBoolean(), algorithm);
            }
            return HashUtil.createHash(primitive.toString(), algorithm);
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            JsonArray hashedArray = new JsonArray();
            array.forEach(e -> hashedArray.add(processElement(e, algorithm, true)));
            return hashedArray;
        }

        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            JsonObject hashedObject = new JsonObject();
            object.entrySet()
                    .forEach(entry -> {
                        hashedObject.add(processElement(
                                        new JsonPrimitive(entry.getKey()), algorithm, true).getAsString(),
                                processElement(entry.getValue(), algorithm, true));
                    });
            return hashedObject;
        }

        return element;
    }

}
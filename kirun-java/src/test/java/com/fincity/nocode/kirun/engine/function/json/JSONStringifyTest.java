package com.fincity.nocode.kirun.engine.function.json;

import java.math.BigInteger;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.system.json.JSONStringify;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class JSONStringifyTest {

    @Test
    void testJSONStringify() {

        JSONStringify jsonStringify = new JSONStringify();

        var jsonArray = new JsonArray();
        jsonArray.add(new JsonPrimitive(BigInteger.valueOf(1l)));
        jsonArray.add(new JsonPrimitive(BigInteger.valueOf(2l)));

        var output = jsonStringify.execute(new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("source", jsonArray)).setContext(Map.of()).setSteps(Map.of()));

        StepVerifier.create(output.map(e -> e.next().getResult().get("value")))
                .expectNext(new JsonPrimitive("[1,2]")).verifyComplete();
    }

    @Test
    void testJSONStringifyWithNull() {
        JSONStringify jsonStringify = new JSONStringify();

        var output = jsonStringify.execute(new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("source", JsonNull.INSTANCE)).setContext(Map.of()).setSteps(Map.of()));

        StepVerifier.create(output.map(e -> e.next().getResult().get("value")))
                .expectNext(new JsonPrimitive("null")).verifyComplete();
    }

    @Test
    void testJSONStringifyWithString() {
        JSONStringify jsonStringify = new JSONStringify();

        var output = jsonStringify.execute(new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("source", new JsonPrimitive("test"))).setContext(Map.of()).setSteps(Map.of()));

        StepVerifier.create(output.map(e -> e.next().getResult().get("value")))
                .expectNext(new JsonPrimitive("\"test\"")).verifyComplete();
    }
}

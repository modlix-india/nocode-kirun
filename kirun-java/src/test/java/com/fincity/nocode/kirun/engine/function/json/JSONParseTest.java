package com.fincity.nocode.kirun.engine.function.json;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.system.json.JSONParse;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class JSONParseTest {

    @Test
    void testJSONParse() {
        JSONParse jsonParse = new JSONParse();

        var output = jsonParse.execute(new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("source", new JsonPrimitive("\"test\""))).setContext(Map.of()).setSteps(Map.of()));

        StepVerifier.create(output.map(e -> e.next().getResult().get("value")))
                .expectNext(new JsonPrimitive("test")).verifyComplete();
    }

    @Test
    void testJSONParseWithInvalidJSON() {
        JSONParse jsonParse = new JSONParse();

        var output = jsonParse.execute(new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("source", new JsonPrimitive("invalid JSON to parse"))).setContext(Map.of())
                .setSteps(Map.of()));

        StepVerifier.create(output.map(e -> e.next().getResult().get("errorMessage")))
                .expectNext(new JsonPrimitive(
                        "com.google.gson.stream.MalformedJsonException: Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 10 path $"))
                .verifyComplete();
    }

    @Test
    void testJSONParseWithNull() {
        JSONParse jsonParse = new JSONParse();

        var output = jsonParse.execute(new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("source", new JsonPrimitive("null"))).setContext(Map.of()).setSteps(Map.of()));

        StepVerifier.create(output.map(e -> e.next().getResult().get("value")))
                .expectNext(JsonNull.INSTANCE).verifyComplete();
    }
}

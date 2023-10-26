package com.fincity.nocode.kirun.engine.function.system;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class WaitTest {

    @Test
    void test() {
        Wait wait = new Wait();
        long time = 2000l;
        long start = System.currentTimeMillis();
        StepVerifier
                .create(wait.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
                        new KIRunReactiveSchemaRepository()).setArguments(Map.of("millis", new JsonPrimitive(time)))))
                .expectNextCount(1).verifyComplete();
        long totalTime = System.currentTimeMillis() - start;

        assert totalTime >= time;
    }

    @Test
    void test3000() {
        Wait wait = new Wait();
        long time = 3000l;
        long start = System.currentTimeMillis();
        StepVerifier
                .create(wait.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
                        new KIRunReactiveSchemaRepository()).setArguments(Map.of("millis", new JsonPrimitive(time)))))
                .expectNextCount(1).verifyComplete();
        long totalTime = System.currentTimeMillis() - start;

        assert totalTime >= time;
    }

    @Test
    void test0() {
        Wait wait = new Wait();
        long start = System.currentTimeMillis();
        StepVerifier
                .create(wait.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
                        new KIRunReactiveSchemaRepository()).setArguments(Map.of())))
                .expectNextCount(1).verifyComplete();
        long totalTime = System.currentTimeMillis() - start;

        assert totalTime <= 300l;
    }

    @Test
    void testNegative() {
        Wait wait = new Wait();
        long time = -3000l;
        StepVerifier
                .create(wait.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
                        new KIRunReactiveSchemaRepository()).setArguments(Map.of("millis", new JsonPrimitive(time)))))
                .expectError().verify(Duration.ofMillis(1000));
    }
}

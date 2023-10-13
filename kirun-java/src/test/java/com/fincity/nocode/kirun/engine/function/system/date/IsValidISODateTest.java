package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class IsValidISODateTest {

    IsValidISODate validDate = new IsValidISODate();

    @Test
    void test1() {

        ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("isodate", new JsonPrimitive("aws")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> !r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

    }

    @Test
    void test2() {

        ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("isodate", new JsonPrimitive("2023-10-04T11:45:38.939ss")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> !r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

    }

    @Test
    void test3() {

        ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("isodate", new JsonPrimitive("2023-10-04T11:45:38.939Z")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

    }

    @Test
    void test4() {

        ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("isodate", new JsonPrimitive("2023-10-10T10:02:54.959Z")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

    }

    @Test
    void test5() {

        ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("isodate", new JsonPrimitive("2023-10-10T10:02:54.959-12:12")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

    }

    @Test
    void test6() {

        ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("isodate", new JsonPrimitive("2023-10-10T10:02:54.959-34:12")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> !r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

    }

}

package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class FromNowTest {

    FromNow fn = new FromNow();
    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void test1() {

        rfep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-23T06:50:47.908Z")));

        StepVerifier.create(fn.execute(rfep))
                .expectNextMatches(r -> {

                    return r.next().getResult().get("difference").getAsString().equals("Several hours ago");
                })
                .verifyComplete();

    }

    @Test
    void test2() {

        rfep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-23T07:39:11.939Z")));

        StepVerifier.create(fn.execute(rfep))
                .expectNextMatches(r -> {

                    return r.next().getResult().get("difference").getAsString().equals("Several hours ago");
                })
                .verifyComplete();

    }

    @Test
    void test3() {

        rfep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-23T07:39:1.939Z")));

        StepVerifier.create(fn.execute(rfep))
                .expectError()
                .verify();

    }
}

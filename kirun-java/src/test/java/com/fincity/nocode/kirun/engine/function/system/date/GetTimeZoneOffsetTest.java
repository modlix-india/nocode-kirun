package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetTimeZoneOffsetTest {

    GetTimeZoneOffset gt = new GetTimeZoneOffset();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void test1() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-24T14:10:30.700+12:10")));

        StepVerifier.create(gt.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsInt() == -730)
                .verifyComplete();
    }

    @Test
    void test2() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-24T14:20:30Z")));

        StepVerifier.create(gt.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsInt() == 0)
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-24T14:20:30.798Z")));

        StepVerifier.create(gt.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsInt() == 0)
                .verifyComplete();

    }

    @Test
    void test3() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("1142-10-24T14:20:30.192-08:15")));

        StepVerifier.create(gt.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsInt() == 495)
                .verifyComplete();

    }

    @Test
    void test4() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("1142-11-30T14:20:30.192-06:09")));

        StepVerifier.create(gt.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsInt() == 369)
                .verifyComplete();

    }

}

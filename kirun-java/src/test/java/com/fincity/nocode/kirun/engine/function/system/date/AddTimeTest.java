package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class AddTimeTest {

    AddTime at = new AddTime();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void test() {

        rfep.setArguments(Map.of("isodate", new JsonPrimitive("1994-10-24T14:05:30.406+00:00"), "add",
                new JsonPrimitive(1020L), "timeunit",
                new JsonPrimitive("SECONDS")));

        StepVerifier.create(at.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("date").getAsString() == "1994-10-24T14:22s:30.406+00:00")
                .verifyComplete();

    }

    @Test
    void test2() {

        rfep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-04T11:45:38.939Z"), "add",
                new JsonPrimitive(100L), "timeunit",
                new JsonPrimitive("MINUTES")));

        StepVerifier.create(at.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("date").getAsString() == "2023-10-04T13:25:38.939Z")
                .verifyComplete();

    }

}

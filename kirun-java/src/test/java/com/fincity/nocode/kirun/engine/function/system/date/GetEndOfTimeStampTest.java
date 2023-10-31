package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetEndOfTimeStampTest {

    GetEndOfTimeStamp get = new GetEndOfTimeStamp();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void yearTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("year")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-12-31T23:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void weekTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-18T15:13:51.123Z"), "unit", new JsonPrimitive("week")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-21T23:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void quarterTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-18T15:13:51.123Z"), "unit", new JsonPrimitive("quarter")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-12-31T23:59:59.999Z"))
                .verifyComplete();
    }

}

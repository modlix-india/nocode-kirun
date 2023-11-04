package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class DateToEpochTest {

    DateToEpoch dte = new DateToEpoch();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void test1() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-21T16:11:50.978Z")));

        StepVerifier.create(dte.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("epoch").getAsLong() == 1697904710978L;
                })
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023")));

        StepVerifier.create(dte.execute(rfep))
                .expectError()
                .verify();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2507-08-07T11:41:50.000Z")));

        StepVerifier.create(dte.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("epoch").getAsLong() == 16964941310000L;
                })
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:13:51.000Z")));

        StepVerifier.create(dte.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("epoch").getAsLong() == 1696431000L;
                })
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive(true)));

        StepVerifier.create(dte.execute(rfep))
                .expectError()
                .verify();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2507-08-0T11:41:50.000+00.00")));

        StepVerifier.create(dte.execute(rfep))
                .expectError()
                .verify();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:13:51Z")));

        StepVerifier.create(dte.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("epoch").getAsLong() == 1696431000L;
                })
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:13:51.0Z")));

        StepVerifier.create(dte.execute(rfep))
                .expectError().verify();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:13:51.00Z")));

        StepVerifier.create(dte.execute(rfep))
                .expectError().verify();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:13:51.00+12:01")));

        StepVerifier.create(dte.execute(rfep))
                .expectError().verify();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:13:51.000+12:01")));

        StepVerifier.create(dte.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("epoch").getAsLong() == 1653171000L;
                })
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:13:51.000Z+12:01")));

        StepVerifier.create(dte.execute(rfep))
                .expectError().verify();
    }

}

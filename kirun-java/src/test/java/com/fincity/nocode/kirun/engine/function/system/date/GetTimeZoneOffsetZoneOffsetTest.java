package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetTimeZoneOffsetZoneOffsetTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void offsetFailTest() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTimeZoneOffset")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("abcd")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTimeZoneOffset")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-19T23:84:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE,
                "GetTimeZoneOffset")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

    }

    @Test
    void offsetSuccessTest1() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-04T11:45:38.939Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTimeZoneOffset")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("timeZoneOffset").getAsLong() == -330L)
                .verifyComplete();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("7765-04-20T14:48:20.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTimeZoneOffset")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("timeZoneOffset")
                                .getAsLong() == 182882069300000L)
                .verifyComplete();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("1994-10-24T02:10:30.700+00:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTimeZoneOffset")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("timeZoneOffset").getAsLong() == 782964630700L)
                .verifyComplete();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("7765-04-20T14:48:20.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTimeZoneOffset")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("timeZoneOffset").getAsLong() == 182882069300000L)
                .verifyComplete();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("1975-01-20T15:13:51.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTimeZoneOffset")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("timeZoneOffset").getAsLong() == 159506091200L)
                .verifyComplete();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("1979-11-30T12:13:51.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTimeZoneOffset")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "timeZoneOffset").getAsLong() == 312855291200L)
                .verifyComplete();

    }

}
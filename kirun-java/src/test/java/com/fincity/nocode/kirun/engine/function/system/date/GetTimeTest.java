package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetTimeTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    static final String message = "Invalid ISO 8601 Date format.";

    @Test
    void test() {
        fep.setArguments(Map.of("isoDate", new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T23:84:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE,
                "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-04T11:45:38.939Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsLong() == 1696419938939L)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("7765-04-20T14:48:20.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result")
                                .getAsLong() == 182882069300000L)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1994-10-24T02:10:30.700+00:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsLong() == 782964630700L)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("7765-04-20T14:48:20.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsLong() == 182882069300000L)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1975-01-20T15:13:51.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsLong() == 159506091200L)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1979-11-30T12:13:51.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsLong() == 312855291200L)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2016-02-31T12:13:56.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-02-29T12:13:41.189-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsLong() == 1709252081189L)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2028-02-29T12:13:49.200+02:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsLong() == 1835431969200L)
                .verifyComplete();
    }

}

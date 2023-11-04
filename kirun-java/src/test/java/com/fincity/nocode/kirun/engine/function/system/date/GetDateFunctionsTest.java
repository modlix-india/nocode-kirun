package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetDateFunctionsTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void getDateTest() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-07T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("date").getAsInt() == 7)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-12-31T07:35:17.111-12:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("date").getAsInt() == 1)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-07T17:35:17.123-11:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("date").getAsInt() == 8)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-32T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-7T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:34:57.561Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("date").getAsInt() == 20)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("date").getAsInt() == 19)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

    }

    @Test
    void getTimeTest() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T23:84:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE,
                "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
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

    }
}

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

    @Test
    void dateFailTest2() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

    }

    @Test
    void dateFailTest3() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("abcd")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

    }

    @Test
    void dateFailTest4() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-19T23:84:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE,
                "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

    }

    @Test
    void dateSuccessTest1() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-04T11:45:38.939Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("time").getAsLong() == 1696419938939L)
                .verifyComplete();

    }

    @Test
    void dateSuccessTest5() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("7765-04-20T14:48:20.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("time").getAsLong() == 182882069300000L)
                .verifyComplete();

    }

    @Test
    void dateSuccessTest2() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("1994-10-24T02:10:30.700+00:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("time").getAsLong() == 782964630700L)
                .verifyComplete();

    }

    @Test
    void dateSuccessTest4() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("7765-04-20T14:48:20.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetTime")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("time")
                                .getAsLong() == 182882069300000L)
                .verifyComplete();

    }
}

package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetMilliSecondsTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void dateSuccessTest1() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-09-07T17:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("milliSeconds").getAsInt() == 000)
                .verifyComplete();

    }

    @Test
    void dateFailTest1() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-09-7T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

    }

    @Test
    void dateSuccessTest2() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("1970-01-20T15:58:57.561Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("milliSeconds").getAsInt() == 561)
                .verifyComplete();

    }

    @Test
    void dateSuccessTest3() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "milliSeconds").getAsInt() == 615)
                .verifyComplete();

    }

    @Test
    void dateFailTest2() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

    }

    @Test
    void dateFailTest3() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("abcd")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

    }

    @Test
    void dateFailTest4() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-19T23:84:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

    }

    @Test
    void dateSuccessTest4() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("1994-10-24T14:05:30.406+00:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("milliSeconds").getAsInt() == 406)
                .verifyComplete();

    }

    @Test
    void dateSuccessTest5() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("1383-10-04T14:10:50.70000+00:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "milliSeconds").getAsInt() == 70000)
                .verifyComplete();

    }

    @Test
    void dateFailTest5() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("202312=12")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

    }
}

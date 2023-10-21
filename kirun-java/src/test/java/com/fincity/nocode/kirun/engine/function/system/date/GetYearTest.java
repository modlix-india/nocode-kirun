package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetYearTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void dateSuccessTest1() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-09-07T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("year").getAsInt() == 2023)
                .verifyComplete();

    }

    @Test
    void dateFailTest1() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2099-12-7T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE,
                "GetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

    }

    @Test
    void dateSuccessTest2() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2999-01-20T15:34:57.561Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("year").getAsInt() == 2999)
                .verifyComplete();

    }

    @Test
    void dateSuccessTest3() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("9999-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("year").getAsInt() == 9999)
                .verifyComplete();

    }

    @Test
    void dateFailTest2() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

    }

    @Test
    void dateFailTest3() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("abcd")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

    }

}

package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetMinutesTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void minutesSuccessTest1() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-09-07T17:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("minutes").getAsInt() == 5)
                .verifyComplete();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("minutes").getAsInt() == 14)
                .verifyComplete();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("1970-01-20T15:58:57.561-12:31")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("minutes").getAsInt() == 59)
                .verifyComplete();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2507-08-08T11:41:50.000+09:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("minutes").getAsInt() == 11)
                .verifyComplete();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("1970-01-20T15:13:51.001+12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("minutes").getAsInt() == 42)
                .verifyComplete();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("1970-01-20T15:13:51.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("minutes").getAsInt() == 44)
                .verifyComplete();

    }

    @Test
    void minutesFailTest1() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-09-7T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

        fep.setArguments(Map.of("isodate", new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-19T23:84:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();
    }

}

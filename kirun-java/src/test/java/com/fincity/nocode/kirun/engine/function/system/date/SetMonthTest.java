package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class SetMonthTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    String message = "Invalid ISO 8601 Date format.";

    @Test
    void failTest() {

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-09-7T07:35:17.000Z"), "monthValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-19T23:84:11.615Z"), "monthValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd"), "monthValue", new JsonPrimitive(121312)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();
    }

    @Test
    void validTest() {

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-04T11:45:38.939Z"), "monthValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-01-04T11:45:38.939Z"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-09-03T17:35:17.000Z"), "monthValue", new JsonPrimitive(18)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-07-03T17:35:17.000Z"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("1970-01-20T15:58:57.561Z"), "monthValue", new JsonPrimitive(31)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1972-08-20T15:58:57.561Z"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-19T06:44:11.615Z"), "monthValue", new JsonPrimitive(100)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2031-05-19T06:44:11.615Z"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-11-15T04:57:14.970Z"), "monthValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-01-15T04:57:14.970Z"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-11-15T04:57:14.970Z"), "monthValue", new JsonPrimitive(0)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-01-15T04:57:14.970Z"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-11-15T04:57:14.970Z"), "monthValue", new JsonPrimitive(-1)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2022-12-15T04:57:14.970Z"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-11-15T04:57:14.970Z"), "monthValue", new JsonPrimitive(-5)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2022-08-15T04:57:14.970Z"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("-012023-11-15T04:57:14.970Z"), "monthValue",
                        new JsonPrimitive(-13)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("-012025-12-15T04:57:14.970Z"))
                .verifyComplete();

    }

    @Test
    void test() {
        
        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-11-15T04:57:14.970Z"), "monthValue", new JsonPrimitive(-19)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2021-06-15T04:57:14.970Z"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-24T14:10:30.700+15:02"), "monthValue",
                        new JsonPrimitive(-100)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2014-09-24T14:10:30.700+15:02"))
                .verifyComplete();

    }

    @Test
    void leapTest() {

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-31T11:45:38.939Z"), "monthValue", new JsonPrimitive(2)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-03-31T11:45:38.939Z"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-31T11:45:38.939Z"), "monthValue",
                        new JsonPrimitive(1)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-03-03T11:45:38.939Z"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2024-12-29T11:45:38.939Z"), "monthValue",
                        new JsonPrimitive(0)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-01-29T11:45:38.939Z"))
                .verifyComplete();

    }
}

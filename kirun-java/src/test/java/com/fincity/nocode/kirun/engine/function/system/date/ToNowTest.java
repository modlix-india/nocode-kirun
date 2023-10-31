package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ToNowTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void toNowTest1() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-09-07T07:35:17.000Z"), "suffix",
                new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "ToNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("In 1 month"))
                .verifyComplete();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-11-01T07:35:17.000Z"), "suffix",
                new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "ToNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("a day ago"))
                .verifyComplete();

    }
    
    @Test
    void toNowTest2() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-25T19:30:04.970+01:30"), "suffix",
                new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "ToNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("In 5 days"))
                .verifyComplete();
    }
    
    @Test
    void toNowTest3() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2022-10-25T19:30:04.970+01:30"), "suffix",
                new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "ToNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("In a year"))
                .verifyComplete();
    }

    @Test
    void toNowTest4() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-09-25T19:30:04.970+01:30"), "suffix",
                new JsonPrimitive(true)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "ToNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("a month"))
                .verifyComplete();
    }
    
    @Test
    void toNowTest5() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-31T12:03:00.970+05:30"), "suffix",
                new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "ToNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("In a few seconds"))
                .verifyComplete();
    }
    
    @Test
    void toNowTest6() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("1990-10-31T12:03:00.970+05:30"), "suffix",
                new JsonPrimitive(true)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "ToNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("33 years"))
                .verifyComplete();
    }
    
    @Test
    void toNowTest7() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-12-31T12:03:00Z"), "suffix",
                new JsonPrimitive(true)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "ToNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("1 month"))
                .verifyComplete();
    }
    
    @Test
    void toNowTest8() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2024-10-31T12:03:00Z"), "suffix",
                new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "ToNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("a year ago"))
                .verifyComplete();
    }
}

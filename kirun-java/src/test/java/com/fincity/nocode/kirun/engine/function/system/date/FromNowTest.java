package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class FromNowTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void fromNowTest1() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-09-07T07:35:17.000Z"), "suffix",
                new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "FromNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("1 month ago"))
                .verifyComplete();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-11-01T07:35:17.000Z"), "suffix",
                new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "FromNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("In a day"))
                .verifyComplete();

    }
    
    @Test
    void fromNowTest2() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-25T19:30:04.970+01:30"), "suffix",
                new JsonPrimitive(true)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "FromNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("5 days"))
                .verifyComplete();
    
    }
    
    @Test
    void fromNowTest3() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2000-10-25T19:30:04.970+01:30"), "suffix",
                new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "FromNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("23 years ago"))
                .verifyComplete();
    
    }
    
    @Test
    void fromNowTest4() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2022-11-18T19:30:04.970+01:30"), "suffix",
                new JsonPrimitive(true)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "FromNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("a year"))
                .verifyComplete();
    
    }
    
    @Test
    void fromNowTest5() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-31T11:23:00.970+05:30"), "suffix",
                new JsonPrimitive(true)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "FromNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("a few seconds"))
                .verifyComplete();
    
    }
    
    
    @Test
    void fromNowTest6() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-31T19:30:04.970+01:30"), "suffix",
                new JsonPrimitive(true)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "FromNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("12 hours"))
                .verifyComplete();
    
    }
    
    
    
    @Test
    void fromNowTest7() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-31T11:30:00.970+05:30"), "suffix",
                new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "FromNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("8 minutes ago"))
                .verifyComplete();
    
    }
    
    
    @Test
    void fromNowTest8() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-31T11:47:00.970+05:30"), "suffix",
                new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "FromNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("In 7 minutes"))
                .verifyComplete();
    
    }
    
    @Test
    void fromNowTest9() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-11-02T11:47:00.970+05:30"), "suffix",
                new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "FromNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("In 2 days"))
                .verifyComplete();
    
    }
 
    @Test
    void fromNowTest10() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2024-11-02T11:47:00.970Z"), "suffix",
                new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "FromNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("In a year"))
                .verifyComplete();
    
    }
    
    @Test
    void fromNowTest11() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-11-31T11:47:00Z"), "suffix",
                new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "FromNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("In a month"))
                .verifyComplete();
    
    }
}

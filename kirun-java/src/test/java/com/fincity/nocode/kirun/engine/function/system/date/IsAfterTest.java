package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class IsAfterTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void yearTest() {

        JsonArray arr = new JsonArray();

        arr.add("years");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-31T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2023-10-31T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> !r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();
    }

    @Test
    void yearTest2() {

        JsonArray arr = new JsonArray();

        arr.add("years");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-31T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2000-10-31T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();
    }

    @Test
    void yearTest3() {

        JsonArray arr = new JsonArray();

        arr.add("years");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-31T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2024-10-31T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> !r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();
    }

    @Test
    void monthTest() {

        JsonArray arr = new JsonArray();

        arr.add("years");
        arr.add("months");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-31T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2000-09-30T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();
    }

    @Test
    void monthTest2() {

        JsonArray arr = new JsonArray();

        arr.add("years");
        arr.add("minutes");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-31T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2023-09-30T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> !r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();
    }

    @Test
    void dayTest3() {

        JsonArray arr = new JsonArray();

        arr.add("years");
        arr.add("days");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-31T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2025-10-31T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> !r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();
    }

    @Test
    void daytest1() {

        JsonArray arr = new JsonArray();

        arr.add("days");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-03T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2023-10-01T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();
    }

    @Test
    void daytest2() {

        JsonArray arr = new JsonArray();

        arr.add("days");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-03T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2023-10-03T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> !r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();
    }

    @Test
    void secondtest1() {

        JsonArray arr = new JsonArray();

        arr.add("days");
        arr.add("seconds");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-05T17:14:20.788Z"), "isoDate2",
                new JsonPrimitive("2023-10-05T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> !r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();
    }

    @Test
    void secondtest2() {

        JsonArray arr = new JsonArray();

        arr.add("seconds");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-05T17:14:20.788Z"), "isoDate2",
                new JsonPrimitive("2023-10-05T17:14:19.700Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();
    }

    @Test
    void minutetest1() {

        JsonArray arr = new JsonArray();

        arr.add("minutes");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-05T17:14:20.788Z"), "isoDate2",
                new JsonPrimitive("2023-10-05T17:14:20.800Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> !r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();
    }

    @Test
    void minutetest2() {

        JsonArray arr = new JsonArray();

        arr.add("minutes");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-05T17:14:20.788Z"), "isoDate2",
                new JsonPrimitive("2023-10-05T17:11:22.800Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();
    }

    @Test
    void hourtest() {

        JsonArray arr = new JsonArray();

        arr.add("hours");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-05T16:14:20.788Z"), "isoDate2",
                new JsonPrimitive("2023-10-05T12:17:22.800Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();
    }

    @Test
    void millisTest() {

        JsonArray arr = new JsonArray();

        arr.add("years");
        arr.add("millis");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-09-30T17:14:21.778+12:11"), "isoDate2",
                new JsonPrimitive("2023-10-31T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> !r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();
    }
}

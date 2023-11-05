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

class InBetweenTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void yearTest() {

        JsonArray arr = new JsonArray();

        arr.add("years");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2001-10-31T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2023-10-31T17:14:20.789Z"), "betweenDate",
                new JsonPrimitive("2010-10-31T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "InBetween").flatMap(e -> e.execute(rfep)))

                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean()).verifyComplete();
    }

    @Test
    void yearTest2() {

        JsonArray arr = new JsonArray();

        arr.add("years");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2001-10-31T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2023-10-31T17:14:20.789Z"), "betweenDate",
                new JsonPrimitive("2000-10-31T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "InBetween").flatMap(e -> e.execute(rfep)))

                .expectNextMatches(r -> !r.next().getResult().get("result").getAsBoolean()).verifyComplete();
    }

    @Test
    void yearTest3() {

        JsonArray arr = new JsonArray();

        arr.add("years");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2001-10-31T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2023-10-31T17:14:20.789Z"), "betweenDate",
                new JsonPrimitive("2001-10-31T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "InBetween").flatMap(e -> e.execute(rfep)))

                .expectNextMatches(r -> !r.next().getResult().get("result").getAsBoolean()).verifyComplete();
    }

    @Test
    void monthTest3() {

        JsonArray arr = new JsonArray();

        arr.add("months");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2001-08-14T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2001-11-18T17:14:20.789Z"), "betweenDate",
                new JsonPrimitive("2001-11-06T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "InBetween").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> !r.next().getResult().get("result").getAsBoolean()).verifyComplete();
    }

    @Test
    void monthTest4() {

        JsonArray arr = new JsonArray();

        arr.add("months");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2001-08-14T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2001-11-18T17:14:20.789Z"), "betweenDate",
                new JsonPrimitive("2001-09-06T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "InBetween").flatMap(e -> e.execute(rfep)))

                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean()).verifyComplete();
    }

    @Test
    void dateTest4() {

        JsonArray arr = new JsonArray();

        arr.add("days");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2001-11-14T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2001-11-18T17:14:20.789Z"), "betweenDate",
                new JsonPrimitive("2001-11-16T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "InBetween").flatMap(e -> e.execute(rfep)))

                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean()).verifyComplete();
    }

    @Test
    void dateTest5() {

        JsonArray arr = new JsonArray();

        arr.add("days");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2001-08-14T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2001-11-18T17:14:20.789Z"), "betweenDate",
                new JsonPrimitive("2001-08-01T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "InBetween").flatMap(e -> e.execute(rfep)))

                .expectNextMatches(r -> !r.next().getResult().get("result").getAsBoolean()).verifyComplete();
    }

    @Test
    void hourTest4() {

        JsonArray arr = new JsonArray();

        arr.add("hours");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2001-08-14T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2001-08-14T19:14:20.789Z"), "betweenDate",
                new JsonPrimitive("2001-08-14T18:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "InBetween").flatMap(e -> e.execute(rfep)))

                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean()).verifyComplete();
    }

    @Test
    void hourTest5() {

        JsonArray arr = new JsonArray();

        arr.add("hours");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2001-11-18T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2001-11-18T17:14:20.789Z"), "betweenDate",
                new JsonPrimitive("2001-09-06T17:14:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "InBetween").flatMap(e -> e.execute(rfep)))

                .expectNextMatches(r -> !r.next().getResult().get("result").getAsBoolean()).verifyComplete();
    }

    @Test
    void minuteTest4() {

        JsonArray arr = new JsonArray();

        arr.add("minutes");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2001-08-14T17:14:21.798Z"), "isoDate2",
                new JsonPrimitive("2001-08-14T17:17:20.789Z"), "betweenDate",
                new JsonPrimitive("2001-08-14T17:16:20.789Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "InBetween").flatMap(e -> e.execute(rfep)))

                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean()).verifyComplete();
    }

    @Test
    void secondTest4() {

        JsonArray arr = new JsonArray();

        arr.add("seconds");
        arr.add("millis");

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2001-08-14T17:14:21.700Z"), "isoDate2",
                new JsonPrimitive("2001-08-14T17:14:29.800Z"), "betweenDate",
                new JsonPrimitive("2001-08-14T17:14:22.775Z"), "unit", arr));

        StepVerifier.create(dfr.find(Namespaces.DATE, "InBetween").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean()).verifyComplete();
    }

}

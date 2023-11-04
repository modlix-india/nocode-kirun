package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class SubtractTimeTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void test() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("1994-10-24T14:05:30.406+00:00"), "subtract",
                new JsonPrimitive(1020), "unit",
                new JsonPrimitive("seconds")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1994-10-24T13:48:30.406Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.000Z"), "subtract",
                new JsonPrimitive(4), "unit",
                new JsonPrimitive("years")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2020-10-10T00:35:00.000Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.000Z"), "subtract",
                new JsonPrimitive(11), "unit",
                new JsonPrimitive("months")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-11-10T00:35:00.000Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.000Z"), "subtract",
                new JsonPrimitive(11), "unit",
                new JsonPrimitive("days")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-09-29T00:35:00.000Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.000Z"), "subtract",
                new JsonPrimitive(27), "unit",
                new JsonPrimitive("hours")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-10-08T21:35:00.000Z"))
                .verifyComplete();

    }

    @Test
    void test2() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.011Z"), "subtract",
                new JsonPrimitive(27), "unit",
                new JsonPrimitive("minutes")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-10-10T00:08:00.011Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-04T11:45:38.939Z"), "subtract",
                new JsonPrimitive(100), "unit",
                new JsonPrimitive("minutes")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-04T10:05:38.939Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.123Z"), "subtract",
                new JsonPrimitive(420), "unit",
                new JsonPrimitive("seconds")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-10-10T00:28:00.123Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.000Z"), "subtract",
                new JsonPrimitive(2040), "unit",
                new JsonPrimitive("millis")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-10-10T00:34:57.960Z"))
                .verifyComplete();

    }

    @Test
    void offsetTest() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.011+05:30"), "subtract",
                new JsonPrimitive(27), "unit",
                new JsonPrimitive("minutes")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-10-10T00:08:00.011+05:30"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-04T11:45:38.939+01:20"), "subtract",
                new JsonPrimitive(100), "unit",
                new JsonPrimitive("minutes")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-04T10:05:38.939+01:20"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.123-07:10"), "subtract",
                new JsonPrimitive(420), "unit",
                new JsonPrimitive("seconds")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-10-10T00:28:00.123-07:10"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.000+01:40"), "subtract",
                new JsonPrimitive(2040), "unit",
                new JsonPrimitive("millis")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get(
                                "result").getAsString().equals("2024-10-10T00:34:57.960+01:40"))
                .verifyComplete();

    }
}

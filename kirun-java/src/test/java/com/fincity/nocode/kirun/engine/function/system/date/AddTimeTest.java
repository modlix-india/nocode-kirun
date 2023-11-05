package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class AddTimeTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void yearTest() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-31T17:14:21.798Z"), "add",
                new JsonPrimitive(100L), "unit", new JsonPrimitive("years")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "AddTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2123-10-31T17:14:21.798Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("1994-10-24T14:05:30.406+00:00"), "add",
                new JsonPrimitive(1020L), "unit",
                new JsonPrimitive("seconds")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "AddTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1994-10-24T14:22:30.406Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.000+01:00"), "add",
                new JsonPrimitive(4L), "unit",
                new JsonPrimitive("years")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "AddTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2028-10-10T00:35:00.000+01:00"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00Z"), "add",
                new JsonPrimitive(4L), "unit",
                new JsonPrimitive("months")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "AddTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2025-02-10T00:35:00.000Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.000Z"), "add",
                new JsonPrimitive(4L), "unit",
                new JsonPrimitive("years")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "AddTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2028-10-10T00:35:00.000Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.000Z"), "add",
                new JsonPrimitive(11L), "unit",
                new JsonPrimitive("months")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "AddTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2025-09-10T00:35:00.000Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.000Z"), "add",
                new JsonPrimitive(11L), "unit",
                new JsonPrimitive("days")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "AddTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-10-21T00:35:00.000Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.000Z"), "add",
                new JsonPrimitive(27), "unit",
                new JsonPrimitive("hours")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "AddTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-10-11T03:35:00.000Z"))
                .verifyComplete();

    }

    @Test
    void addTest() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.011Z"), "add",
                new JsonPrimitive(27), "unit",
                new JsonPrimitive("minutes")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "AddTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-10-10T01:02:00.011Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-04T11:45:38.939Z"), "add",
                new JsonPrimitive(100), "unit",
                new JsonPrimitive("minutes")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "AddTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-04T13:25:38.939Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.123Z"), "add",
                new JsonPrimitive(420), "unit",
                new JsonPrimitive("seconds")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "AddTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-10-10T00:42:00.123Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.000Z"), "add",
                new JsonPrimitive(2040), "unit",
                new JsonPrimitive("millis")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "AddTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get(
                                "result").getAsString().equals("2024-10-10T00:35:02.040Z"))
                .verifyComplete();

    }

    @Test
    void offsetTest() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.011+05:30"), "add",
                new JsonPrimitive(27), "unit",
                new JsonPrimitive("minutes")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "AddTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-10-10T01:02:00.011+05:30"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-04T11:45:38.939+01:20"), "add",
                new JsonPrimitive(100), "unit",
                new JsonPrimitive("minutes")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "AddTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-04T13:25:38.939+01:20"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.123-07:10"), "add",
                new JsonPrimitive(420), "unit",
                new JsonPrimitive("seconds")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "AddTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2024-10-10T00:42:00.123-07:10"))
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-10-10T00:35:00.000Z"), "add",
                new JsonPrimitive(2040), "unit",
                new JsonPrimitive("millis")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "AddTime").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get(
                                "result").getAsString().equals("2024-10-10T00:35:02.040Z"))
                .verifyComplete();

    }

}

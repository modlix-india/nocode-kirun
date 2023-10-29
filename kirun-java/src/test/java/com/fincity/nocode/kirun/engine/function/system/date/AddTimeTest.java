package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class AddTimeTest {

    AddTime at = new AddTime();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void test() {

        rfep.setArguments(Map.of("isodate", new JsonPrimitive("1994-10-24T14:05:30.406+00:00"), "add",
                new JsonPrimitive(1020), "unit",
                new JsonPrimitive("SECONDS")));

        StepVerifier.create(at.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("dateTime").getAsString().equals("1994-10-24T14:22:30.406Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isodate", new JsonPrimitive("2024-10-10T00:35:00.000Z"), "add",
                new JsonPrimitive(4), "unit",
                new JsonPrimitive("YEARS")));

        StepVerifier.create(at.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("dateTime").getAsString().equals("2028-10-10T00:35:00.000Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isodate", new JsonPrimitive("2024-10-10T00:35:00.000Z"), "add",
                new JsonPrimitive(11), "unit",
                new JsonPrimitive("MONTHS")));

        StepVerifier.create(at.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("dateTime").getAsString().equals("2025-09-10T00:35:00.000Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isodate", new JsonPrimitive("2024-10-10T00:35:00.000Z"), "add",
                new JsonPrimitive(11), "unit",
                new JsonPrimitive("DAYS")));

        StepVerifier.create(at.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("dateTime").getAsString().equals("2024-10-21T00:35:00.000Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isodate", new JsonPrimitive("2024-10-10T00:35:00.000Z"), "add",
                new JsonPrimitive(27), "unit",
                new JsonPrimitive("HOURS")));

        StepVerifier.create(at.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("dateTime").getAsString().equals("2024-10-11T03:35:00.000Z"))
                .verifyComplete();

    }

    @Test
    void test2() {

        rfep.setArguments(Map.of("isodate", new JsonPrimitive("2024-10-10T00:35:00.011Z"), "add",
                new JsonPrimitive(27), "unit",
                new JsonPrimitive("MINUTES")));

        StepVerifier.create(at.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("dateTime").getAsString().equals("2024-10-10T01:02:00.011Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-04T11:45:38.939Z"), "add",
                new JsonPrimitive(100), "unit",
                new JsonPrimitive("MINUTES")));

        StepVerifier.create(at.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("dateTime").getAsString().equals("2023-10-04T13:25:38.939Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isodate", new JsonPrimitive("2024-10-10T00:35:00.123Z"), "add",
                new JsonPrimitive(420), "unit",
                new JsonPrimitive("SECONDS")));

        StepVerifier.create(at.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("dateTime").getAsString().equals("2024-10-10T00:42:00.123Z"))
                .verifyComplete();

        rfep.setArguments(Map.of("isodate", new JsonPrimitive("2024-10-10T00:35:00.000Z"), "add",
                new JsonPrimitive(2040), "unit",
                new JsonPrimitive("MILLIS")));

        StepVerifier.create(at.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("dateTime").getAsString().equals("2024-10-10T00:35:02.040Z"))
                .verifyComplete();

    }

}

package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetStartOfTimeStampTest {

    GetStartOfTimeStamp gst = new GetStartOfTimeStamp();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void yearTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("year")));

        StepVerifier.create(gst.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-01-01T00:00:00.000Z"))
                .verifyComplete();
    }

    @Test
    void monthTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("month")));

        StepVerifier.create(gst.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-01T00:00:00.000Z"))
                .verifyComplete();
    }

    @Test
    void quarterTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("quarter")));

        StepVerifier.create(gst.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-01T00:00:00.000Z"))
                .verifyComplete();
    }

    @Test
    void weekTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("week")));

        StepVerifier.create(gst.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-29T00:00:00.000Z"))
                .verifyComplete();
    }

    @Test
    void dateTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("date")));

        StepVerifier.create(gst.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T00:00:00.000Z"))
                .verifyComplete();
    }

    @Test
    void dayTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("day")));

        StepVerifier.create(gst.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T00:00:00.000Z"))
                .verifyComplete();
    }

    @Test
    void hourTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("hour")));

        StepVerifier.create(gst.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T15:00:00.000Z"))
                .verifyComplete();
    }

    @Test
    void minuteTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("minute")));

        StepVerifier.create(gst.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T15:13:00.000Z"))
                .verifyComplete();
    }

    @Test
    void secondTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("second")));

        StepVerifier.create(gst.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T15:13:51.000Z"))
                .verifyComplete();
    }

}

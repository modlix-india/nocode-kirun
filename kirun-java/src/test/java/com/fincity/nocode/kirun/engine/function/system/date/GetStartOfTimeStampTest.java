package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetStartOfTimeStampTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void yeartest() {

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("years")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-01-01T00:00:00.000Z"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("1999-09-31T00:00:00.000Z"), "unit", new JsonPrimitive("years")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1999-01-01T00:00:00.000Z"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("1999-09-31T00:00:00Z"), "unit", new JsonPrimitive("years")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1999-01-01T00:00:00.000Z"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("1999-09-31T00:00:00.000+09:00"), "unit",
                        new JsonPrimitive("years")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1999-01-01T00:00:00.000+09:00"))
                .verifyComplete();
    }

    @Test
    void monthTest() {

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("months")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-01T00:00:00.000Z"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-01-31T15:13:51.123+10:20"), "unit",
                        new JsonPrimitive("months")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-01-01T00:00:00.000+10:20"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-01-31T15:13:51.123-01:30"), "unit",
                        new JsonPrimitive("months")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-01-01T00:00:00.000-01:30"))
                .verifyComplete();
    }

    @Test
    void dateTest() {

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("days")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T00:00:00.000Z"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-30T15:13:51Z"), "unit", new JsonPrimitive("days")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-30T00:00:00.000Z"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("1989-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("days")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1989-10-31T00:00:00.000Z"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-31T15:13:51.123-12:18"), "unit",
                        new JsonPrimitive("days")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T00:00:00.000-12:18"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-30T15:13:51.123+14:00"), "unit",
                        new JsonPrimitive("days")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-30T00:00:00.000+14:00"))
                .verifyComplete();

    }

    @Test
    void hourTest() {

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("hours")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T15:00:00.000Z"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-31T22:13:51.123-12:45"), "unit",
                        new JsonPrimitive("hours")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T22:00:00.000-12:45"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2000-10-31T23:00:00.000Z"), "unit", new JsonPrimitive("hours")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2000-10-31T23:00:00.000Z"))
                .verifyComplete();
    }

    @Test
    void minuteTest() {
        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("minutes")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T15:13:00.000Z"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-31T22:13:51.123-12:45"), "unit",
                        new JsonPrimitive("minutes")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T22:13:00.000-12:45"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("1990-11-30T15:29:00.000Z"), "unit", new JsonPrimitive("minutes")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1990-11-30T15:29:00.000Z"))
                .verifyComplete();
    }

    @Test
    void secondTest() {

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("seconds")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T15:13:51.000Z"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2000-10-31T15:13:59.123+01:28"), "unit",
                        new JsonPrimitive("seconds")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2000-10-31T15:13:59.000+01:28"))
                .verifyComplete();
    }

    @Test
    void milliSecondTest() { // need to test

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("millis")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T15:13:51.000Z"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2000-10-31T15:13:59.123+01:28"), "unit",
                        new JsonPrimitive("millis")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetStartOfTimeStamp").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2000-10-31T15:13:59.000+01:28"))
                .verifyComplete();
    }

}

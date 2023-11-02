package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetEndOfTimeStampTest {

    GetEndOfTimeStamp get = new GetEndOfTimeStamp();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void yearTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("year")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-12-31T23:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void weekTest1() {

        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-18T15:13:51.123Z"), "unit", new JsonPrimitive("week")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-21T23:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void quarterTest1() {

        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-18T15:13:51.123Z"), "unit", new JsonPrimitive("quarter")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-12-31T23:59:59.999Z"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-05-18T15:13:51.123Z"), "unit", new JsonPrimitive("quarter")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-06-30T23:59:59.999Z"))
                .verifyComplete();

        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-07-18T15:13:51.123Z"), "unit", new JsonPrimitive("quarter")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-09-30T23:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void dateTest() {

        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-18T15:13:51.123Z"), "unit", new JsonPrimitive("date")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-18T23:59:59.999Z"))
                .verifyComplete();
    }
    
    @Test
    void yearTest1() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("year")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-12-31T23:59:59.999Z"))
                .verifyComplete();
    }
    
    @Test
    void yearTest2() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("1999-09-31T00:00:00.000Z"), "unit", new JsonPrimitive("year")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1999-12-31T23:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void yearTest3() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("1999-09-31T00:00:00Z"), "unit", new JsonPrimitive("year")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1999-12-31T23:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void weekTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-18T15:13:51.123Z"), "unit", new JsonPrimitive("week")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-21T23:59:59.999Z"))
                .verifyComplete();
    }
    
    @Test
    void weekTest2() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-28T15:13:51.123Z"), "unit", new JsonPrimitive("week")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-28T23:59:59.999Z"))
                .verifyComplete();
    }
    

    @Test
    void weekTest3() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-27T15:13:51.123Z"), "unit", new JsonPrimitive("week")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-28T23:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void quarterTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-18T15:13:51.123Z"), "unit", new JsonPrimitive("quarter")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-12-31T23:59:59.999Z"))
                .verifyComplete();
    }


    @Test
    void quarterTest2() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-07-18T15:13:51.123Z"), "unit", new JsonPrimitive("quarter")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-09-30T23:59:59.999Z"))
                .verifyComplete();
    }
    
    @Test
    void quarterTest3() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-05-18T15:13:51.123Z"), "unit", new JsonPrimitive("quarter")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-06-30T23:59:59.999Z"))
                .verifyComplete();
    }
    
    @Test
    void monthTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("month")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T23:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void monthTest2() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-11-30T15:13:51.123Z"), "unit", new JsonPrimitive("month")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-11-30T23:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void monthTest3() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-02-31T15:13:51.123Z"), "unit", new JsonPrimitive("month")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-02-28T23:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void dateTest1() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("date")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T23:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void dateTest2() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-30T15:13:51Z"), "unit", new JsonPrimitive("date")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-30T23:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void dateTest3() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("1989-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("date")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1989-10-31T23:59:59.999Z"))
                .verifyComplete();
    }

    
    @Test
    void dayTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("day")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T23:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void dayTest2() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-30T15:13:51.123+14:00"), "unit",
                        new JsonPrimitive("day")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-30T23:59:59.999Z"))
                .verifyComplete();
    }
    
    @Test
    void hourTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("hour")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T15:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void hourTest2() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T22:13:51.123Z"), "unit", new JsonPrimitive("hour")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T22:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void hourTest3() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2000-10-31T23:00:00.000Z"), "unit", new JsonPrimitive("hour")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2000-10-31T23:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void minuteTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("minute")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T15:13:59.999Z"))
                .verifyComplete();
    }

    @Test
    void minuteTest2() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("1990-11-29T15:59:51.123Z"), "unit", new JsonPrimitive("minute")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1990-11-29T15:59:59.999Z"))
                .verifyComplete();
    }

    @Test
    void minuteTest3() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("1990-11-30T15:29:00.000Z"), "unit", new JsonPrimitive("minute")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1990-11-30T15:29:59.999Z"))
                .verifyComplete();
    }

    @Test
    void secondTest() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-31T15:13:51.123Z"), "unit", new JsonPrimitive("second")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-31T15:13:51.999Z"))
                .verifyComplete();
    }

    @Test
    void secondTest2() {
        rfep.setArguments(
                Map.of("isodate", new JsonPrimitive("2000-10-31T15:13:59.000Z"), "unit", new JsonPrimitive("second")));

        StepVerifier.create(get.execute(rfep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2000-10-31T15:13:59.999Z"))
                .verifyComplete();
    }
}

    
    
    
   

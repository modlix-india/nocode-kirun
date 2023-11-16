package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class SetMilliSecondTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    String message = "Invalid ISO 8601 Date format.";

    @Test
    void failTest() {

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-09-7T07:35:17.000Z"), "milliValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-19T23:84:11.615Z"), "milliValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd"), "milliValue", new JsonPrimitive(121312)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-09-07T07:35:17.000Z"), "milliValue",
                        new JsonPrimitive(1212)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Milliseconds should be in the range of 0 and 999")
                .verify();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-09-07T07:35:17.000Z"), "milliValue",
                        new JsonPrimitive(-12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Milliseconds should be in the range of 0 and 999")
                .verify();
    }

    @Test
    void validTest() {

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-09-03T17:35:17.980Z"), "milliValue",
                        new JsonPrimitive(100)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-09-03T17:35:17.100Z"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-24T14:10:30.700+12:00"), "milliValue",
                        new JsonPrimitive(100)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-24T14:10:30.100+12:00"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-09-07T07:35:17.000+12:00"), "milliValue",
                        new JsonPrimitive(100)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString()
                                .equals("2023-09-07T07:35:17.100+12:00"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("1994-10-24T14:05:30.406-18:00"), "milliValue",
                        new JsonPrimitive(546)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString()
                                .equals("1994-10-24T14:05:30.546-18:00"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("-121300-10-25T05:42:10.435+14:00"), "milliValue",
                        new JsonPrimitive(123)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString()
                                .equals("-121300-10-25T05:42:10.123+14:00"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("+001300-10-25T05:42:10.435+14:00"), "milliValue",
                        new JsonPrimitive(456)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString()
                                .equals("+001300-10-25T05:42:10.456+14:00"))
                .verifyComplete();
    }
}

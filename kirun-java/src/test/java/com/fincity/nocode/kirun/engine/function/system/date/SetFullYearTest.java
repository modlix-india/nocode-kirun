package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class SetFullYearTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    String message = "Invalid ISO 8601 Date format.";

//    @Test
    void failTest() {

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-09-7T07:35:17.000Z"), "yearValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-19T23:84:11.615Z"), "yearValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd"), "yearValue", new JsonPrimitive(121312)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

    }

    @Test
    void yearTest() {

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-19T23:54:11.615-02:10"), "yearValue",
                        new JsonPrimitive(2)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("0002-10-19T23:54:11.615-02:10"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-09-07T17:35:17.123Z"), "yearValue", new JsonPrimitive(8347)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("8347-09-07T17:35:17.123Z"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-09-03T17:35:17.999Z"), "yearValue", new JsonPrimitive(1209)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result")
                                .getAsString().equals("1209-09-03T17:35:17.999Z"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("1970-01-20T15:58:57.561Z"), "yearValue", new JsonPrimitive(1997)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("1997-01-20T15:58:57.561Z"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-12-29T06:44:11.615Z"), "yearValue", new JsonPrimitive(2030)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsString().equals("2030-12-29T06:44:11.615Z"))
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-24T14:10:30.700+12:00"), "yearValue",
                new JsonPrimitive(9999)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsString().equals("9999-10-24T14:10:30.700+12:00"))
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1994-10-24T14:05:30.406-18:00"), "yearValue",
                new JsonPrimitive(10012)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString()
                                .equals("+010012-10-24T14:05:30.406-18:00"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("1300-10-25T05:42:10.435+14:00"), "yearValue",
                        new JsonPrimitive(971)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsString()
                                .equals("0971-10-25T05:42:10.435+14:00"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("1200-12-31T05:42:10.435+14:00"), "yearValue",
                        new JsonPrimitive(-27576)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsString()
                                .equals("-027576-12-31T05:42:10.435+14:00"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-24T14:10:30.700+12:00"), "yearValue",
                        new JsonPrimitive(10000)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsString()
                                .equals("+010000-10-24T14:10:30.700+12:00"))
                .verifyComplete();

    }

}

package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class SetDateFunctionsTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void setYearFailTest() {

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-09-7T07:35:17.000Z"), "yearValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-19T23:84:11.615Z"), "yearValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd"), "yearValue", new JsonPrimitive(121312)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("202312=12"), "yearValue", new JsonPrimitive(1245)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2053-10-04T14:10:50.70000+00:00"), "yearValue",
                new JsonPrimitive(123)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-19T23:84:11.615Z"), "yearValue", new JsonPrimitive(1)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

    }

    @Test
    void setYearSuccessTest() {

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-09-07T17:35:17.123Z"), "yearValue", new JsonPrimitive(8347)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 8347)
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-09-03T17:35:17.999Z"), "yearValue", new JsonPrimitive(1209)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result")
                                .getAsInt() == 1209)
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("1970-01-20T15:58:57.561Z"), "yearValue", new JsonPrimitive(1997)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 1997)
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-12-31T06:44:11.615Z"), "yearValue", new JsonPrimitive(2030)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsInt() == 2030)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-24T14:10:30.700+12:00"), "yearValue",
                new JsonPrimitive(9999)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsInt() == 9999)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1994-10-24T14:05:30.406-18:00"), "yearValue",
                new JsonPrimitive(8575)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 8575)
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("1300-10-25T05:42:10.435+14:00"), "yearValue",
                        new JsonPrimitive(1997)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsInt() == 1997)
                .verifyComplete();

    }

    @Test
    void setDateSuccessTest() {

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-09-07T17:35:17.000Z"), "dateValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 12)
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-09-03T17:35:17.000Z"), "dateValue", new JsonPrimitive(18)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result")
                                .getAsInt() == 18)
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("1970-01-20T15:58:57.561Z"), "dateValue", new JsonPrimitive(31)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 31)
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-19T06:44:11.615Z"), "dateValue", new JsonPrimitive(32)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsInt() == 1)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-24T14:10:30.700+12:00"), "dateValue",
                new JsonPrimitive(40)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsInt() == 9)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1994-10-24T14:05:30.406-18:00"), "dateValue",
                new JsonPrimitive(76)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 15)
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("1300-10-25T05:42:10.435+14:00"), "dateValue",
                        new JsonPrimitive(130)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsInt() == 7)
                .verifyComplete();

    }

    @Test
    void setDateFailTest() {

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-09-7T07:35:17.000Z"), "dateValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-19T23:84:11.615Z"), "dateValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd"), "dateValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("202312=12"), "dateValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2053-10-04T14:10:50.70000+00:00"), "dateValue",
                new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-19T23:84:11.615Z"), "dateValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

    }

}

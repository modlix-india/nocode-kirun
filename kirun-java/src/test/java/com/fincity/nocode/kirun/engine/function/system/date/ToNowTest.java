package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ToNowTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void toNowTest1() {

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-09-07T07:35:17.000Z"), "suffix",
                new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "ToNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsString().equals("In 1 month"))
                .verifyComplete();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-11-01T07:35:17.000Z"), "suffix",
                new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "ToNow")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").equals("a day ago"))
                .verifyComplete();

    }

}

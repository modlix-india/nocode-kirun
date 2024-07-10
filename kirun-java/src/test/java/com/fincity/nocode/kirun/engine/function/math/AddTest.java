package com.fincity.nocode.kirun.engine.function.math;

import java.math.BigInteger;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.system.math.Add;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class AddTest {

    @Test
    void testAdd() {
        Add add = new Add();

        var jsonArray = new JsonArray();
        jsonArray.add(new JsonPrimitive(BigInteger.valueOf(1l)));
        jsonArray.add(new JsonPrimitive(BigInteger.valueOf(2l)));

        var output = add.execute(new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("value", jsonArray)).setContext(Map.of()).setSteps(Map.of()));

        StepVerifier.create(output.map(e -> e.next().getResult().get("value")))
                .expectNext(new JsonPrimitive(BigInteger.valueOf(3l))).verifyComplete();
    }
}

package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class RemoveDuplicatesTest {

    @Test
    void simpleDuplicatesTest() {

        RemoveDuplicates remDup = new RemoveDuplicates();

        JsonArray ja = new JsonArray();
        ja.add(2);
        ja.add(2);
        ja.add(2);
        ja.add(2);
        ja.add(2);
        ja.add(2);

        JsonArray res = new JsonArray();
        res.add(2);
        res.add(2);
        res.add(2);

        ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(RemoveDuplicates.PARAMETER_ARRAY_SOURCE.getParameterName(), ja,
                        RemoveDuplicates.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(2),
                        RemoveDuplicates.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(4)))
                .setContext(Map.of())
                .setSteps(Map.of());

        StepVerifier.create(remDup.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("result").equals(res);
                }).verifyComplete();

        ja.add(100);

        res.add(2);
        res.add(100);

        rfep.setArguments(Map.of(RemoveDuplicates.PARAMETER_ARRAY_SOURCE.getParameterName(), ja,
                RemoveDuplicates.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(2),
                RemoveDuplicates.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(3)));

        StepVerifier.create(remDup.execute(rfep))
                .expectNextMatches(r -> {
                     return r.next().getResult().get("result").equals(res);
                }).verifyComplete();

    }

    @Test
    void EmptyArrayDuplicatesTest() {
        RemoveDuplicates remDup = new RemoveDuplicates();

        JsonArray ja = new JsonArray();

        ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(RemoveDuplicates.PARAMETER_ARRAY_SOURCE.getParameterName(), ja,
                        RemoveDuplicates.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(2),
                        RemoveDuplicates.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(4)));

        StepVerifier.create(remDup.execute(rfep))
                .expectError().verify();
    }

    @Test
    void arrayOfObjectsDuplicateTest() {

        RemoveDuplicates remDup = new RemoveDuplicates();

        JsonObject jo = new JsonObject();
        jo.addProperty("firstName", "surendar");

        JsonObject jo2 = new JsonObject();
        jo2.addProperty("lastname", "s");

        JsonArray ja = new JsonArray();
        ja.add(jo);
        ja.add(jo);
        ja.add(2);
        ja.add(jo2);
        ja.add(jo2);

        JsonArray res = new JsonArray();
        res.add(jo);
        res.add(2);
        res.add(jo2);

        ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(RemoveDuplicates.PARAMETER_ARRAY_SOURCE.getParameterName(), ja));
       
        StepVerifier.create(remDup.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("result").equals(res);
                }).verifyComplete();

    }
}

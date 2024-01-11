package com.fincity.nocode.kirun.engine.function.system;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class IfTest {

    @Test
    void test() {
        If ifFunction = new If();

        ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository());

        StepVerifier
                .create(ifFunction.execute(fep.setArguments(Map.of("condition", new JsonPrimitive(true))))
                        .map(e -> e.allResults().get(0).getName()))
                .expectNext("true")
                .verifyComplete();

        StepVerifier
                .create(ifFunction.execute(fep.setArguments(Map.of("condition", new JsonPrimitive(false))))
                        .map(e -> e.allResults().get(0).getName()))
                .expectNext("false")
                .verifyComplete();

        StepVerifier
                .create(ifFunction.execute(fep.setArguments(Map.of("condition", JsonNull.INSTANCE)))
                        .map(e -> e.allResults().get(0).getName()))
                .expectNext("false")
                .verifyComplete();

        StepVerifier
                .create(ifFunction.execute(fep.setArguments(Map.of("condition", new JsonPrimitive(0))))
                        .map(e -> e.allResults().get(0).getName()))
                .expectNext("false")
                .verifyComplete();

        StepVerifier
                .create(ifFunction.execute(fep.setArguments(Map.of("condition", new JsonPrimitive(1))))
                        .map(e -> e.allResults().get(0).getName()))
                .expectNext("true")
                .verifyComplete();

        StepVerifier
                .create(ifFunction.execute(fep.setArguments(Map.of("condition", new JsonPrimitive(-1))))
                        .map(e -> e.allResults().get(0).getName()))
                .expectNext("true")
                .verifyComplete();

        StepVerifier
                .create(ifFunction.execute(fep.setArguments(Map.of("condition", new JsonPrimitive(""))))
                        .map(e -> e.allResults().get(0).getName()))
                .expectNext("true")
                .verifyComplete();

        StepVerifier
                .create(ifFunction.execute(fep.setArguments(Map.of("condition", new JsonPrimitive(" "))))
                        .map(e -> e.allResults().get(0).getName()))
                .expectNext("true")
                .verifyComplete();

        StepVerifier
                .create(ifFunction.execute(fep.setArguments(Map.of("condition", new JsonPrimitive("abc"))))
                        .map(e -> e.allResults().get(0).getName()))
                .expectNext("true")
                .verifyComplete();

        StepVerifier
                .create(ifFunction.execute(fep.setArguments(Map.of("condition", new JsonObject())))
                        .map(e -> e.allResults().get(0).getName()))
                .expectNext("true")
                .verifyComplete();

        StepVerifier
                .create(ifFunction.execute(fep.setArguments(Map.of("condition", new JsonArray())))
                        .map(e -> e.allResults().get(0).getName()))
                .expectNext("true")
                .verifyComplete();
    }
}

package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

public class DifferenceOfTimestampsTest {

    DifferenceOfTimestamps dts = new DifferenceOfTimestamps();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void invalidTest(){

        fep.setArguments(Map.of("isoDateOne", new JsonPrimitive("2029-05-95T06:04:18.073Z"),
        "isoDateTwo", new JsonPrimitive("2029-05-05T06:04:18.073Z")));

        StepVerifier.create(dts.execute(fep))
                .expectError().verify();

        fep.setArguments(Map.of("isoDateOne", new JsonPrimitive("2029-05-05T06:04:18.073Z")));

        StepVerifier.create(dts.execute(fep))
                .expectError().verify();
    }

    @Test
	void test() {

		fep.setArguments(Map.of("isoDateOne", new JsonPrimitive("2024-09-13T23:52:34.633-05:30"),
        "isoDateTwo", new JsonPrimitive("2024-09-13T23:52:34.633Z")));

        StepVerifier.create(dts.execute(fep))
                .expectNextMatches(res -> res.next()
                .getResult()
                .get("result")
                .getAsInt() == -330)
                .verifyComplete();

	}

	@Test
	void test1() {

		fep.setArguments(Map.of("isoDateOne", new JsonPrimitive("2024-09-13T23:52:34.633-05:30"),
        "isoDateTwo", new JsonPrimitive("2024-09-12T23:52:34.633Z")));

        StepVerifier.create(dts.execute(fep))
            .expectNextMatches(res -> res.next()
            .getResult()
            .get("result")
            .getAsInt() == -1770)
            .verifyComplete();

	}

    @Test
    void test3() {

        fep.setArguments(Map.of("isoDateOne", new JsonPrimitive("2023-09-12T23:52:34.633Z"),
        "isoDateTwo", new JsonPrimitive("2024-09-13T23:52:34.633-05:30")));


        StepVerifier.create(dts.execute(fep))
            .expectNextMatches(res -> res.next()
            .getResult()
            .get("result")
            .getAsInt() == 528810)
            .verifyComplete();
    }
    
}

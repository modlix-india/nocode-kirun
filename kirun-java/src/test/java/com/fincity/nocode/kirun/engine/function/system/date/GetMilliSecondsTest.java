package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

public class GetMilliSecondsTest {
    
    DateFunctionRepository dfr = new DateFunctionRepository();

	ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
	        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void testInvalidDate() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2029-05-95T06:04:18.073Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectError().verify();


        fep.setArguments(Map.of("isoDate", new JsonPrimitive(2029)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectError().verify();
        
    }



	@Test
	void test() {

		fep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-09-01T23:52:53.126-05:30")));

		StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
		        .flatMap(e -> e.execute(fep)))
                .expectNextMatches(res -> res.next()
                .getResult()
                .get("result")
                .getAsInt() ==126)
                .verifyComplete();

	}

	@Test
	void test1() {

		fep.setArguments(Map.of("isoDate", new JsonPrimitive("2019-11-13T00:52:34.633Z")));

		StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
		        .flatMap(e -> e.execute(fep)))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsInt() == 633)
		        .verifyComplete();

	}

    @Test
    void test3() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-12-31T07:59:17.543-12:00")));

		StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
		        .flatMap(e -> e.execute(fep)))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsInt() == 543)
		        .verifyComplete();
    }
}

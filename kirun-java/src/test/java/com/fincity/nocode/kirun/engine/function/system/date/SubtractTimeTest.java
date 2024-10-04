package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

public class SubtractTimeTest {
    
	DateFunctionRepository dfr = new DateFunctionRepository();

	ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
	        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());


    @Test
    void testInvalidDate(){


        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2029-05-95T06:04:18.073Z") ,
        "intValue", new JsonPrimitive(10), "unit", new JsonPrimitive("MILLISECOND")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime")
                .flatMap(e -> e.execute(fep)))
                .expectError().verify();


        fep.setArguments(Map.of("isoDate", new JsonPrimitive(false),
        "intValue", new JsonPrimitive(10), "unit", new JsonPrimitive("MILLISECOND")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime")
                .flatMap(e -> e.execute(fep)))
                .expectError().verify();
    }

    @Test   
    void testSubtractTime1(){

		fep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-09-13T23:52:34.633-05:30"),
        "intValue", new JsonPrimitive(10), "unit", new JsonPrimitive("MINUTE")));

	
        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(res -> res.next()
                .getResult()
                .get("result")
                .getAsString().equals("2024-09-13T23:42:34.633-05:30"))
                .verifyComplete();

    }

    @Test
    void testSubtractTime2(){

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-09-13T23:52:34.633-05:30"),
        "intValue", new JsonPrimitive(13), "unit", new JsonPrimitive("MONTH")));

	
        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(res -> res.next()
                .getResult()
                .get("result")
                .getAsString().equals("2023-08-13T23:52:34.633-05:30"))
                .verifyComplete();

    }

    @Test
    void testSubtractTime3(){

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-09-13T23:52:34.633-05:30"),
        "intValue", new JsonPrimitive(70), "unit", new JsonPrimitive("SECOND")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(res -> res.next()
                .getResult()
                .get("result")
                .getAsString().equals("2024-09-13T23:51:24.633-05:30"))
                .verifyComplete();
        
    }

    @Test
    void testSubtractTime4(){


        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-09-13T23:52:34.633-05:30"),
        "intValue", new JsonPrimitive(5), "unit", new JsonPrimitive("YEAR")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SubtractTime")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(res -> res.next()
                .getResult()
                .get("result")
                .getAsString().equals("2019-09-13T23:52:34.633-05:30"))
                .verifyComplete();

    }

}

package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class IsAfterTest {

	 DateFunctionRepository dfr = new DateFunctionRepository();

	    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
	            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());
	    @Test
	    void yearTest() {

	        JsonArray arr = new JsonArray();

	        arr.add("year");
	      
	        rfep.setArguments(Map.of("dateone", new JsonPrimitive("2023-10-31T17:14:21.798Z"), "datetwo",
	                new JsonPrimitive("2023-10-31T17:14:20.789Z"), "unit", arr));

	        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
	                .expectNextMatches(r -> !r.next().getResult().get("result").getAsBoolean())
	                .verifyComplete();
	    }
	    
	    @Test
	    void yearTest2() {

	        JsonArray arr = new JsonArray();

	        arr.add("year");
	      
	        rfep.setArguments(Map.of("dateone", new JsonPrimitive("2023-10-31T17:14:21.798Z"), "datetwo",
	                new JsonPrimitive("2000-10-31T17:14:20.789Z"), "unit", arr));

	        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
	                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean())
	                .verifyComplete();
	    }
	    
	    @Test
	    void yearTest3() {

	        JsonArray arr = new JsonArray();

	        arr.add("year");
	      
	        rfep.setArguments(Map.of("dateone", new JsonPrimitive("2023-10-31T17:14:21.798Z"), "datetwo",
	                new JsonPrimitive("2024-10-31T17:14:20.789Z"), "unit", arr));

	        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
	                .expectNextMatches(r -> !r.next().getResult().get("result").getAsBoolean())
	                .verifyComplete();
	    }
	    
	    @Test
	    void monthTest() {

	        JsonArray arr = new JsonArray();

	        arr.add("year");
	        arr.add("month");

	        rfep.setArguments(Map.of("dateone", new JsonPrimitive("2023-10-31T17:14:21.798Z"), "datetwo",
	                new JsonPrimitive("2000-09-30T17:14:20.789Z"), "unit", arr));

	        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
	                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean())
	                .verifyComplete();
	    }
	    
	    @Test
	    void monthTest2() {

	        JsonArray arr = new JsonArray();

	        arr.add("year");
	        arr.add("month");

	        rfep.setArguments(Map.of("dateone", new JsonPrimitive("2023-10-31T17:14:21.798Z"), "datetwo",
	                new JsonPrimitive("2023-09-30T17:14:20.789Z"), "unit", arr));

	        StepVerifier.create(dfr.find(Namespaces.DATE, "IsAfter").flatMap(e -> e.execute(rfep)))
	                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean())
	                .verifyComplete();
	    }
	    
}

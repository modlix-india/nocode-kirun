package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class IsDstTest {
 
	  IsDst isDSTFunction = new IsDst();
	  
	  ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
	            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());


	 @Test
	    void test1() {

	        ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
	                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
	                .setArguments(Map.of("date", new JsonPrimitive("2023-09-07T17:35:17.000Z")));

	        StepVerifier.create(isDSTFunction.execute(rfep))
	                .expectNextMatches(r -> !r.next().getResult().get("output").getAsBoolean())
	                .verifyComplete();

	    }
	 @Test
	    void test2() {

	        ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
	                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
	                .setArguments(Map.of("date", new JsonPrimitive("2023-06-15T12:00:00.000-04:00")));

	        StepVerifier.create(isDSTFunction.execute(rfep))
	                .expectNextMatches(r -> r.next().getResult().get("output").getAsBoolean())
	                .verifyComplete();

	    }
	 

	 @Test
	    void test3() {

	        ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
	                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
	                .setArguments(Map.of("date", new JsonPrimitive("2023-10-15T12:00:00.000-12:00")));

	        StepVerifier.create(isDSTFunction.execute(rfep))
	                .expectNextMatches(r -> !r.next().getResult().get("output").getAsBoolean())
	                .verifyComplete();

	    }
	 

	 
}


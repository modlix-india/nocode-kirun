package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetLocalTimeTest {


	    GetLocalTime getlocaltime = new GetLocalTime();
	    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
	            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());
	    
	    
	    
	    
	    @Test
	    void test() {

	        rfep.setArguments(Map.of("isodate", new JsonPrimitive("1994-10-24T02:10:30.700+05:00")));

	        StepVerifier.create(getlocaltime.execute(rfep))
	                .expectNextMatches(
	                        r -> r.allResults().get(0).getResult().get("localtime").getAsString().equals("1994-10-24T02:40:30.700"))
	                .verifyComplete();

	    }
	    
	    
	    @Test
	    void test1() {

	        rfep.setArguments(Map.of("isodate", new JsonPrimitive("1994-10-24T02:10:30.700Z")));

	        StepVerifier.create(getlocaltime.execute(rfep))
	                .expectNextMatches(
	                        r -> r.allResults().get(0).getResult().get("localtime").getAsString().equals("1994-10-24T07:40:30.700"))
	                .verifyComplete();

	    }
	    
	    
	    @Test
	    void test2() {

	        rfep.setArguments(Map.of("isodate", new JsonPrimitive("1994-10-24T02:10:30.000Z")));

	        StepVerifier.create(getlocaltime.execute(rfep))
	                .expectNextMatches(
	                        r -> r.allResults().get(0).getResult().get("localtime").getAsString().equals("1994-10-24T07:40:30.000"))
	                .verifyComplete();

	    }
	    
	    @Test
	    void test3() {

	        rfep.setArguments(Map.of("isodate", new JsonPrimitive("1994-10-24T02:10:30.000-08:00")));

	        StepVerifier.create(getlocaltime.execute(rfep))
	                .expectNextMatches(
	                        r -> r.allResults().get(0).getResult().get("localtime").getAsString().equals("1994-10-24T15:40:30.000"))
	                .verifyComplete();

	    }
	    
}

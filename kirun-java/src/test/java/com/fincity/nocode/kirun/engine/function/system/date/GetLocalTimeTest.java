package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetLocalTimeTest {

	GetLocalTime glt=new GetLocalTime();
	

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

	    @Test
    void test1() {
        fep.setArguments(Map.of("isodate", new JsonPrimitive("1994-10-24T02:10:30.000Z")));
        StepVerifier.create(glt.execute(fep))
                .expectNextMatches(
                        r -> r.allResults().get(0).getResult().get("localtime").getAsString().equals("1994-10-24T07:40:30+05:30"))
                .verifyComplete();
    }
    
	    @Test
	    void test2() {
	        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-24T09:10:30.000+09:00")));
	        StepVerifier.create(glt.execute(fep))
	                .expectNextMatches(
	                        r -> r.allResults().get(0).getResult().get("localtime").getAsString().equals("2023-10-24T05:40:30+05:30"))
	                .verifyComplete();
	    }
	    

	    @Test
	    void test3() {
	        fep.setArguments(Map.of("isodate", new JsonPrimitive("2000-10-24T09:10:30.010-09:00")));
	        StepVerifier.create(glt.execute(fep))
	                .expectNextMatches(
	                        r -> r.allResults().get(0).getResult().get("localtime").getAsString().equals("2000-10-24T23:40:30.010+05:30"))
	                .verifyComplete();
	    }
	    
	

	    @Test
	    void test4() {
	        fep.setArguments(Map.of("isodate", new JsonPrimitive("2016-05-03T22:15:01.678+02:00")));
	        StepVerifier.create(glt.execute(fep))
	                .expectNextMatches(
	                        r -> r.allResults().get(0).getResult().get("localtime").getAsString().equals("2016-05-04T01:45:01.678+05:30"))
	                .verifyComplete();
	    }
	    
	
}

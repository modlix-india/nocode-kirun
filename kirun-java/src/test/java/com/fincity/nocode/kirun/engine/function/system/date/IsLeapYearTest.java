package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class IsLeapYearTest {
	 DateFunctionRepository dfr = new DateFunctionRepository();

	    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
	            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());
	   
	    
	    @Test
	    void test() {

	        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-09-07T07:35:17.000Z")));

	        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear")
	                .flatMap(e -> e.execute(fep)))
	                .expectNextMatches(r -> !r.next().getResult().get("leap").getAsBoolean())
	                .verifyComplete();
 
	   }
	    
	    @Test
	    void test1() {

	        fep.setArguments(Map.of("isodate", new JsonPrimitive("2020-09-07T07:35:17.000Z")));

	        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear")
	                .flatMap(e -> e.execute(fep)))
	                .expectNextMatches(r -> r.next().getResult().get("leap").getAsBoolean())
	                .verifyComplete();
	    }
 
	        @Test
		    void test2() {

		        fep.setArguments(Map.of("isodate", new JsonPrimitive("2020-12-31T23:35:17.000Z")));

		        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear")
		                .flatMap(e -> e.execute(fep)))
		                .expectNextMatches(r -> !r.next().getResult().get("leap").getAsBoolean())
		                .verifyComplete();
	        }
		        
		        @Test
			    void test3() {

			        fep.setArguments(Map.of("isodate", new JsonPrimitive("1990-09-07T07:35:17.000+11:00")));

			        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear")
			                .flatMap(e -> e.execute(fep)))
			                .expectNextMatches(r -> !r.next().getResult().get("leap").getAsBoolean())
			                .verifyComplete();
		 
}
		        @Test
			    void test4() {

			        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-12-31T20:35:17.000Z")));

			        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear")
			                .flatMap(e -> e.execute(fep)))
			                .expectNextMatches(r -> r.next().getResult().get("leap").getAsBoolean())
			                .verifyComplete();
		 
}
		        @Test
			    void test5() {

			        fep.setArguments(Map.of("isodate", new JsonPrimitive("1300-09-07T07:35:17.000+05:30")));

			        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear")
			                .flatMap(e -> e.execute(fep)))
			                .expectNextMatches(r -> !r.next().getResult().get("leap").getAsBoolean())
			                .verifyComplete();
		 
		        
}
		        @Test
			    void test6() {

			        fep.setArguments(Map.of("isodate", new JsonPrimitive("2000-09-07T07:35:17Z")));

			        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear")
			                .flatMap(e -> e.execute(fep)))
			                .expectNextMatches(r -> r.next().getResult().get("leap").getAsBoolean())
			                .verifyComplete();
		 }
		        }
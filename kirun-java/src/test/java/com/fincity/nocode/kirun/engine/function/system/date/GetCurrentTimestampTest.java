package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;

import reactor.test.StepVerifier;

class GetCurrentTimestampTest {

	GetCurrentTimestamp gct = new GetCurrentTimestamp();
	ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
	        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

	@Test
	void testEqualTimestamp() {

		StepVerifier.create(gct.execute(rfep))
		        .expectNextMatches(r ->
				{
			        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			        String currentTime = formatter.format(java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC));
			        String resultTime = r.next()
			                .getResult()
			                .get("time")
			                .getAsString();
			        return resultTime.substring(0, 21)
			                .equals(currentTime.substring(0, 21));
		        })
		        .verifyComplete();
	}

	@Test
	void testUnequalTimestamp() {

		String pastTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
		        .format(java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
		                .minusSeconds(1));

		StepVerifier.create(gct.execute(rfep))
		        .expectNextMatches(r ->
				{
			        String resultTime = r.next()
			                .getResult()
			                .get("time")
			                .getAsString();
			        return !resultTime.equals(pastTime);
		        })
		        .verifyComplete();
	}

}

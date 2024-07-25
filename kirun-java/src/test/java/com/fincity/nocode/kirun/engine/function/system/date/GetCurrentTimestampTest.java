package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.format.DateTimeFormatter;
import java.util.Date;

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
	void test() {

		DateTimeFormatter sdf = DateTimeFormatter.ISO_INSTANT;

//		String date = sdf.format(Instant.now().toEpochMilli());

		Date date = new Date();

		System.out.println(date);

		StepVerifier.create(gct.execute(rfep))
		        .expectNextMatches(r ->
				{
			        return r.next()
			                .getResult()
			                .get("time")
			                .getAsString()
			                .equals(date);
		        })
		        .verifyComplete();
	}

}

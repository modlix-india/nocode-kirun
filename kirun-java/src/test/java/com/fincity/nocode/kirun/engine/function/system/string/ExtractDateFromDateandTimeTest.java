package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ExtractDateFromDateandTimeTest {

	@Test
	void test() {
		
		String dateandtime = "2002-11-08T12:10:14";
		
		ExtractDateFromDateandTime extractdate = new ExtractDateFromDateandTime();
		
		StepVerifier.create(extractdate
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(ExtractDateFromDateandTime.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
				.map(fo -> fo.allResults().get(0).getResult().get(ExtractDateFromDateandTime.EVENT_RESULT_NAME)
						.getAsString()))
		.expectNext("2002-11-08")
		.verifyComplete();
		
	}
		
		
		
		
		
		
		
		
	}
	


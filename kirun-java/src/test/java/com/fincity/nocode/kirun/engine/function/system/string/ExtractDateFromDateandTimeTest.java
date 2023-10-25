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
		
		String dateandtime = "2002-11-08T12:10:14Z";
		
		ExtractDateFromDateandTime extractdate = new ExtractDateFromDateandTime();
		
		StepVerifier.create(extractdate
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(ExtractDateFromDateandTime.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
				.map(fo -> fo.allResults().get(0).getResult().get(ExtractDateFromDateandTime.EVENT_RESULT_NAME)
						.getAsString()))
		.expectNext("8")
		.verifyComplete();
		
	}
		
	@Test
	void test1() {
		
		String dateandtime = "2002-11-08T12:10:14+11:00";
		
		ExtractDateFromDateandTime extractdate = new ExtractDateFromDateandTime();
		
		StepVerifier.create(extractdate
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(ExtractDateFromDateandTime.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
				.map(fo -> fo.allResults().get(0).getResult().get(ExtractDateFromDateandTime.EVENT_RESULT_NAME)
						.getAsString()))
		.expectNext("8")
		.verifyComplete();
		
	}
		
		
	@Test
	void test2() {
		
		String dateandtime = "2000-08-14T12:10:14Z";
		
		ExtractDateFromDateandTime extractdate = new ExtractDateFromDateandTime();
		
		StepVerifier.create(extractdate
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(ExtractDateFromDateandTime.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
				.map(fo -> fo.allResults().get(0).getResult().get(ExtractDateFromDateandTime.EVENT_RESULT_NAME)
						.getAsString()))
		.expectNext("14")
		.verifyComplete();
		
	}
		
	@Test
	void test3() {
		
		String dateandtime = "2000-08-01T12:10:14Z";
		
		ExtractDateFromDateandTime extractdate = new ExtractDateFromDateandTime();
		
		StepVerifier.create(extractdate
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of(ExtractDateFromDateandTime.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
				.map(fo -> fo.allResults().get(0).getResult().get(ExtractDateFromDateandTime.EVENT_RESULT_NAME)
						.getAsString()))
		.expectNext("1")
		.verifyComplete();
		
	}
		
		
	}
	


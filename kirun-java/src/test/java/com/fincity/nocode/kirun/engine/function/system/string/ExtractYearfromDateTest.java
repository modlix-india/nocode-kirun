package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ExtractYearfromDateTest {
	
	 @Test
		void test() {
			
			String dateandtime = "2023-November-18T12:10:14";
			
			ExtractYearfromDate extractday = new ExtractYearfromDate();
			
			StepVerifier.create(extractday
					.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
							new KIRunReactiveSchemaRepository())
							.setArguments(Map.of(ExtractDayfromDate.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
					.map(fo -> fo.allResults().get(0).getResult().get(ExtractDayfromDate.EVENT_RESULT_NAME)
							.getAsString()))
			.expectNext("2023")
			.verifyComplete();
			
		}
			


@Test
void test2() {
	
	String dateandtime = "2023-11-18T12:10:14";
	
	ExtractYearfromDate extractday = new ExtractYearfromDate();
	
	StepVerifier.create(extractday
			.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
					new KIRunReactiveSchemaRepository())
					.setArguments(Map.of(ExtractDayfromDate.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
			.map(fo -> fo.allResults().get(0).getResult().get(ExtractDayfromDate.EVENT_RESULT_NAME)
					.getAsString()))
	.expectNext("2023")
	.verifyComplete();
	
}
	


@Test
void test3() {
	
	String dateandtime = "11-18-2023";
	
	ExtractYearfromDate extractday = new ExtractYearfromDate();
	
	StepVerifier.create(extractday
			.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
					new KIRunReactiveSchemaRepository())
					.setArguments(Map.of(ExtractDayfromDate.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
			.map(fo -> fo.allResults().get(0).getResult().get(ExtractDayfromDate.EVENT_RESULT_NAME)
					.getAsString()))
	.expectNext("2023")
	.verifyComplete();
	
}
	
}
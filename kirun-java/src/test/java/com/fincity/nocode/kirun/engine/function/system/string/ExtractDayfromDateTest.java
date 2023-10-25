package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ExtractDayfromDateTest {
	 @Test
		void test() {
			
			String dateandtime = "1300-10-25T05:42:10.435Z";
			
			ExtractDayfromDate extractday = new ExtractDayfromDate();
			
			StepVerifier.create(extractday
					.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
							new KIRunReactiveSchemaRepository())
							.setArguments(Map.of(ExtractDayfromDate.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
					.map(fo -> fo.allResults().get(0).getResult().get(ExtractDayfromDate.EVENT_RESULT_NAME)
							.getAsString()))
			.expectNext("1")
			.verifyComplete();
			
		}
	 
	 @Test
		void test1() {
			
			String dateandtime = "2023-11-18T06:50:47.687+00:11";
			
			ExtractDayfromDate extractday = new ExtractDayfromDate();
			
			StepVerifier.create(extractday
					.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
							new KIRunReactiveSchemaRepository())
							.setArguments(Map.of(ExtractDayfromDate.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
					.map(fo -> fo.allResults().get(0).getResult().get(ExtractDayfromDate.EVENT_RESULT_NAME)
							.getAsString()))
			.expectNext("6")
			.verifyComplete();
			
		}
			



@Test
void test2() {
	
	String dateandtime = "0002-12-31T05:42:10.435Z";
	
	ExtractDayfromDate extractday = new ExtractDayfromDate();
	
	StepVerifier.create(extractday
			.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
					new KIRunReactiveSchemaRepository())
					.setArguments(Map.of(ExtractDayfromDate.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
			.map(fo -> fo.allResults().get(0).getResult().get(ExtractDayfromDate.EVENT_RESULT_NAME)
					.getAsString()))
	.expectNext("2")
	.verifyComplete();
	
}


@Test
void test3() {
	
	String dateandtime = "2023-11-17T05:42:11.435Z";
	
	ExtractDayfromDate extractday = new ExtractDayfromDate();
	
	StepVerifier.create(extractday
			.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
					new KIRunReactiveSchemaRepository())
					.setArguments(Map.of(ExtractDayfromDate.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
			.map(fo -> fo.allResults().get(0).getResult().get(ExtractDayfromDate.EVENT_RESULT_NAME)
					.getAsString()))
	.expectNext("5")
	.verifyComplete();
	
}

	
@Test
void test4() {
	
	String dateandtime = "2023-11-16T05:42:12.435+00:10";
	
	ExtractDayfromDate extractday = new ExtractDayfromDate();
	
	StepVerifier.create(extractday
			.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
					new KIRunReactiveSchemaRepository())
					.setArguments(Map.of(ExtractDayfromDate.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
			.map(fo -> fo.allResults().get(0).getResult().get(ExtractDayfromDate.EVENT_RESULT_NAME)
					.getAsString()))
	.expectNext("4")
	.verifyComplete();
	
}


}
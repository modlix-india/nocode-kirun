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
			
			String dateandtime = "2023-11-18T12:10:14";
			
			ExtractDayfromDate extractday = new ExtractDayfromDate();
			
			StepVerifier.create(extractday
					.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
							new KIRunReactiveSchemaRepository())
							.setArguments(Map.of(ExtractDayfromDate.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
					.map(fo -> fo.allResults().get(0).getResult().get(ExtractDayfromDate.EVENT_RESULT_NAME)
							.getAsString()))
			.expectNext("SATURDAY")
			.verifyComplete();
			
		}
			
}

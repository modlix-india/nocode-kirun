package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetTimezoneTest {

	@Test
	void test() {
		String dateandtime = "2023-10-25T15:30:00+11:00";
		GetTimezone extracttimezone = new GetTimezone();

		StepVerifier.create(extracttimezone.execute(new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(ExtractDateFromDateandTime.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
				.map(fo -> fo.allResults().get(0).getResult().get(ExtractDateFromDateandTime.EVENT_RESULT_NAME)
						.getAsString()))
				.expectNext("+11:00").verifyComplete();

	}

	@Test
	void tes2() {
		String dateandtime = "2023-10-25T15:30:00+05:00";
		GetTimezone extracttimezone = new GetTimezone();

		StepVerifier.create(extracttimezone.execute(new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(ExtractDateFromDateandTime.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
				.map(fo -> fo.allResults().get(0).getResult().get(ExtractDateFromDateandTime.EVENT_RESULT_NAME)
						.getAsString()))
				.expectNext("+05:00").verifyComplete();

	}

	@Test
	void tes3() {
		String dateandtime = "2023-10-25T15:30:00.23112Z";
		GetTimezone extracttimezone = new GetTimezone();

		StepVerifier.create(extracttimezone.execute(new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(ExtractDateFromDateandTime.PARAMETER_STRING_NAME, new JsonPrimitive(dateandtime))))
				.map(fo -> fo.allResults().get(0).getResult().get(ExtractDateFromDateandTime.EVENT_RESULT_NAME)
						.getAsString()))
				.expectNext("UTC").verifyComplete();

	}
}

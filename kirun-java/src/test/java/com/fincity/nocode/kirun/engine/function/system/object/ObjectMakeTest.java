package com.fincity.nocode.kirun.engine.function.system.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.ArgumentsTokenValueExtractor;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ObjectMakeTest {

	@Test
	void testConvertStringToPhoneNumberWithCountryCode() {
		String phoneNumberString = "+918293840192";
		JsonPrimitive source = new JsonPrimitive(phoneNumberString);

		JsonObject resultStruct = new JsonObject();
		resultStruct.add("number", new JsonPrimitive("{{Arguments.source}}"));
		resultStruct.add("countryCode", new JsonPrimitive("{{Arguments.source[0..3]}}"));

		ObjectMake objectMake = new ObjectMake();

		ArgumentsTokenValueExtractor argumentsExtractor = new ArgumentsTokenValueExtractor(
				Map.of("source", source, "resultStruct", resultStruct));

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(
						"source", source,
						"resultStruct", resultStruct))
				.addTokenValueExtractor(argumentsExtractor)
				.setContext(Map.of())
				.setSteps(Map.of());

		StepVerifier.create(objectMake.execute(fep)
				.map(output -> output.allResults().getFirst().getResult().get("value")))
				.expectNextMatches(result -> {
					assertNotNull(result);
					assertTrue(result.isJsonObject());
					JsonObject phoneNumberObj = result.getAsJsonObject();

					assertTrue(phoneNumberObj.has("number"));
					assertEquals(phoneNumberString, phoneNumberObj.get("number").getAsString());

					assertTrue(phoneNumberObj.has("countryCode"));
					String extractedCountryCode = phoneNumberObj.get("countryCode").getAsString();
					assertTrue(extractedCountryCode.length() <= 3);

					System.out.println(result);
					return true;
				})
				.verifyComplete();
	}

}

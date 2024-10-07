package com.fincity.nocode.kirun.engine.function.system.object;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.convertor.enums.ConversionMode;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ObjectConvertTest {

	@Test
	void testBooleanArrayConversion() {
		JsonArray booleanArray = new JsonArray();
		booleanArray.add(new JsonPrimitive(true));
		booleanArray.add(new JsonPrimitive(false));
		booleanArray.add(new JsonPrimitive("yes"));
		booleanArray.add(new JsonPrimitive("no"));
		booleanArray.add(new JsonPrimitive("y"));
		booleanArray.add(new JsonPrimitive("n"));
		booleanArray.add(new JsonPrimitive(1));
		booleanArray.add(new JsonPrimitive(0));

		JsonArray expectedArray = new JsonArray();
		expectedArray.add(new JsonPrimitive(true));
		expectedArray.add(new JsonPrimitive(false));
		expectedArray.add(new JsonPrimitive(true));
		expectedArray.add(new JsonPrimitive(false));
		expectedArray.add(new JsonPrimitive(true));
		expectedArray.add(new JsonPrimitive(false));
		expectedArray.add(new JsonPrimitive(true));
		expectedArray.add(new JsonPrimitive(false));

		var arrayOfBooleanSchema = new JsonObject();
		arrayOfBooleanSchema.addProperty("name", "ArrayType");
		arrayOfBooleanSchema.addProperty("type", "ARRAY");
		arrayOfBooleanSchema.add("defaultValue", new JsonArray());

		var booleanSchema = new JsonObject();
		booleanSchema.addProperty("name", "EachElement");
		booleanSchema.addProperty("type", "BOOLEAN");
		arrayOfBooleanSchema.add("items", booleanSchema);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(
						"source", booleanArray,
						"schema", arrayOfBooleanSchema,
						"conversionMode", new JsonPrimitive(ConversionMode.STRICT.name())))
				.setContext(Map.of())
				.setSteps(Map.of());

		ObjectConvert oc = new ObjectConvert();

		StepVerifier.create(oc.execute(fep).map(e -> e.next().getResult().get("value")))
				.expectNext(expectedArray)
				.verifyComplete();
	}
}

package com.fincity.nocode.kirun.engine.runtime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class ExpressionEvaluatorTest {

	@Test
	void test() {
		
		JsonObject phone = new JsonObject();
		phone.addProperty("phone1", "1234");
		phone.addProperty("phone2", "5678");
		phone.addProperty("phone3", "5678");
		
		JsonObject address = new JsonObject();
		address.addProperty("line1", "Flat 202, PVR Estates");
		address.addProperty("line2", "Nagvara");
		address.addProperty("city", "Benguluru");
		address.addProperty("pin", "560048");
		address.add("phone", phone);

		JsonObject obj = new JsonObject();
		obj.add("studentName", new JsonPrimitive("Kumar"));
		obj.add("math", new JsonPrimitive(20));
		obj.add("isStudent", new JsonPrimitive(true));
		obj.add("address", address);
		
		Map<String, Map<String, Map<String, JsonElement>>> output = Map.of("step1", Map.of("output", Map.of("name", new JsonPrimitive("Kiran"), "obj", obj)));

		FunctionExecutionParameters parameters = new FunctionExecutionParameters().setArguments(Map.of())
		        .setContext(Map.of());

		assertEquals(new JsonPrimitive(10), new ExpressionEvaluator("3 + 7").evaluate(parameters, output));
		assertEquals(new JsonPrimitive("asdf333"), new ExpressionEvaluator("\"asdf\"+333").evaluate(parameters, output));
		assertEquals(new JsonPrimitive(422), new ExpressionEvaluator("10*11+12*13*14/7").evaluate(parameters, output));
		assertEquals(new JsonPrimitive(true), new ExpressionEvaluator("34 >> 2 = 8 ").evaluate(parameters, output));
		
		assertEquals(new JsonPrimitive(true), new ExpressionEvaluator("34 >> 2 = 8 ").evaluate(parameters, output));
		
		assertEquals(new JsonPrimitive(true), new ExpressionEvaluator("\"Kiran\" = Steps.step1.output.name ").evaluate(parameters, output));
		
		assertEquals(new JsonPrimitive(true),
		        new ExpressionEvaluator("null = Steps.step1.output.name1 ").evaluate(parameters, output));
		
		assertEquals(new JsonPrimitive(true),
		        new ExpressionEvaluator("Steps.step1.output.obj.phone.phone2 = Steps.step1.output.obj.phone.phone2 ").evaluate(parameters, output));
		
		assertEquals(new JsonPrimitive(true),
		        new ExpressionEvaluator("Steps.step1.output.obj.address.phone.phone2 != Steps.step1.output.address.obj.phone.phone1 ").evaluate(parameters, output));
	}

}

package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.tokenextractors.OutputMapTokenValueExtractor;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class OutputMapTokenValueExtractorTest {

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
		
		var omtv = new OutputMapTokenValueExtractor(output);
		
		assertEquals(new JsonPrimitive("5678"), omtv.getValue("Steps.step1.output.obj.address.phone.phone2"));
	}

}

package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

class ConcatenateTest {

	@Test
	void test() {
		Concatenate catF = new Concatenate();
		var list = new JsonArray();
		list.add("no code ");
		list.add(" Kirun ");
		list.add(" true ");
		list.add("\"'this is between the strings qith special characters'\"");
		list.add(" PLATform ");
		assertEquals(
				new JsonPrimitive(
						"no code  Kirun  true \"'this is between the strings qith special characters'\" PLATform "),
				catF.execute(new FunctionExecutionParameters().setArguments(Map.of("value", list))).next().getResult()
						.get("value"));
	}

}

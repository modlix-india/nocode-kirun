package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class ReverseTest {

	@Test
	void test() {

		Reverse rev = new Reverse();

		assertEquals(new JsonPrimitive(" mr\"ofta\"lp edoc on a si sihT"),
				rev.execute(new FunctionExecutionParameters()
						.setArguments(Map.of("value", new JsonPrimitive("This is a no code pl\"atfo\"rm ")))).next()
						.getResult().get("value"));
	}

}

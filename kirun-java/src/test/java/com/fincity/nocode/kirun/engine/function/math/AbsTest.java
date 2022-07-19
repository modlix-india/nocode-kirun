package com.fincity.nocode.kirun.engine.function.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class AbsTest {
	
	private final String VALUE = "value";
	
	@Test
	void testExecute() {

		var abs = new Abs();
		var nums = new JsonPrimitive(-45.57345);
//		nums.add(-45);
//		nums.add(-85);
//		nums.add(95.45);
//		nums.add(-38.96);
//		nums.add(-234.54);
//		nums.add(-4049.45);
//		
		assertEquals(new JsonPrimitive(45.57345), 
				abs.execute(new FunctionExecutionParameters().setArguments(Map.of(VALUE,nums))).next()
				.getResult().get(VALUE));
	}

}

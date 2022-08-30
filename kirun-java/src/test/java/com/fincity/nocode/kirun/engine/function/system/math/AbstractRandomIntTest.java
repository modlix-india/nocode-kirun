package com.fincity.nocode.kirun.engine.function.system.math;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class AbstractRandomIntTest {

	@Test
	void test() {

		var min = new JsonPrimitive(1009);

		var max = new JsonPrimitive(1000012);

		AbstractRandom absR = new AbstractRandom("RandomInt", SchemaType.INTEGER);

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of("minValue", min, "maxValue", max));

		int val = absR.execute(fep).allResults().get(0).getResult().get("value").getAsInt();
		System.out.println(val);

		assertTrue(val >= min.getAsInt() && val <= max.getAsInt());

	}

}

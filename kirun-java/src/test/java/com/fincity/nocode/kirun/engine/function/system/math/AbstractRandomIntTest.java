package com.fincity.nocode.kirun.engine.function.system.math;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class AbstractRandomIntTest {

	@Test
	void test() {

		var min = new JsonPrimitive(1009);

		var max = new JsonPrimitive(1000012);

		AbstractRandom absR = new AbstractRandom("RandomInt", Schema.ofInteger("value"));

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of("minValue", min, "maxValue", max));

		double val = absR.execute(fep).allResults().get(0).getResult().get("value").getAsDouble();
		System.out.println(val);

		assertTrue(val >= min.getAsDouble() && val <= max.getAsDouble());

	}

	@Test
	void test2() {

		var min = new JsonPrimitive(10.09d);

		RandomRepository rand = new RandomRepository();

		AbstractRandom absR = rand.find(Namespaces.MATH, "RandomDouble");

		FunctionExecutionParameters fep = new FunctionExecutionParameters().setArguments(Map.of("minValue", min));

		double val = absR.execute(fep).allResults().get(0).getResult().get("value").getAsDouble();
		System.out.println(val);

		assertTrue(val >= min.getAsDouble() && val <= Double.MAX_VALUE);

	}

	@Test
	void test3() {

		var min = new JsonPrimitive(-123);

		var max = new JsonPrimitive(-1);

		RandomRepository rand = new RandomRepository();

		AbstractRandom absR = rand.find(Namespaces.MATH, "RandomInteger");

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of("minValue", min, "maxValue", max));

		int val = absR.execute(fep).allResults().get(0).getResult().get("value").getAsInt();
		System.out.println(val);

		assertTrue(val <= max.getAsInt() && val >= min.getAsInt());
	}
}

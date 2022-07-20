package com.fincity.nocode.kirun.engine.function.math;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.system.Random;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;

class RandomTest {

	@Test
	void test() {
		var rand = new Random();
		System.out.println(rand.execute(new FunctionExecutionParameters()).next().getResult().get("value"));
	}

}

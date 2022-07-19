package com.fincity.nocode.kirun.engine.function.math;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;

class RandomTest {

	@Test
	void test() {
		var rand = new Random();
		System.out.println(rand.execute(new FunctionExecutionParameters()).next().getResult().get("value"));
	}

}

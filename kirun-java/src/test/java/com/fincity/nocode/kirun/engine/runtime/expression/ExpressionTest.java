package com.fincity.nocode.kirun.engine.runtime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExpressionTest {

	@Test
	void test() {

		assertEquals("(2+3)", new Expression("2+3").toString());
	}

}

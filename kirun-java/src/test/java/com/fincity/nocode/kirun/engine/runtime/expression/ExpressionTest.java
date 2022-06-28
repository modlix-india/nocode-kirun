package com.fincity.nocode.kirun.engine.runtime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExpressionTest {

	@Test
	void test() {

		assertEquals("(2+3)", new Expression("2+3").toString());
		assertEquals("((10*11)+(12*(13*(14/7))))", new Expression("10*11+12*13*14/7").toString());
		assertEquals("((34<<2)=8)", new Expression("34 << 2 = 8 ").toString());
	}

}

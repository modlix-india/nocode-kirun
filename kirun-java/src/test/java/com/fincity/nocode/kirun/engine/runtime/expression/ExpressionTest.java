package com.fincity.nocode.kirun.engine.runtime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExpressionTest {

	@Test
	void test() {

		assertEquals("(2+3)", new Expression("2+3").toString());
		assertEquals("((10*11)+(12*(13*(14/7))))", new Expression("10*11+12*13*14/7").toString());
		assertEquals("((34<<2)=8)", new Expression("34 << 2 = 8 ").toString());

		Expression ex = new Expression(
		        "Context.a[Steps.loop.iteration.index - 1]+ Context.a[Steps.loop.iteration.index - 2]");
		assertEquals("((Context.a[(Steps.loop.iteration.index-1))+(Context.a[(Steps.loop.iteration.index-2)))",
		        ex.toString());

		ex = new Expression("Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+2");
		assertEquals("((Steps.step1.output.obj.array[(Steps.step1.output.obj.num+1))+2)", ex.toString());
	}

}

package com.fincity.nocode.kirun.engine.runtime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExpressionTest {

	@Test
	void test() {

		assertEquals("(2+3)", new Expression("2+3").toString());
		assertEquals("((2.234)+(3*(1.22243)))", new Expression("2.234 + 3 * 1.22243").toString());
		assertEquals("((10*11)+(12*(13*(14/7))))", new Expression("10*11+12*13*14/7").toString());
		assertEquals("((34<<2)=8)", new Expression("34 << 2 = 8 ").toString());

		Expression ex = new Expression(
		        "Context.a[Steps.loop.iteration.index - 1]+ Context.a[Steps.loop.iteration.index - 2]");
		assertEquals(
		        "((Context.(a[((Steps.(loop.(iteration.index)))-1)))+(Context.(a[((Steps.(loop.(iteration.index)))-2))))",
		        ex.toString());

		ex = new Expression("Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+2");
		assertEquals("((Steps.(step1.(output.(obj.(array[((Steps.(step1.(output.(obj.num))))+1))))))+2)",
		        ex.toString());

		Expression arrays = new Expression("Context.a[Steps.loop.iteration.index][Steps.loop.iteration.index + 1]");
		Expression deepObject = new Expression("Context.a.b.c");
		Expression deepObjectWithArray = new Expression("Context.a.b[2].c");

		assertEquals("(Context.(a[((Steps.(loop.(iteration.index)))[((Steps.(loop.(iteration.index)))+1))))", arrays.toString());
		assertEquals("(Context.(a.(b.c)))", deepObject.toString());
		assertEquals("(Context.(a.(b[(2.c))))", deepObjectWithArray.toString());
		
		Expression opInTheName = new Expression("Store.a.b.c or Store.c.d.x");
		assertEquals("((Store.(a.(b.c)))or(Store.(c.(d.x))))", opInTheName.toString());
		
		opInTheName = new Expression("Store.a.b.corStore.c.d.x");
		assertEquals("(Store.(a.(b.(corStore.(c.(d.x))))))", opInTheName.toString());
	}

}
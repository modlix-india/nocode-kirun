package com.fincity.nocode.kirun.engine.runtime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.ArgumentsTokenValueExtractor;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class ExpressionEvaluatorTernaryOperatorTest {

	@Test
	void testExpression() {

		var exp = new Expression("a > 10 ?  a - 2 : a + 3".replace(" ", ""));
		assertEquals("((a>10)?(a-2):(a+3))", exp.toString());

		exp = new Expression("a > 10 ?  a - 2 : a + 3");
		assertEquals("((a>10)?(a-2):(a+3))", exp.toString());

		exp = new Expression("a > 10 ? a > 15 ? a + 2 : a - 2 : a + 3");
		assertEquals("((a>10)?((a>15)?(a+2):(a-2)):(a+3))", exp.toString());
	}

	@Test
	void testTernaryOperator() {

		var cobj = new JsonObject();
		cobj.addProperty("a", 2);

		var cbArray = new JsonArray();
		cbArray.add(true);
		cbArray.add(false);
		cobj.add("b", cbArray);

		var ccObj = new JsonObject();
		ccObj.addProperty("x", "kiran");

		var dcObj = new JsonObject();
		dcObj.addProperty("x", "kiran");

		cobj.add("c", ccObj);

		ArgumentsTokenValueExtractor atv = new ArgumentsTokenValueExtractor(Map.of("a", new JsonPrimitive("kirun "),
		        "b", new JsonPrimitive(2), "b2", new JsonPrimitive(4), "c", cobj, "d", new JsonPrimitive(20)));
		Map<String, TokenValueExtractor> valuesMap = Map.of(atv.getPrefix(), atv);

		var ev = new ExpressionEvaluator("Arguments.e = null ? Arguments.c.a : 3 ");
		assertEquals(2, ev.evaluate(valuesMap)
		        .getAsInt());
		
		ev = new ExpressionEvaluator("Arguments.f ? Arguments.c.a : 3 ");
		assertEquals(3, ev.evaluate(valuesMap)
		        .getAsInt());
		
		ev = new ExpressionEvaluator("Arguments.e = null ? Arguments.c : 3 ");
		assertEquals(cobj, ev.evaluate(valuesMap)
		        .getAsJsonObject());
	}

}

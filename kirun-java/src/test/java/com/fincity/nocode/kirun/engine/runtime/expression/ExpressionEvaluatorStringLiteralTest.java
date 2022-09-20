package com.fincity.nocode.kirun.engine.runtime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.expression.exception.ExpressionEvaluationException;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.ArgumentsTokenValueExtractor;
import com.google.gson.JsonPrimitive;

class ExpressionEvaluatorStringLiteralTest {

	@Test
	void test() {

		Expression ex = new Expression("'ki/run'+'ab'");

		ExpressionEvaluator ev = new ExpressionEvaluator(ex);

		assertEquals(new JsonPrimitive("ki/runab"), ev.evaluate(Map.of()));

		ExpressionEvaluator evt = new ExpressionEvaluator("\"Steps.a");

		ArgumentsTokenValueExtractor atv = new ArgumentsTokenValueExtractor(
		        Map.of("a", new JsonPrimitive("kirun "), "b", new JsonPrimitive(2), "c", new JsonPrimitive(true)));
		Map<String, TokenValueExtractor> valuesMap = Map.of(atv.getPrefix(), atv);

		assertThrows(ExpressionEvaluationException.class, () -> evt.evaluate(valuesMap));

		ev = new ExpressionEvaluator("Arguments.a+'kiran'");

		assertEquals(new JsonPrimitive("kirun kiran"), ev.evaluate(valuesMap));

		ev = new ExpressionEvaluator("Arguments.b+'kiran'");

		assertEquals(new JsonPrimitive("2kiran"), ev.evaluate(valuesMap));

		ev = new ExpressionEvaluator("Arguments.c+'k\"ir\"an'");

		assertEquals(new JsonPrimitive("truek\"ir\"an"), ev.evaluate(valuesMap));

		ev = new ExpressionEvaluator("Arguments.b+\"'kir\" + ' an'");

		assertEquals(new JsonPrimitive("2'kir an"), ev.evaluate(valuesMap));

		ev = new ExpressionEvaluator("Arguments.a+'kiran'+ Arguments.b");

		assertEquals(new JsonPrimitive("kirun kiran2"), ev.evaluate(valuesMap));
	}

	@Test
	void testMultiplication() {

		ExpressionEvaluator ev = new ExpressionEvaluator("'a' * 10");

		ArgumentsTokenValueExtractor atv = new ArgumentsTokenValueExtractor(Map.of("a", new JsonPrimitive("kirun "),
		        "b", new JsonPrimitive(2), "c", new JsonPrimitive(true), "d", new JsonPrimitive(1.5)));
		Map<String, TokenValueExtractor> valuesMap = Map.of(atv.getPrefix(), atv);

		assertEquals("aaaaaaaaaa", ev.evaluate(valuesMap)
		        .getAsString());

		ev = new ExpressionEvaluator("2.5*Arguments.a");
		assertEquals("kirun kirun kir", ev.evaluate(valuesMap).getAsString());
		
		ev = new ExpressionEvaluator("-0.5*Arguments.a");
		assertEquals("rik", ev.evaluate(valuesMap).getAsString());
		
		ev = new ExpressionEvaluator("'asdf'*-1");
		assertEquals("fdsa", ev.evaluate(valuesMap).getAsString());
		
		ev = new ExpressionEvaluator("'asdf'*0");
		assertEquals("", ev.evaluate(valuesMap).getAsString());
	}
}

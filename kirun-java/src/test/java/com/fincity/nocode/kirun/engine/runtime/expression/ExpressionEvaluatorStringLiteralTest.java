package com.fincity.nocode.kirun.engine.runtime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.expression.exception.ExpressionEvaluationException;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.ArgumentsTokenValueExtractor;
import com.google.gson.JsonObject;
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
		
		ev = new ExpressionEvaluator("2.val");
		assertEquals("2.val", ev.evaluate(valuesMap).getAsString());
	}
	
    @Test
    void testForStringLengthCase() {

        JsonObject jsonObj = new JsonObject();
        jsonObj.add("greeting", new JsonPrimitive("hello"));
        jsonObj.add("name", new JsonPrimitive("surendhar"));

        ArgumentsTokenValueExtractor atve = new ArgumentsTokenValueExtractor(
                Map.of("a", new JsonPrimitive("surendhar "),
                        "b", new JsonPrimitive(2), "c", new JsonPrimitive(true), "d", new JsonPrimitive(1.5), "obj",
                        jsonObj));

        Map<String, TokenValueExtractor> valuesMap = Map.of(atve.getPrefix(), atve);
        ExpressionEvaluator ev = new ExpressionEvaluator("Arguments.a.length");

        assertEquals(new JsonPrimitive(10), ev.evaluate(valuesMap));

        ev = new ExpressionEvaluator("Arguments.a.length");

        ExpressionEvaluator nm = new ExpressionEvaluator("Arguments.b.length");
        assertThrows(ExpressionEvaluationException.class, () -> nm.evaluate(valuesMap));

        ev = new ExpressionEvaluator("Arguments.obj.greeting.length*'S'");
        assertEquals(new JsonPrimitive("SSSSS"), ev.evaluate(valuesMap));
        ev = new ExpressionEvaluator("Arguments.obj.greeting.length*'SP'");
        assertEquals(new JsonPrimitive("SPSPSPSPSP"), ev.evaluate(valuesMap));

        ev = new ExpressionEvaluator("Arguments.obj.name.length ? 'fun':'not Fun'");
        assertEquals(new JsonPrimitive("fun"), ev.evaluate(valuesMap));

    }
}

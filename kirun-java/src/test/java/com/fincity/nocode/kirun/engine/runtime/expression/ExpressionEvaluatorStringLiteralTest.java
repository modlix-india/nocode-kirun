package com.fincity.nocode.kirun.engine.runtime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.expression.exception.ExpressionEvaluationException;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.ArgumentsTokenValueExtractor;
import com.google.gson.JsonElement;
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

	@Test
	void testForStringLengthCaseSquareBracket() {

		JsonObject jsonObj = new JsonObject();
		jsonObj.add("greeting", new JsonPrimitive("hello"));
		jsonObj.add("name", new JsonPrimitive("surendhar"));

		ArgumentsTokenValueExtractor atve = new ArgumentsTokenValueExtractor(
				Map.of("a", new JsonPrimitive("surendhar "),
						"b", new JsonPrimitive(2), "c", new JsonPrimitive(true), "d", new JsonPrimitive(1.5), "obj",
						jsonObj));

		Map<String, TokenValueExtractor> valuesMap = Map.of(atve.getPrefix(), atve);
		ExpressionEvaluator ev = new ExpressionEvaluator("Arguments.a[\"length\"]");

		assertEquals(new JsonPrimitive(10), ev.evaluate(valuesMap));

		ev = new ExpressionEvaluator("Arguments.a[\"length\"]");

		ExpressionEvaluator nm = new ExpressionEvaluator("Arguments.b[\"length\"]");
		assertThrows(ExpressionEvaluationException.class, () -> nm.evaluate(valuesMap));

		ev = new ExpressionEvaluator("Arguments.obj.greeting[\"length\"]*'S'");
		assertEquals(new JsonPrimitive("SSSSS"), ev.evaluate(valuesMap));
		ev = new ExpressionEvaluator("Arguments.obj.greeting[\"length\"]*'SP'");
		assertEquals(new JsonPrimitive("SPSPSPSPSP"), ev.evaluate(valuesMap));

		ev = new ExpressionEvaluator("Arguments.obj[\"greeting\"][\"length\"]*'S'");
		assertEquals(new JsonPrimitive("SSSSS"), ev.evaluate(valuesMap));
		ev = new ExpressionEvaluator("Arguments.obj[\"greeting\"][\"length\"]*'SP'");
		assertEquals(new JsonPrimitive("SPSPSPSPSP"), ev.evaluate(valuesMap));

		ev = new ExpressionEvaluator("Arguments.obj.name[\"length\"] ? 'fun':'not Fun'");
		assertEquals(new JsonPrimitive("fun"), ev.evaluate(valuesMap));

	}

	@Test
	void testForStringLengthCaseWLengthObjectSquareBracket() {

		JsonObject jsonObj = new JsonObject();
		jsonObj.add("length", new JsonPrimitive("hello"));
		jsonObj.add("name", new JsonPrimitive("surendhar"));

		ArgumentsTokenValueExtractor atve = new ArgumentsTokenValueExtractor(
				Map.of("a", new JsonPrimitive("surendhar "),
						"b", new JsonPrimitive(2), "c", new JsonPrimitive(true), "d", new JsonPrimitive(1.5), "obj",
						jsonObj));

		Map<String, TokenValueExtractor> valuesMap = Map.of(atve.getPrefix(), atve);
		ExpressionEvaluator ev = new ExpressionEvaluator("Arguments.a[\"length\"]");

		assertEquals(new JsonPrimitive(10), ev.evaluate(valuesMap));

		ev = new ExpressionEvaluator("Arguments.a[\"length\"]");

		ExpressionEvaluator nm = new ExpressionEvaluator("Arguments.b[\"length\"]");
		assertThrows(ExpressionEvaluationException.class, () -> nm.evaluate(valuesMap));

		ev = new ExpressionEvaluator("Arguments.obj.length[\"length\"]*'S'");
		assertEquals(new JsonPrimitive("SSSSS"), ev.evaluate(valuesMap));
		ev = new ExpressionEvaluator("Arguments.obj.length[\"length\"]*'SP'");
		assertEquals(new JsonPrimitive("SPSPSPSPSP"), ev.evaluate(valuesMap));

		ev = new ExpressionEvaluator("Arguments.obj[\"length\"][\"length\"]*'S'");
		assertEquals(new JsonPrimitive("SSSSS"), ev.evaluate(valuesMap));
		ev = new ExpressionEvaluator("Arguments.obj[\"length\"][\"length\"]*'SP'");
		assertEquals(new JsonPrimitive("SPSPSPSPSP"), ev.evaluate(valuesMap));

		ev = new ExpressionEvaluator("Arguments.obj.name[\"length\"] ? 'fun':'not Fun'");
		assertEquals(new JsonPrimitive("fun"), ev.evaluate(valuesMap));

	}

	/**
	 * Custom token extractor for testing Steps.* paths
	 */
	static class StepsTokenValueExtractor extends TokenValueExtractor {
		private final Map<String, JsonElement> data;

		public StepsTokenValueExtractor(Map<String, JsonElement> data) {
			this.data = data;
		}

		@Override
		protected JsonElement getValueInternal(String token) {
			String[] parts = TokenValueExtractor.splitPath(token);
			String key = parts[1];
			int bIndex = key.indexOf('[');
			int fromIndex = 2;
			if (bIndex != -1) {
				key = parts[1].substring(0, bIndex);
				parts[1] = parts[1].substring(bIndex);
				fromIndex = 1;
			}
			return this.retrieveElementFrom(token, parts, fromIndex, this.data.get(key));
		}

		@Override
		public String getPrefix() {
			return "Steps.";
		}

		@Override
		public JsonElement getStore() {
			JsonObject store = new JsonObject();
			for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
				store.add(entry.getKey(), entry.getValue());
			}
			return store;
		}
	}

	@Test
	void testStringLiteralWithTemplateInterpolation() {
		// Create Steps extractor with nested data
		JsonObject countLoop = new JsonObject();
		JsonObject iteration = new JsonObject();
		iteration.add("index", new JsonPrimitive(1));
		countLoop.add("iteration", iteration);

		Map<String, JsonElement> stepsData = new HashMap<>();
		stepsData.put("countLoop", countLoop);
		stepsData.put("index", new JsonPrimitive(5));

		StepsTokenValueExtractor stepsAtv = new StepsTokenValueExtractor(stepsData);

		ArgumentsTokenValueExtractor argsAtv = new ArgumentsTokenValueExtractor(
			Map.of("a", new JsonPrimitive("test"),
				"b", new JsonPrimitive(10),
				"c", new JsonPrimitive(15)));

		Map<String, TokenValueExtractor> valuesMap = new HashMap<>();
		valuesMap.put(stepsAtv.getPrefix(), stepsAtv);
		valuesMap.put(argsAtv.getPrefix(), argsAtv);

		// Test that {{}} expressions inside string literals are evaluated
		ExpressionEvaluator ev = new ExpressionEvaluator(
			"'Page.appDefinitions.content[{{Steps.countLoop.iteration.index}}].stringValue'");
		assertEquals(new JsonPrimitive("Page.appDefinitions.content[1].stringValue"), ev.evaluate(valuesMap));

		// Test with double quotes
		ev = new ExpressionEvaluator(
			"\"Page.appDefinitions.content[{{Steps.countLoop.iteration.index}}].stringValue\"");
		assertEquals(new JsonPrimitive("Page.appDefinitions.content[1].stringValue"), ev.evaluate(valuesMap));

		// Test concatenation with string containing template interpolation
		ev = new ExpressionEvaluator("Arguments.a + ' - ' + 'Path: {{Steps.index}}'");
		assertEquals(new JsonPrimitive("test - Path: 5"), ev.evaluate(valuesMap));

		// Test multiple template placeholders in one string
		ev = new ExpressionEvaluator("'{{Arguments.a}} + {{Arguments.b}} = {{Arguments.c}}'");
		assertEquals(new JsonPrimitive("test + 10 = 15"), ev.evaluate(valuesMap));

		// Test with arithmetic inside {{}}
		ev = new ExpressionEvaluator("'Result: {{Arguments.b + Arguments.c}}!'");
		assertEquals(new JsonPrimitive("Result: 25!"), ev.evaluate(valuesMap));

		// Test nested property access
		ev = new ExpressionEvaluator("'Item {{Steps.countLoop.iteration.index}} of {{Arguments.c}}'");
		assertEquals(new JsonPrimitive("Item 1 of 15"), ev.evaluate(valuesMap));
	}
}

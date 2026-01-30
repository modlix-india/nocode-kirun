package com.fincity.nocode.kirun.engine.runtime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.ArgumentsTokenValueExtractor;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Expression parsing tests replicating kirun-js ExpressionParsingTest.
 * Verifies nullish coalescing (??), comparison operators, dynamic array indexing, and related features.
 */
class ExpressionParsingTest {

	/**
	 * Token extractor for testing Page.* and Parent.* paths with JsonObject data.
	 */
	static class PrefixJsonTokenValueExtractor extends TokenValueExtractor {
		private final String prefix;
		private final JsonObject data;

		PrefixJsonTokenValueExtractor(String prefix, JsonObject data) {
			this.prefix = prefix;
			this.data = data;
		}

		@Override
		protected JsonElement getValueInternal(String token) {
			String[] parts = TokenValueExtractor.splitPath(token);
			if (parts.length < 2) return null;
			String key = parts[1];
			int bIndex = key.indexOf('[');
			int fromIndex = 2;
			if (bIndex != -1) {
				String[] partsCopy = parts.clone();
				key = parts[1].substring(0, bIndex);
				partsCopy[1] = parts[1].substring(bIndex);
				return retrieveElementFrom(token, partsCopy, 1, data.get(key));
			}
			return retrieveElementFrom(token, parts, fromIndex, data.get(key));
		}

		@Override
		public String getPrefix() {
			return prefix;
		}

		@Override
		public JsonElement getStore() {
			return data;
		}
	}

	@Test
	void parseExpressionWithNullishCoalescingAndStringConcat() {
		JsonObject pageData = new JsonObject();
		pageData.addProperty("userFirstName", "John");
		pageData.addProperty("userLastName", "Doe");

		PrefixJsonTokenValueExtractor pageExtractor = new PrefixJsonTokenValueExtractor("Page.", pageData);
		Map<String, TokenValueExtractor> valuesMap = Map.of(pageExtractor.getPrefix(), pageExtractor);

		String expr = "(Page.userFirstName??'') +' '+ (Page.userLastName??'')";
		assertNotNull(new Expression(expr));

		ExpressionEvaluator ev = new ExpressionEvaluator(expr);
		assertEquals(new JsonPrimitive("John Doe"), ev.evaluate(valuesMap));

		// Both null/undefined - should return " "
		PrefixJsonTokenValueExtractor pageEmpty = new PrefixJsonTokenValueExtractor("Page.", new JsonObject());
		Map<String, TokenValueExtractor> valuesMapEmpty = Map.of(pageEmpty.getPrefix(), pageEmpty);
		assertEquals(new JsonPrimitive(" "), ev.evaluate(valuesMapEmpty));

		// First name only
		JsonObject pageFirst = new JsonObject();
		pageFirst.addProperty("userFirstName", "Jane");
		PrefixJsonTokenValueExtractor pageFirstExt = new PrefixJsonTokenValueExtractor("Page.", pageFirst);
		Map<String, TokenValueExtractor> valuesMapFirst = Map.of(pageFirstExt.getPrefix(), pageFirstExt);
		assertEquals(new JsonPrimitive("Jane "), ev.evaluate(valuesMapFirst));

		// Last name only
		JsonObject pageLast = new JsonObject();
		pageLast.addProperty("userLastName", "Smith");
		PrefixJsonTokenValueExtractor pageLastExt = new PrefixJsonTokenValueExtractor("Page.", pageLast);
		Map<String, TokenValueExtractor> valuesMapLast = Map.of(pageLastExt.getPrefix(), pageLastExt);
		assertEquals(new JsonPrimitive(" Smith"), ev.evaluate(valuesMapLast));
	}

	@Test
	void parseExpressionWithNullishCoalescing() {
		JsonObject parentData = new JsonObject();
		JsonObject projectInfo = new JsonObject();
		projectInfo.addProperty("projectType", "Commercial");
		parentData.add("projectInfo", projectInfo);

		PrefixJsonTokenValueExtractor parentExtractor = new PrefixJsonTokenValueExtractor("Parent.", parentData);
		Map<String, TokenValueExtractor> valuesMap = Map.of(parentExtractor.getPrefix(), parentExtractor);

		String expr = "Parent.projectInfo.projectType?? '-'";
		assertNotNull(new Expression(expr));

		ExpressionEvaluator ev = new ExpressionEvaluator("Parent.projectInfo.projectType ?? '-'");
		assertEquals(new JsonPrimitive("Commercial"), ev.evaluate(valuesMap));

		ExpressionEvaluator ev1 = new ExpressionEvaluator("Parent.projectInfo.projectType1?? '-'");
		assertEquals(new JsonPrimitive("-"), ev1.evaluate(valuesMap));

		// Value null - should return default
		JsonObject parentNull = new JsonObject();
		JsonObject projectInfoNull = new JsonObject();
		projectInfoNull.add("projectType", JsonNull.INSTANCE);
		parentNull.add("projectInfo", projectInfoNull);
		PrefixJsonTokenValueExtractor parentNullExt = new PrefixJsonTokenValueExtractor("Parent.", parentNull);
		Map<String, TokenValueExtractor> valuesMapNull = Map.of(parentNullExt.getPrefix(), parentNullExt);
		assertEquals(new JsonPrimitive("-"), ev.evaluate(valuesMapNull));

		// Missing property - should return default
		JsonObject parentMissing = new JsonObject();
		parentMissing.add("projectInfo", new JsonObject());
		PrefixJsonTokenValueExtractor parentMissingExt = new PrefixJsonTokenValueExtractor("Parent.", parentMissing);
		Map<String, TokenValueExtractor> valuesMapMissing = Map.of(parentMissingExt.getPrefix(), parentMissingExt);
		assertEquals(new JsonPrimitive("-"), ev.evaluate(valuesMapMissing));
	}

	@Test
	void parseExpressionWithDynamicArrayIndexAndDotAccess() {
		// Use Arguments with numeric index - Arguments.perCount[1] (static index)
		com.google.gson.JsonArray perCount = new com.google.gson.JsonArray();
		for (int pct : new int[] { 10, 25, 50 }) {
			JsonObject item = new JsonObject();
			JsonObject value = new JsonObject();
			value.addProperty("Percentage", pct);
			item.add("value", value);
			perCount.add(item);
		}
		ArgumentsTokenValueExtractor argsExtractor = new ArgumentsTokenValueExtractor(
				Map.of("perCount", perCount));
		Map<String, TokenValueExtractor> valuesMap = Map.of(argsExtractor.getPrefix(), argsExtractor);

		String expr = "Arguments.perCount[1].value.Percentage + '%'";
		assertNotNull(new Expression(expr));

		ExpressionEvaluator ev = new ExpressionEvaluator(expr);
		assertEquals(new JsonPrimitive("25%"), ev.evaluate(valuesMap));
	}

	@Test
	void parseExpressionWithLessThanComparison() {
		JsonObject pageData = new JsonObject();
		JsonObject dealData = new JsonObject();
		dealData.addProperty("size", 10);
		dealData.addProperty("totalElements", 100);
		pageData.add("dealData", dealData);

		PrefixJsonTokenValueExtractor pageExtractor = new PrefixJsonTokenValueExtractor("Page.", pageData);
		Map<String, TokenValueExtractor> valuesMap = Map.of(pageExtractor.getPrefix(), pageExtractor);

		String expr = "Page.dealData.size < Page.dealData.totalElements";
		assertNotNull(new Expression(expr));

		ExpressionEvaluator ev = new ExpressionEvaluator(expr);
		assertEquals(new JsonPrimitive(true), ev.evaluate(valuesMap));

		// size >= totalElements
		JsonObject pageEqual = new JsonObject();
		JsonObject dealEqual = new JsonObject();
		dealEqual.addProperty("size", 100);
		dealEqual.addProperty("totalElements", 100);
		pageEqual.add("dealData", dealEqual);
		PrefixJsonTokenValueExtractor pageEqualExt = new PrefixJsonTokenValueExtractor("Page.", pageEqual);
		Map<String, TokenValueExtractor> valuesMapEqual = Map.of(pageEqualExt.getPrefix(), pageEqualExt);
		assertEquals(new JsonPrimitive(false), ev.evaluate(valuesMapEqual));

		// size > totalElements
		JsonObject pageGreater = new JsonObject();
		JsonObject dealGreater = new JsonObject();
		dealGreater.addProperty("size", 150);
		dealGreater.addProperty("totalElements", 100);
		pageGreater.add("dealData", dealGreater);
		PrefixJsonTokenValueExtractor pageGreaterExt = new PrefixJsonTokenValueExtractor("Page.", pageGreater);
		Map<String, TokenValueExtractor> valuesMapGreater = Map.of(pageGreaterExt.getPrefix(), pageGreaterExt);
		assertEquals(new JsonPrimitive(false), ev.evaluate(valuesMapGreater));
	}

	@Test
	void parseExpressionWithExtraSpaces() {
		JsonObject pageData = new JsonObject();
		JsonObject dealData = new JsonObject();
		dealData.addProperty("size", 5);
		dealData.addProperty("totalElements", 20);
		pageData.add("dealData", dealData);

		PrefixJsonTokenValueExtractor pageExtractor = new PrefixJsonTokenValueExtractor("Page.", pageData);
		Map<String, TokenValueExtractor> valuesMap = Map.of(pageExtractor.getPrefix(), pageExtractor);

		String expr = "Page.dealData.size <  Page.dealData.totalElements";
		assertNotNull(new Expression(expr));

		ExpressionEvaluator ev = new ExpressionEvaluator(expr);
		assertEquals(new JsonPrimitive(true), ev.evaluate(valuesMap));
	}

	@Test
	void expressionToStringPreservesStructure() {
		Expression expr1 = new Expression("Page.dealData.size");
		assertTrue(expr1.toString().contains("Page"));
		assertTrue(expr1.toString().contains("dealData"));

		Expression expr2 = new Expression("Page.dealData.size < Page.dealData.totalElements");
		assertTrue(expr2.toString().contains("<"));

		Expression expr3 = new Expression("Steps.floorWeekOne.output.value * 86400");
		assertTrue(expr3.toString().contains("*"));

		Expression expr4 = new Expression("Parent.projectInfo.projectType ?? '-'");
		assertTrue(expr4.toString().contains("??"));
	}

	@Test
	void parseComplexExpressionWithArrayAccessAndStringConcatenation() {
		com.google.gson.JsonArray perCount = new com.google.gson.JsonArray();
		for (int pct : new int[] { 15, 30, 45 }) {
			JsonObject item = new JsonObject();
			JsonObject value = new JsonObject();
			value.addProperty("Percentage", pct);
			item.add("value", value);
			perCount.add(item);
		}
		ArgumentsTokenValueExtractor argsExtractor = new ArgumentsTokenValueExtractor(
				Map.of("perCount", perCount));
		Map<String, TokenValueExtractor> valuesMap = Map.of(argsExtractor.getPrefix(), argsExtractor);

		ExpressionEvaluator ev = new ExpressionEvaluator("Arguments.perCount[2].value.Percentage + '%'");
		assertEquals(new JsonPrimitive("45%"), ev.evaluate(valuesMap));
	}

	@Test
	void originalExpressionParsingVerifyAllParseWithoutThrowing() {
		String[] expressions = {
			"Parent.projectInfo.projectType?? '-'",
			"Page.dealData.size <  Page.dealData.totalElements",
			"(Page.userFirstName??'') +' '+ (Page.userLastName??'')"
		};

		for (String expression : expressions) {
			assertNotNull(new Expression(expression));
		}
	}

}

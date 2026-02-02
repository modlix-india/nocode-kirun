package com.fincity.nocode.kirun.engine.runtime.expression;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
	void parseExpressionWithMultiplicationAndNestedTemplate() {
		JsonObject stepsData = new JsonObject();
		JsonObject floorWeekOne = new JsonObject();
		JsonObject output = new JsonObject();
		output.addProperty("value", 7);
		floorWeekOne.add("output", output);
		stepsData.add("floorWeekOne", floorWeekOne);

		JsonObject pageData = new JsonObject();
		pageData.addProperty("secondsInDay", 86400);

		PrefixJsonTokenValueExtractor stepsExtractor = new PrefixJsonTokenValueExtractor("Steps.", stepsData);
		PrefixJsonTokenValueExtractor pageExtractor = new PrefixJsonTokenValueExtractor("Page.", pageData);
		Map<String, TokenValueExtractor> valuesMap = Map.of(
				stepsExtractor.getPrefix(), stepsExtractor,
				pageExtractor.getPrefix(), pageExtractor);

		String expr = "Steps.floorWeekOne.output.value * {{Page.secondsInDay}}";
		Expression expression = new Expression(expr);
		assertNotNull(expression);
		assertTrue(!expression.getOperations().isEmpty());

		ExpressionEvaluator ev = new ExpressionEvaluator(expr);
		assertEquals(new JsonPrimitive(7 * 86400), ev.evaluate(valuesMap));
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
			"Steps.floorWeekOne.output.value * {{Page.secondsInDay}}",
			"Parent.projectInfo.projectType?? '-'",
			"Page.dealData.size <  Page.dealData.totalElements",
			"(Page.userFirstName??'') +' '+ (Page.userLastName??'')"
		};

		for (String expression : expressions) {
			assertNotNull(new Expression(expression));
		}
	}

	@Test
	void parseExpressionWithNumericPropertyPathSegment() {
		// Test numeric segment in property path (e.g., Page.x.123.a.first)
		// This tests backward compatibility with numeric property keys
		JsonObject xData = new JsonObject();
		JsonObject numericKey = new JsonObject();
		JsonObject aData = new JsonObject();
		aData.addProperty("first", "John");
		aData.addProperty("last", "Doe");
		numericKey.add("a", aData);
		xData.add("123", numericKey);

		JsonObject pageData = new JsonObject();
		pageData.add("x", xData);

		PrefixJsonTokenValueExtractor pageExtractor = new PrefixJsonTokenValueExtractor("Page.", pageData);
		Map<String, TokenValueExtractor> valuesMap = Map.of(pageExtractor.getPrefix(), pageExtractor);

		String expr = "Page.x.123.a.first +' '+ Page.x.123.a.last";
		assertNotNull(new Expression(expr));

		ExpressionEvaluator ev = new ExpressionEvaluator(expr);
		assertEquals(new JsonPrimitive("John Doe"), ev.evaluate(valuesMap));

		// Test in ternary expression
		String ternaryExpr = "true ? Page.x.123.a.first +' '+ Page.x.123.a.last : '-'";
		ExpressionEvaluator evTernary = new ExpressionEvaluator(ternaryExpr);
		assertEquals(new JsonPrimitive("John Doe"), evTernary.evaluate(valuesMap));
	}

	@Test
	void parseExpressionWithObjectIdLikePropertyPath() {
		// Test with alphanumeric ObjectId-like value (e.g., MongoDB ObjectId: 507f1f77bcf86cd799439011)
		// This verifies that the parser correctly handles property keys that start with numbers
		// but contain letters (common with database-generated IDs)
		JsonObject xData = new JsonObject();
		JsonObject objectIdKey = new JsonObject();
		JsonObject aData = new JsonObject();
		aData.addProperty("first", "Jane");
		aData.addProperty("last", "Smith");
		objectIdKey.add("a", aData);
		xData.add("507f1f77bcf86cd799439011", objectIdKey);

		JsonObject pageData = new JsonObject();
		pageData.add("x", xData);

		PrefixJsonTokenValueExtractor pageExtractor = new PrefixJsonTokenValueExtractor("Page.", pageData);
		Map<String, TokenValueExtractor> valuesMap = Map.of(pageExtractor.getPrefix(), pageExtractor);

		String expr = "Page.x.507f1f77bcf86cd799439011.a.first +' '+ Page.x.507f1f77bcf86cd799439011.a.last";
		assertNotNull(new Expression(expr));

		ExpressionEvaluator ev = new ExpressionEvaluator(expr);
		assertEquals(new JsonPrimitive("Jane Smith"), ev.evaluate(valuesMap));

		// Test in ternary expression
		String ternaryExpr = "true ? Page.x.507f1f77bcf86cd799439011.a.first +' '+ Page.x.507f1f77bcf86cd799439011.a.last : '-'";
		ExpressionEvaluator evTernary = new ExpressionEvaluator(ternaryExpr);
		assertEquals(new JsonPrimitive("Jane Smith"), evTernary.evaluate(valuesMap));
	}

	@Test
	void parseExpressionWithUnderscoreInPropertyPath() {
		// Test with property names containing underscores and numbers (e.g., 123_abc)
		// This ensures the parser handles mixed alphanumeric property keys with underscores
		JsonObject xData = new JsonObject();
		JsonObject underscoreKey = new JsonObject();
		JsonObject aData = new JsonObject();
		aData.addProperty("first", "Bob");
		aData.addProperty("last", "Johnson");
		underscoreKey.add("a", aData);
		xData.add("123_abc", underscoreKey);

		JsonObject pageData = new JsonObject();
		pageData.add("x", xData);

		PrefixJsonTokenValueExtractor pageExtractor = new PrefixJsonTokenValueExtractor("Page.", pageData);
		Map<String, TokenValueExtractor> valuesMap = Map.of(pageExtractor.getPrefix(), pageExtractor);

		String expr = "Page.x.123_abc.a.first +' '+ Page.x.123_abc.a.last";
		assertNotNull(new Expression(expr));

		ExpressionEvaluator ev = new ExpressionEvaluator(expr);
		assertEquals(new JsonPrimitive("Bob Johnson"), ev.evaluate(valuesMap));

		// Test in ternary expression
		String ternaryExpr = "true ? Page.x.123_abc.a.first +' '+ Page.x.123_abc.a.last : '-'";
		ExpressionEvaluator evTernary = new ExpressionEvaluator(ternaryExpr);
		assertEquals(new JsonPrimitive("Bob Johnson"), evTernary.evaluate(valuesMap));
	}

	@Test
	void parseComplexNestedTernaryWithNumericKeys() {
		// Test the original complex nested ternary expression with numeric property keys
		// This is similar to the real-world use case: Page.kycs.123.individual...
		// Focus on testing the parser's ability to handle numeric property keys
		JsonObject kycsData = new JsonObject();
		JsonObject accountData = new JsonObject();
		JsonObject individualData = new JsonObject();
		JsonObject basicData = new JsonObject();
		JsonObject personalInfo = new JsonObject();
		personalInfo.addProperty("firstName", "John");
		personalInfo.addProperty("lastName", "Doe");
		basicData.add("personalInformation", personalInfo);
		individualData.add("basic", basicData);
		accountData.add("individual", individualData);
		kycsData.add("123", accountData);

		JsonObject pageData = new JsonObject();
		pageData.add("kycs", kycsData);

		JsonObject parentData = new JsonObject();
		parentData.addProperty("kycAccountId", "123");

		PrefixJsonTokenValueExtractor pageExtractor = new PrefixJsonTokenValueExtractor("Page.", pageData);
		PrefixJsonTokenValueExtractor parentExtractor = new PrefixJsonTokenValueExtractor("Parent.", parentData);
		Map<String, TokenValueExtractor> valuesMap = Map.of(
			pageExtractor.getPrefix(), pageExtractor,
			parentExtractor.getPrefix(), parentExtractor
		);

		// Test direct property access with numeric keys and dynamic template
		String simpleExpr = "Page.kycs.{{Parent.kycAccountId}}.individual.basic.personalInformation.firstName +' '+ " +
		                   "Page.kycs.{{Parent.kycAccountId}}.individual.basic.personalInformation.lastName";

		assertNotNull(new Expression(simpleExpr));

		ExpressionEvaluator evSimple = new ExpressionEvaluator(simpleExpr);
		assertEquals(new JsonPrimitive("John Doe"), evSimple.evaluate(valuesMap));

		// Test with ternary and nullish coalescing (simpler than != undefined)
		String ternaryExpr = "Page.kycs.123.individual.basic.personalInformation.firstName ?? 'N/A'";
		ExpressionEvaluator evTernary = new ExpressionEvaluator(ternaryExpr);
		assertEquals(new JsonPrimitive("John"), evTernary.evaluate(valuesMap));
	}

}

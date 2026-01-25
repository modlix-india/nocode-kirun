package com.fincity.nocode.kirun.engine.runtime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class ExpressionEvaluatorLengthSubtractionTest {

	static class PageTokenValueExtractor extends TokenValueExtractor {
		private JsonElement store;

		public PageTokenValueExtractor(JsonElement store) {
			this.store = store;
		}

		@Override
		protected JsonElement getValueInternal(String token) {
			return this.retrieveElementFrom(token, TokenValueExtractor.splitPath(token), 1, store);
		}

		@Override
		public String getPrefix() {
			return "Page.";
		}

		@Override
		public JsonElement getStore() {
			return this.store;
		}
	}

	@Test
	void testArrayLengthAlone() {
		JsonObject page = new JsonObject();
		JsonArray conditions = new JsonArray();
		conditions.add(1);
		conditions.add(2);
		conditions.add(3);
		conditions.add(4);
		conditions.add(5);
		page.add("conditions", conditions);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.conditions.length");
		assertEquals(new JsonPrimitive(5), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testArrayLengthMinus1WithSpaces() {
		JsonObject page = new JsonObject();
		JsonArray conditions = new JsonArray();
		conditions.add(1);
		conditions.add(2);
		conditions.add(3);
		conditions.add(4);
		conditions.add(5);
		page.add("conditions", conditions);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.conditions.length - 1");
		assertEquals(new JsonPrimitive(4), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testArrayLengthMinus1WithoutSpaces() {
		JsonObject page = new JsonObject();
		JsonArray conditions = new JsonArray();
		conditions.add(1);
		conditions.add(2);
		conditions.add(3);
		conditions.add(4);
		conditions.add(5);
		page.add("conditions", conditions);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.conditions.length-1");
		assertEquals(new JsonPrimitive(4), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testNestedArrayAccessWithLengthMinus1WithoutSpaces() {
		JsonObject page = new JsonObject();
		JsonObject mainFilterCondition = new JsonObject();
		JsonObject condition = new JsonObject();
		JsonArray conditions = new JsonArray();
		
		JsonObject item1 = new JsonObject();
		JsonArray item1Conditions = new JsonArray();
		item1Conditions.add(1);
		item1Conditions.add(2);
		item1Conditions.add(3);
		item1Conditions.add(4);
		item1.add("conditions", item1Conditions);
		
		JsonObject item2 = new JsonObject();
		JsonArray item2Conditions = new JsonArray();
		item2Conditions.add(5);
		item2Conditions.add(6);
		item2.add("conditions", item2Conditions);
		
		conditions.add(item1);
		conditions.add(item2);
		condition.add("conditions", conditions);
		mainFilterCondition.add("condition", condition);
		page.add("mainFilterCondition", mainFilterCondition);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator(
				"Page.mainFilterCondition.condition.conditions[0].conditions.length-1");
		assertEquals(new JsonPrimitive(3), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testNestedArrayAccessWithLengthMinus1WithSpaces() {
		JsonObject page = new JsonObject();
		JsonObject mainFilterCondition = new JsonObject();
		JsonObject condition = new JsonObject();
		JsonArray conditions = new JsonArray();
		
		JsonObject item1 = new JsonObject();
		JsonArray item1Conditions = new JsonArray();
		item1Conditions.add(1);
		item1Conditions.add(2);
		item1Conditions.add(3);
		item1Conditions.add(4);
		item1.add("conditions", item1Conditions);
		
		JsonObject item2 = new JsonObject();
		JsonArray item2Conditions = new JsonArray();
		item2Conditions.add(5);
		item2Conditions.add(6);
		item2.add("conditions", item2Conditions);
		
		conditions.add(item1);
		conditions.add(item2);
		condition.add("conditions", conditions);
		mainFilterCondition.add("condition", condition);
		page.add("mainFilterCondition", mainFilterCondition);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator(
				"Page.mainFilterCondition.condition.conditions[0].conditions.length - 1");
		assertEquals(new JsonPrimitive(3), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testArrayLengthWithAddition() {
		JsonObject page = new JsonObject();
		JsonArray items = new JsonArray();
		items.add(1);
		items.add(2);
		items.add(3);
		page.add("items", items);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.items.length+2");
		assertEquals(new JsonPrimitive(5), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testArrayLengthWithMultiplication() {
		JsonObject page = new JsonObject();
		JsonArray items = new JsonArray();
		items.add(1);
		items.add(2);
		items.add(3);
		page.add("items", items);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.items.length*2");
		assertEquals(new JsonPrimitive(6), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testArrayLengthInComplexExpression() {
		JsonObject page = new JsonObject();
		JsonArray arr1 = new JsonArray();
		arr1.add(1);
		arr1.add(2);
		arr1.add(3);
		JsonArray arr2 = new JsonArray();
		arr2.add(4);
		arr2.add(5);
		arr2.add(6);
		arr2.add(7);
		page.add("arr1", arr1);
		page.add("arr2", arr2);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// arr1.length + arr2.length - 1 = 3 + 4 - 1 = 6
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.arr1.length + Page.arr2.length - 1");
		assertEquals(new JsonPrimitive(6), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testUsingLengthMinus1AsArrayIndex() {
		JsonObject page = new JsonObject();
		JsonArray items = new JsonArray();
		items.add(10);
		items.add(20);
		items.add(30);
		items.add(40);
		items.add(50);
		page.add("items", items);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// Access last element: items[items.length - 1]
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.items[Page.items.length - 1]");
		assertEquals(new JsonPrimitive(50), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testUsingLengthMinus1WithoutSpacesAsArrayIndex() {
		JsonObject page = new JsonObject();
		JsonArray items = new JsonArray();
		items.add(10);
		items.add(20);
		items.add(30);
		items.add(40);
		items.add(50);
		page.add("items", items);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// Access last element: items[items.length-1]
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.items[Page.items.length-1]");
		assertEquals(new JsonPrimitive(50), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testNestedObjectWithArrayLengthSubtraction() {
		JsonObject page = new JsonObject();
		JsonObject data = new JsonObject();
		JsonObject nested = new JsonObject();
		JsonArray list = new JsonArray();
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("d");
		nested.add("list", list);
		data.add("nested", nested);
		page.add("data", data);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.data.nested.list.length-1");
		assertEquals(new JsonPrimitive(3), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testStringLengthWithSubtraction() {
		JsonObject page = new JsonObject();
		page.addProperty("text", "hello");
		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.text.length-1");
		assertEquals(new JsonPrimitive(4), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testObjectKeysLengthWithSubtraction() {
		JsonObject page = new JsonObject();
		JsonObject obj = new JsonObject();
		obj.addProperty("a", 1);
		obj.addProperty("b", 2);
		obj.addProperty("c", 3);
		obj.addProperty("d", 4);
		page.add("obj", obj);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// Object with 4 keys, length should be 4
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.obj.length-1");
		assertEquals(new JsonPrimitive(3), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testEmptyArrayLengthMinus1() {
		JsonObject page = new JsonObject();
		JsonArray empty = new JsonArray();
		page.add("empty", empty);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.empty.length-1");
		assertEquals(new JsonPrimitive(-1), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testMultipleLengthOperationsInSameExpression() {
		JsonObject page = new JsonObject();
		JsonArray list1 = new JsonArray();
		list1.add(1);
		list1.add(2);
		list1.add(3);
		JsonArray list2 = new JsonArray();
		list2.add(4);
		list2.add(5);
		page.add("list1", list1);
		page.add("list2", list2);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// (list1.length-1) * (list2.length-1) = 2 * 1 = 2
		ExpressionEvaluator ev = new ExpressionEvaluator("(Page.list1.length-1) * (Page.list2.length-1)");
		assertEquals(new JsonPrimitive(2), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testLengthWithDivision() {
		JsonObject page = new JsonObject();
		JsonArray items = new JsonArray();
		items.add(1);
		items.add(2);
		items.add(3);
		items.add(4);
		items.add(5);
		items.add(6);
		page.add("items", items);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.items.length/2");
		assertEquals(new JsonPrimitive(3), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testLengthWithModulus() {
		JsonObject page = new JsonObject();
		JsonArray items = new JsonArray();
		items.add(1);
		items.add(2);
		items.add(3);
		items.add(4);
		items.add(5);
		page.add("items", items);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.items.length%3");
		assertEquals(new JsonPrimitive(2), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testSimpleGreaterThanComparison() {
		JsonObject page = new JsonObject();
		JsonArray items = new JsonArray();
		items.add(1);
		items.add(2);
		items.add(3);
		page.add("items", items);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// Page.items.length > 0 = 3 > 0 = true
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.items.length > 0");
		assertEquals(new JsonPrimitive(true), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	// Alternative patterns with parentheses
	@Test
	void testAlternativeLengthComparisonWithParenthesesAndSpaces() {
		JsonObject page = new JsonObject();
		JsonArray items = new JsonArray();
		items.add(1);
		items.add(2);
		items.add(3);
		page.add("items", items);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// Using parentheses: (Page.items.length - 1) > 0
		ExpressionEvaluator ev = new ExpressionEvaluator("(Page.items.length - 1) > 0");
		assertEquals(new JsonPrimitive(true), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testAlternativeLengthComparisonWithParenthesesNoSpaces() {
		JsonObject page = new JsonObject();
		JsonArray items = new JsonArray();
		items.add(1);
		items.add(2);
		items.add(3);
		page.add("items", items);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// Using parentheses: (Page.items.length-1) > 0
		ExpressionEvaluator ev = new ExpressionEvaluator("(Page.items.length-1) > 0");
		assertEquals(new JsonPrimitive(true), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testAlternativeLiteralSubtractionAndComparisonWithParentheses() {
		PageTokenValueExtractor ttv = new PageTokenValueExtractor(new JsonObject());
		// Using parentheses: (5 - 1) > 0
		ExpressionEvaluator ev = new ExpressionEvaluator("(5 - 1) > 0");
		assertEquals(new JsonPrimitive(true), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testAlternativeChainedSubtractionWithParentheses() {
		PageTokenValueExtractor ttv = new PageTokenValueExtractor(new JsonObject());
		// Using parentheses: (5 - 1) - 2 = 4 - 2 = 2
		ExpressionEvaluator ev = new ExpressionEvaluator("(5 - 1) - 2");
		assertEquals(new JsonPrimitive(2), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testAlternativeLengthInTernaryWithParentheses() {
		JsonObject page = new JsonObject();
		JsonArray items = new JsonArray();
		items.add(1);
		items.add(2);
		items.add(3);
		items.add(4);
		page.add("items", items);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// Using parentheses: (Page.items.length - 1) > 2 ? 'big' : 'small'
		ExpressionEvaluator ev = new ExpressionEvaluator("(Page.items.length - 1) > 2 ? 'big' : 'small'");
		assertEquals(new JsonPrimitive("big"), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testAlternativeSubtractionWithEqualityUsingParentheses() {
		JsonObject page = new JsonObject();
		JsonArray items = new JsonArray();
		items.add(1);
		items.add(2);
		items.add(3);
		page.add("items", items);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// Using parentheses: (Page.items.length - 1) = 2
		ExpressionEvaluator ev = new ExpressionEvaluator("(Page.items.length - 1) = 2");
		assertEquals(new JsonPrimitive(true), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	// Previously known issues (now fixed)
	@Test
	void testFixedLengthComparisonWithoutParentheses() {
		JsonObject page = new JsonObject();
		JsonArray items = new JsonArray();
		items.add(1);
		items.add(2);
		items.add(3);
		page.add("items", items);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// This now works: Page.items.length - 1 > 0
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.items.length - 1 > 0");
		assertEquals(new JsonPrimitive(true), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testFixedSimpleSubtractionAndComparisonWithoutParentheses() {
		PageTokenValueExtractor ttv = new PageTokenValueExtractor(new JsonObject());
		// This now works: 5 - 1 > 0
		ExpressionEvaluator ev = new ExpressionEvaluator("5 - 1 > 0");
		assertEquals(new JsonPrimitive(true), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testFixedChainedSubtraction() {
		PageTokenValueExtractor ttv = new PageTokenValueExtractor(new JsonObject());
		// This now correctly evaluates as (5-1)-2 = 2
		ExpressionEvaluator ev = new ExpressionEvaluator("5 - 1 - 2");
		assertEquals(new JsonPrimitive(2), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testFixedSubtractionThenEqualityWithoutParentheses() {
		JsonObject page = new JsonObject();
		JsonArray items = new JsonArray();
		items.add(1);
		items.add(2);
		items.add(3);
		page.add("items", items);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// This now works: Page.items.length - 1 = 2
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.items.length - 1 = 2");
		assertEquals(new JsonPrimitive(true), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}

	@Test
	void testFixedLengthInTernaryWithoutParentheses() {
		JsonObject page = new JsonObject();
		JsonArray items = new JsonArray();
		items.add(1);
		items.add(2);
		items.add(3);
		items.add(4);
		page.add("items", items);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// This now works: Page.items.length - 1 > 2 ? 'big' : 'small'
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.items.length - 1 > 2 ? 'big' : 'small'");
		assertEquals(new JsonPrimitive("big"), ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
	}
}

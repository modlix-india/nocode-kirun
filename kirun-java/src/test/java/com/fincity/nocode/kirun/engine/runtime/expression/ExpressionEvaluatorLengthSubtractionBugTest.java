package com.fincity.nocode.kirun.engine.runtime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class ExpressionEvaluatorLengthSubtractionBugTest {

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
	void testNestedArrayAccessWithLengthMinus1ExactFailingCase() {
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
		// This is the exact expression from the bug report
		ExpressionEvaluator ev = new ExpressionEvaluator(
				"Page.mainFilterCondition.condition.conditions[0].conditions.length-1");
		
		// Expected result: 3 (array length 4 - 1)
		JsonElement result = assertDoesNotThrow(() -> ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
		assertEquals(new JsonPrimitive(3), result);
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
		
		conditions.add(item1);
		condition.add("conditions", conditions);
		mainFilterCondition.add("condition", condition);
		page.add("mainFilterCondition", mainFilterCondition);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator(
				"Page.mainFilterCondition.condition.conditions[0].conditions.length - 1");
		
		JsonElement result = assertDoesNotThrow(() -> ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
		assertEquals(new JsonPrimitive(3), result);
	}

	@Test
	void testFixedObjectWithLengthPropertyThatIsAnObject() {
		JsonObject page = new JsonObject();
		JsonObject obj = new JsonObject();
		JsonObject lengthObj = new JsonObject();
		lengthObj.addProperty("nested", "object");
		obj.add("length", lengthObj); // Object has a length property
		page.add("obj", obj);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// When accessing obj.length, it should return Object.keys(obj).length
		// (ignoring the length property on the object)
		// Then obj.length - 1 should work correctly
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.obj.length-1");
		
		// Should return Object.keys(obj).length - 1 = 1 - 1 = 0
		// (obj has one key: 'length')
		JsonElement result = ev.evaluate(Map.of(ttv.getPrefix(), ttv));
		assertEquals(new JsonPrimitive(0), result);
	}

	@Test
	void testNestedObjectWithLengthPropertyInPath() {
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
		JsonObject lengthObj = new JsonObject();
		lengthObj.addProperty("some", "object");
		item1.add("length", lengthObj); // Object has length property
		
		conditions.add(item1);
		condition.add("conditions", conditions);
		mainFilterCondition.add("condition", condition);
		page.add("mainFilterCondition", mainFilterCondition);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator(
				"Page.mainFilterCondition.condition.conditions[0].conditions.length-1");
		
		// Should still return 3 (array length 4 - 1), not affected by the length property on the parent
		JsonElement result = assertDoesNotThrow(() -> ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
		assertEquals(new JsonPrimitive(3), result);
	}

	@Test
	void testObjectWithLengthPropertyThatIsANumber() {
		JsonObject page = new JsonObject();
		JsonObject obj = new JsonObject();
		obj.addProperty("length", 5); // length property exists and is a number
		page.add("obj", obj);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// When obj has a length property, it should return that value
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.obj.length-1");
		JsonElement result = ev.evaluate(Map.of(ttv.getPrefix(), ttv));
		
		// Should return 5 - 1 = 4 (the length property value minus 1)
		assertEquals(new JsonPrimitive(4), result);
	}

	@Test
	void testArrayLengthShouldAlwaysWork() {
		JsonObject page = new JsonObject();
		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add(2);
		arr.add(3);
		arr.add(4);
		arr.add(5);
		page.add("arr", arr);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.arr.length-1");
		JsonElement result = ev.evaluate(Map.of(ttv.getPrefix(), ttv));
		
		// Arrays always have .length, should return 5 - 1 = 4
		assertEquals(new JsonPrimitive(4), result);
	}

	@Test
	void testDeeplyNestedStructureWithMultipleArrayAccesses() {
		JsonObject page = new JsonObject();
		JsonObject level1 = new JsonObject();
		JsonObject level2 = new JsonObject();
		JsonObject level3 = new JsonObject();
		JsonArray items = new JsonArray();
		
		JsonObject item1 = new JsonObject();
		JsonArray subItems1 = new JsonArray();
		subItems1.add("a");
		subItems1.add("b");
		subItems1.add("c");
		subItems1.add("d");
		subItems1.add("e");
		item1.add("subItems", subItems1);
		
		JsonObject item2 = new JsonObject();
		JsonArray subItems2 = new JsonArray();
		subItems2.add("f");
		subItems2.add("g");
		item2.add("subItems", subItems2);
		
		items.add(item1);
		items.add(item2);
		level3.add("items", items);
		level2.add("level3", level3);
		level1.add("level2", level2);
		page.add("level1", level1);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator(
				"Page.level1.level2.level3.items[0].subItems.length-1");
		
		JsonElement result = assertDoesNotThrow(() -> ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
		assertEquals(new JsonPrimitive(4), result); // 5 - 1
	}

	@Test
	void testFixedMultipleLengthOperationsInSameExpression() {
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
		arr2.add(8);
		page.add("arr1", arr1);
		page.add("arr2", arr2);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator(
				"Page.arr1.length-1 + Page.arr2.length-1");
		
		// Should work correctly now
		JsonElement result = assertDoesNotThrow(() -> ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
		
		// Expected result: (3-1) + (5-1) = 2 + 4 = 6
		assertEquals(new JsonPrimitive(6), result);
	}

	@Test
	void testLengthMinus1UsedAsArrayIndex() {
		JsonObject page = new JsonObject();
		JsonArray items = new JsonArray();
		items.add(10);
		items.add(20);
		items.add(30);
		items.add(40);
		items.add(50);
		page.add("items", items);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// Access last element using length-1
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.items[Page.items.length-1]");
		
		JsonElement result = assertDoesNotThrow(() -> ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
		assertEquals(new JsonPrimitive(50), result);
	}

	@Test
	void testStringLengthSubtraction() {
		JsonObject page = new JsonObject();
		page.addProperty("text", "hello world");
		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.text.length-1");
		JsonElement result = ev.evaluate(Map.of(ttv.getPrefix(), ttv));
		
		assertEquals(new JsonPrimitive(10), result); // 11 - 1
	}

	@Test
	void testEmptyArrayLengthMinus1() {
		JsonObject page = new JsonObject();
		JsonArray empty = new JsonArray();
		page.add("empty", empty);
		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.empty.length-1");
		JsonElement result = ev.evaluate(Map.of(ttv.getPrefix(), ttv));
		
		assertEquals(new JsonPrimitive(-1), result); // 0 - 1
	}

	@Test
	void testObjectWithNoLengthProperty() {
		JsonObject page = new JsonObject();
		JsonObject obj = new JsonObject();
		obj.addProperty("a", 1);
		obj.addProperty("b", 2);
		obj.addProperty("c", 3);
		page.add("obj", obj);
		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.obj.length-1");
		JsonElement result = ev.evaluate(Map.of(ttv.getPrefix(), ttv));
		
		assertEquals(new JsonPrimitive(2), result); // 3 keys - 1
	}

	@Test
	void testScenarioWhereIntermediateObjectHasLengthProperty() {
		JsonObject page = new JsonObject();
		JsonObject mainFilterCondition = new JsonObject();
		JsonObject condition = new JsonObject();
		JsonObject lengthObj = new JsonObject();
		lengthObj.addProperty("conflict", true);
		condition.add("length", lengthObj); // This might cause issues
		JsonArray conditions = new JsonArray();
		
		JsonObject item1 = new JsonObject();
		JsonArray item1Conditions = new JsonArray();
		item1Conditions.add(1);
		item1Conditions.add(2);
		item1Conditions.add(3);
		item1Conditions.add(4);
		item1.add("conditions", item1Conditions);
		
		conditions.add(item1);
		condition.add("conditions", conditions);
		mainFilterCondition.add("condition", condition);
		page.add("mainFilterCondition", mainFilterCondition);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		// Accessing conditions.length should still work (it's an array)
		ExpressionEvaluator ev = new ExpressionEvaluator(
				"Page.mainFilterCondition.condition.conditions[0].conditions.length-1");
		
		JsonElement result = assertDoesNotThrow(() -> ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
		assertEquals(new JsonPrimitive(3), result);
	}

	@Test
	void testScenarioWhereArrayElementHasLengthProperty() {
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
		JsonObject lengthObj = new JsonObject();
		lengthObj.addProperty("nested", "value");
		item1.add("length", lengthObj); // Array element has length
		
		conditions.add(item1);
		condition.add("conditions", conditions);
		mainFilterCondition.add("condition", condition);
		page.add("mainFilterCondition", mainFilterCondition);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator(
				"Page.mainFilterCondition.condition.conditions[0].conditions.length-1");
		
		// This should still work - we're accessing conditions.length (the array)
		// not the length property of the object
		JsonElement result = assertDoesNotThrow(() -> ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
		assertEquals(new JsonPrimitive(3), result);
	}

	@Test
	void testLengthMinus1WithoutSpacesInComplexPath() {
		JsonObject page = new JsonObject();
		JsonObject data = new JsonObject();
		JsonObject nested = new JsonObject();
		JsonArray list = new JsonArray();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);
		nested.add("list", list);
		data.add("nested", nested);
		page.add("data", data);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.data.nested.list.length-1");
		
		JsonElement result = assertDoesNotThrow(() -> ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
		assertEquals(new JsonPrimitive(5), result);
	}

	@Test
	void testLengthMinus1WithSpacesInComplexPath() {
		JsonObject page = new JsonObject();
		JsonObject data = new JsonObject();
		JsonObject nested = new JsonObject();
		JsonArray list = new JsonArray();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);
		nested.add("list", list);
		data.add("nested", nested);
		page.add("data", data);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("Page.data.nested.list.length - 1");
		
		JsonElement result = assertDoesNotThrow(() -> ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
		assertEquals(new JsonPrimitive(5), result);
	}

	@Test
	void testParenthesesAroundLengthMinus1() {
		JsonObject page = new JsonObject();
		JsonArray items = new JsonArray();
		items.add(1);
		items.add(2);
		items.add(3);
		items.add(4);
		page.add("items", items);

		PageTokenValueExtractor ttv = new PageTokenValueExtractor(page);
		ExpressionEvaluator ev = new ExpressionEvaluator("(Page.items.length-1)");
		
		JsonElement result = assertDoesNotThrow(() -> ev.evaluate(Map.of(ttv.getPrefix(), ttv)));
		assertEquals(new JsonPrimitive(3), result);
	}
}

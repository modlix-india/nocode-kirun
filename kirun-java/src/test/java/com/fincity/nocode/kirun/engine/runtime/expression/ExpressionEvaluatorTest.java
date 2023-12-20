package com.fincity.nocode.kirun.engine.runtime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.ArgumentsTokenValueExtractor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class ExpressionEvaluatorTest {

	@Test
	void test() {

		JsonObject phone = new JsonObject();
		phone.addProperty("phone1", "1234");
		phone.addProperty("phone2", "5678");
		phone.addProperty("phone3", "5678");

		JsonObject address = new JsonObject();
		address.addProperty("line1", "Flat 202, PVR Estates");
		address.addProperty("line2", "Nagvara");
		address.addProperty("city", "Benguluru");
		address.addProperty("pin", "560048");
		address.add("phone", phone);

		JsonArray arr = new JsonArray();
		arr.add(10);
		arr.add(20);
		arr.add(30);

		JsonObject obj = new JsonObject();
		obj.add("studentName", new JsonPrimitive("Kumar"));
		obj.add("math", new JsonPrimitive(20));
		obj.add("isStudent", new JsonPrimitive(true));
		obj.add("address", address);
		obj.add("array", arr);
		obj.add("num", new JsonPrimitive(1));

		Map<String, Map<String, Map<String, JsonElement>>> output = Map.of("step1",
		        Map.of("output", Map.of("name", new JsonPrimitive("Kiran"), "obj", obj)));

		ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
		        new KIRunReactiveSchemaRepository()).setArguments(Map.of())
		        .setContext(Map.of())
		        .setSteps(output);

		assertEquals(new JsonPrimitive(10), new ExpressionEvaluator("3 + 7").evaluate(parameters.getValuesMap()));
		assertEquals(new JsonPrimitive("asdf333"),
		        new ExpressionEvaluator("\"asdf\"+333").evaluate(parameters.getValuesMap()));
		assertEquals(new JsonPrimitive(422),
		        new ExpressionEvaluator("10*11+12*13*14/7").evaluate(parameters.getValuesMap()));
		assertEquals(new JsonPrimitive(true),
		        new ExpressionEvaluator("34 >> 2 = 8 ").evaluate(parameters.getValuesMap()));

		assertEquals(new JsonPrimitive(true),
		        new ExpressionEvaluator("34 >> 2 = 8 ").evaluate(parameters.getValuesMap()));

		assertEquals(null, new ExpressionEvaluator("Steps.step1.output.name1").evaluate(parameters.getValuesMap()));

		assertEquals(new JsonPrimitive(true),
		        new ExpressionEvaluator("\"Kiran\" = Steps.step1.output.name ").evaluate(parameters.getValuesMap()));

		assertEquals(new JsonPrimitive(true),
		        new ExpressionEvaluator("null = Steps.step1.output.name1 ").evaluate(parameters.getValuesMap()));

		assertEquals(new JsonPrimitive(true),
		        new ExpressionEvaluator("Steps.step1.output.obj.phone.phone2 = Steps.step1.output.obj.phone.phone2 ")
		                .evaluate(parameters.getValuesMap()));

		assertEquals(new JsonPrimitive(true),
		        new ExpressionEvaluator(
		                "Steps.step1.output.obj.address.phone.phone2 != Steps.step1.output.address.obj.phone.phone1 ")
		                .evaluate(parameters.getValuesMap()));

		assertEquals(new JsonPrimitive(32),
		        new ExpressionEvaluator("Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+2")
		                .evaluate(parameters.getValuesMap()));

		assertEquals(new JsonPrimitive(60), new ExpressionEvaluator(
		        "Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]")
		        .evaluate(parameters.getValuesMap()));

		assertEquals(new JsonPrimitive(60), new ExpressionEvaluator(
		        "Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]")
		        .evaluate(parameters.getValuesMap()));

		assertEquals(new JsonPrimitive(32),
		        new ExpressionEvaluator("Steps.step1.output.obj.array[-Steps.step1.output.obj.num + 3]+2")
		                .evaluate(parameters.getValuesMap()));

		assertEquals(new JsonPrimitive(17.3533f),
		        new ExpressionEvaluator("2.43*4.22+7.0987").evaluate(parameters.getValuesMap()));
	}

	@Test
	void deepTest() {

		var cobj = new JsonObject();
		var dobj = new JsonObject();

		cobj.addProperty("a", 2);
		dobj.addProperty("a", 2);

		var cbArray = new JsonArray();
		cbArray.add(true);
		cbArray.add(false);
		cobj.add("b", cbArray);

		var dbArray = new JsonArray();
		dbArray.add(true);
		dbArray.add(false);
		dobj.add("b", dbArray);

		var ccObj = new JsonObject();
		ccObj.addProperty("x", "kiran");

		var dcObj = new JsonObject();
		dcObj.addProperty("x", "kiran");

		cobj.add("c", ccObj);
		dobj.add("c", dcObj);

		ArgumentsTokenValueExtractor atv = new ArgumentsTokenValueExtractor(
		        Map.of("a", new JsonPrimitive("kirun "), "b", new JsonPrimitive(2), "c", cobj, "d", dobj));
		Map<String, TokenValueExtractor> valuesMap = Map.of(atv.getPrefix(), atv);

		var ev = new ExpressionEvaluator("Arguments.a = Arugments.b");
		assertFalse(ev.evaluate(valuesMap)
		        .getAsBoolean());

		ev = new ExpressionEvaluator("Arguments.c = Arguments.d");
		assertTrue(ev.evaluate(valuesMap)
		        .getAsBoolean());

		ev = new ExpressionEvaluator("Arguments.e = null");
		assertTrue(ev.evaluate(valuesMap)
		        .getAsBoolean());

		ev = new ExpressionEvaluator("Arguments.e != null");
		assertFalse(ev.evaluate(valuesMap)
		        .getAsBoolean());

		ev = new ExpressionEvaluator("false = false");
		assertTrue(ev.evaluate(valuesMap)
		        .getAsBoolean());

		ev = new ExpressionEvaluator("Arguments.e = false");
		assertTrue(ev.evaluate(valuesMap)
		        .getAsBoolean());
	}

	@Test
	void testNullCoalescing() {

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
		        "b", new JsonPrimitive(2), "b2", new JsonPrimitive(4), "c", cobj));
		Map<String, TokenValueExtractor> valuesMap = Map.of(atv.getPrefix(), atv);

		var ev = new ExpressionEvaluator("(Arguments.e ?? Arguments.b ?? Arguments.b1) + 4");
		assertEquals(6, ev.evaluate(valuesMap)
		        .getAsInt());

		ev = new ExpressionEvaluator("(Arguments.e ?? Arguments.b2 ?? Arguments.b1) + 4");
		assertEquals(8, ev.evaluate(valuesMap)
		        .getAsInt());
	}

	@Test
	void testNestedExpression() {

		var cobj = new JsonObject();
		cobj.addProperty("a", 2);

		var cbArray = new JsonArray();
		cbArray.add(true);
		cbArray.add(false);
		cobj.add("b", cbArray);

		var ccObj = new JsonObject();
		ccObj.addProperty("x", "Arguments.b2");

		cobj.add("c", ccObj);

		ArgumentsTokenValueExtractor atv = new ArgumentsTokenValueExtractor(Map.of("a", new JsonPrimitive("kirun "),
		        "b", new JsonPrimitive(2), "b2", new JsonPrimitive(4), "c", cobj, "d", new JsonPrimitive("c")));
		Map<String, TokenValueExtractor> valuesMap = Map.of(atv.getPrefix(), atv);

		var ev = new ExpressionEvaluator("Arguments.{{Arguments.d}}.a + {{Arguments.{{Arguments.d}}.c.x}}");

		assertEquals(6, ev.evaluate(valuesMap)
		        .getAsInt());

		ev = new ExpressionEvaluator(
		        "'There are {{{{Arguments.{{Arguments.d}}.c.x}}}} boys in the class room...' * Arguments.b");

		assertEquals("There are 4 boys in the class room...There are 4 boys in the class room...",
		        ev.evaluate(valuesMap)
		                .getAsString());
	}

	@Test
	void testPartialPathEvaluation() {

		var cobj = new JsonObject();
		cobj.addProperty("a", 2);

		var cbArray = new JsonArray();
		cbArray.add(true);
		cbArray.add(false);
		cobj.add("b", cbArray);

		var ccObj = new JsonObject();
		ccObj.addProperty("x", "Arguments.b2");

		cobj.add("c", ccObj);

		var cKeysArray = new JsonArray();
		cKeysArray.add(new JsonPrimitive("a"));
		cKeysArray.add(new JsonPrimitive("e"));

		var keys2Obj = new JsonObject();
		keys2Obj.add("val", new JsonPrimitive(5));
		cKeysArray.add(keys2Obj);
		cobj.add("keys", cKeysArray);

		var eArray = new JsonArray();
		var e1 = new JsonObject();
		e1.add("name", new JsonPrimitive("Kiran"));
		e1.add("num", new JsonPrimitive(1));

		var e2 = new JsonObject();
		e2.add("name", new JsonPrimitive("Good"));
		e2.add("num", new JsonPrimitive(2));
		eArray.add(e1);
		eArray.add(e2);

		ArgumentsTokenValueExtractor atv = new ArgumentsTokenValueExtractor(
		        Map.of("a", new JsonPrimitive("kirun "), "b", new JsonPrimitive(2), "b2", new JsonPrimitive(4), "c",
		                cobj, "d", new JsonPrimitive("c"), "e", eArray));

		Map<String, TokenValueExtractor> valuesMap = Map.of(atv.getPrefix(), atv);

		var ev = new ExpressionEvaluator("(Arguments.f ?? Arguments.e)[1+1-1].num");
		assertEquals(new JsonPrimitive(2), ev.evaluate(valuesMap));
		
		ev = new ExpressionEvaluator("Arguments.c.keys[2].val + 3");
		assertEquals(new JsonPrimitive(8), ev.evaluate(valuesMap));

	}
	
	@Test
	void testEmptyStringwithNULLs() {
		
		ArgumentsTokenValueExtractor atv = new ArgumentsTokenValueExtractor(
		        Map.of("a", new JsonPrimitive(""), "b", new JsonPrimitive("Kiran")));
		
		Map<String, TokenValueExtractor> valuesMap = Map.of(atv.getPrefix(), atv);
		
		var ev = new ExpressionEvaluator("Arguments.a = ''");
		assertEquals(new JsonPrimitive(true), ev.evaluate(valuesMap));
		
		ev = new ExpressionEvaluator("Arguments.a != ''");
		assertEquals(new JsonPrimitive(false), ev.evaluate(valuesMap));
		
		ev = new ExpressionEvaluator("Arguments.b != ''");
		assertEquals(new JsonPrimitive(true), ev.evaluate(valuesMap));
		
		ev = new ExpressionEvaluator("Arguments.b = ''");
		assertEquals(new JsonPrimitive(false), ev.evaluate(valuesMap));
	}
	
	
	@Test
	void testconsecutivenegatives() {
		
		ArgumentsTokenValueExtractor atv = new ArgumentsTokenValueExtractor(
		        Map.of("a", new JsonPrimitive("kirun"), "b", new JsonPrimitive(1),"b1",new JsonPrimitive(4),"b2",new JsonPrimitive(4)));
		
		Map<String, TokenValueExtractor> valuesMap = Map.of(atv.getPrefix(), atv);
		 ExpressionEvaluator ev = new ExpressionEvaluator("Arguments.b - Arguments.b1 - Arguments.b2");
	        JsonElement result = ev.evaluate(valuesMap);
		assertEquals(new JsonPrimitive(-7),ev.evaluate(valuesMap));
		
	}
	
	@Test
	void Expressionwithmultiplecoalesceoperator() {
		
		ArgumentsTokenValueExtractor atv = new ArgumentsTokenValueExtractor(
		        Map.of("a", new JsonPrimitive("kirun"), "b", new JsonPrimitive(1),"b1",new JsonPrimitive(4),"b2",new JsonPrimitive(4)));
		
		Map<String, TokenValueExtractor> valuesMap = Map.of(atv.getPrefix(), atv);
		 ExpressionEvaluator ev = new ExpressionEvaluator("Arguments.b3 ?? (Arguments.b - 3) ?? Arguments.b5 ?? 4");
	        JsonElement result = ev.evaluate(valuesMap);
		assertEquals(new JsonPrimitive(-2),ev.evaluate(valuesMap));
		
	}
	
	
}

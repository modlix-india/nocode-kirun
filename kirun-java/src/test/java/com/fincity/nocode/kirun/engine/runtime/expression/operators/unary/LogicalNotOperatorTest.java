package com.fincity.nocode.kirun.engine.runtime.expression.operators.unary;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class LogicalNotOperatorTest {

	@Test
	void test() {

		assertTrue(new LogicalNotOperator().apply(null)
		        .getAsBoolean());
		assertTrue(new LogicalNotOperator().apply(JsonNull.INSTANCE)
		        .getAsBoolean());
		assertTrue(new LogicalNotOperator().apply(new JsonPrimitive(false))
		        .getAsBoolean());
		assertTrue(new LogicalNotOperator().apply(new JsonPrimitive(0))
		        .getAsBoolean());
		
		assertFalse(new LogicalNotOperator().apply(new JsonPrimitive(true))
		        .getAsBoolean());
		assertFalse(new LogicalNotOperator().apply(new JsonPrimitive(1))
		        .getAsBoolean());
		JsonObject job = new JsonObject();
		job.addProperty("name", "Kiran");
		
		assertFalse(new LogicalNotOperator().apply(job).getAsBoolean());
		
		JsonArray ja = new JsonArray();
		assertFalse(new LogicalNotOperator().apply(ja).getAsBoolean());
		assertFalse(new LogicalNotOperator().apply(new JsonPrimitive("")).getAsBoolean());
		assertFalse(new LogicalNotOperator().apply(new JsonPrimitive("TRUE")).getAsBoolean());
	}

}

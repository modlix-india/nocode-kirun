package com.fincity.nocode.kirun.engine.function.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.ExecutionException;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuples;

class PrimitiveUtilTest {

	@Test
	void testFindPrimitive() {

		assertEquals(PrimitiveUtil.findPrimitiveNullAsBoolean(new JsonPrimitive(true)), Tuples.of(SchemaType.BOOLEAN, true));
		assertEquals(PrimitiveUtil.findPrimitiveNullAsBoolean(new JsonPrimitive("Kiran")), Tuples.of(SchemaType.STRING, "Kiran"));
		assertEquals(PrimitiveUtil.findPrimitiveNullAsBoolean(new JsonPrimitive(10d)), Tuples.of(SchemaType.DOUBLE, 10d));
	}

	@Test
	void testFindPrimitiveNumberType() {

		assertEquals(PrimitiveUtil.findPrimitiveNumberType(new JsonPrimitive(10.0d)),
		        Tuples.of(SchemaType.DOUBLE, 10.0d));
		assertEquals(PrimitiveUtil.findPrimitiveNumberType(new JsonPrimitive(10l)), Tuples.of(SchemaType.LONG, 10l));
		assertEquals(PrimitiveUtil.findPrimitiveNumberType(new JsonPrimitive(10.0f)),
		        Tuples.of(SchemaType.FLOAT, 10.0f));
		assertEquals(PrimitiveUtil.findPrimitiveNumberType(new JsonPrimitive(10)), Tuples.of(SchemaType.INTEGER, 10));

		var stringJson = new JsonPrimitive("asdf");
		assertThrows(ExecutionException.class, () -> PrimitiveUtil.findPrimitiveNumberType(stringJson));
		
		var booleanJson = new JsonPrimitive(false);
		assertThrows(ExecutionException.class, () -> PrimitiveUtil.findPrimitiveNumberType(booleanJson));
		
		// Did you know?
		assertEquals(false, new JsonPrimitive("true").isBoolean());
		assertEquals(false, new JsonPrimitive(2).isString());
	}

	@Test
	void testToPrimitiveType() {

		assertEquals(PrimitiveUtil.toPrimitiveType(10), new JsonPrimitive(10));
		assertEquals(PrimitiveUtil.toPrimitiveType(10.0), new JsonPrimitive(10));
	}

}

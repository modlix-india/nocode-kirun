package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.ContextElement;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.ContextTokenValueExtractor;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

class ContextTokenValueExtractorTest {

	@Test
	void test() {

		var arr = new JsonArray();
		arr.add(new JsonPrimitive(1));
		arr.add(new JsonPrimitive(2));
		arr.add(new JsonPrimitive(3));
		arr.add(new JsonPrimitive(4));
		var vEx = new ContextTokenValueExtractor(Map.of("a", new ContextElement().setElement(arr)));

		assertEquals(new JsonPrimitive(3), vEx.getValue("Context.a[2]"));
	}

}

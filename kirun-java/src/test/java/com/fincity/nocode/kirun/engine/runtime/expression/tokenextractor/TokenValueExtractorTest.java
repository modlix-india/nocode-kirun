package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class TokenValueExtractorTest {

	private TokenValueExtractor extractor;

	@BeforeEach
	void beforeEach() {
		this.extractor = new TokenValueExtractor() {

			@Override
			protected JsonElement getValueInternal(String token) {
				return null;
			}

			@Override
			public String getPrefix() {
				return "Testing";
			}
		};
	}

	@Test
	void test() {

		JsonArray darr = new JsonArray();
		JsonArray darr0 = new JsonArray();
		darr0.add(2);
		darr0.add(4);
		darr0.add(6);
		JsonArray darr1 = new JsonArray();
		darr1.add(3);
		darr1.add(6);
		darr1.add(9);
		JsonArray darr2 = new JsonArray();
		darr2.add(4);
		darr2.add(8);
		darr2.add(12);
		darr2.add(16);

		darr.add(darr0);
		darr.add(darr1);
		darr.add(darr2);

		JsonArray arr = new JsonArray();
		arr.add(0);
		arr.add(2);
		arr.add(4);
		arr.add(6);

		JsonObject b = new JsonObject();
		b.add("c", new JsonPrimitive("K"));
		b.add("arr", arr);
		b.add("darr", darr);

		JsonObject a = new JsonObject();
		a.add("b", b);

		JsonObject obj = new JsonObject();
		obj.add("a", a);
		obj.add("array", arr);

		String token = "[2]";
		assertEquals(new JsonPrimitive(4), this.extractor.retrieveElementFrom(token, token.split("\\."), 0, arr));
		
		token="[1][1]";
		assertEquals(new JsonPrimitive(6), this.extractor.retrieveElementFrom(token, token.split("\\."), 0, darr));
		
		token="[2].length";
		assertEquals(new JsonPrimitive(4), this.extractor.retrieveElementFrom(token, token.split("\\."), 0, darr));

		token = "a.b.c";
		assertEquals("K", this.extractor.retrieveElementFrom(token, token.split("\\."), 0, obj)
		        .getAsString());

		token = "a.b";
		assertEquals(b, this.extractor.retrieveElementFrom(token, token.split("\\."), 0, obj));

		token = "a.b.c";
		assertEquals("K", this.extractor.retrieveElementFrom(token, token.split("\\."), 1, a)
		        .getAsString());

		token = "a.b.arr[2]";
		assertEquals(new JsonPrimitive(4), this.extractor.retrieveElementFrom(token, token.split("\\."), 1, a));

		token = "a.b.darr[2][3]";
		assertEquals(new JsonPrimitive(16), this.extractor.retrieveElementFrom(token, token.split("\\."), 1, a));

		token = "a.b.darr[2].length";
		assertEquals(new JsonPrimitive(4), this.extractor.retrieveElementFrom(token, token.split("\\."), 1, a));
	}

}

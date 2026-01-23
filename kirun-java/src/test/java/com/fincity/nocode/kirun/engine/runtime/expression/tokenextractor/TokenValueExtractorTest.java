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

			@Override
			public JsonElement getStore() {
				return null;
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

		token = "[1][1]";
		assertEquals(new JsonPrimitive(6), this.extractor.retrieveElementFrom(token, token.split("\\."), 0, darr));

		token = "[2].length";
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

	@Test
	void testBracketNotationWithDottedKeys() {
		// Test bracket notation with keys containing dots
		JsonObject config = new JsonObject();
		config.add("mail.props.port", new JsonPrimitive(587));
		config.add("mail.props.host", new JsonPrimitive("smtp.example.com"));
		config.add("api.key.secret", new JsonPrimitive("secret123"));
		config.add("simple", new JsonPrimitive("value"));

		JsonObject obj = new JsonObject();
		obj.add("config", config);

		// Test with double quotes
		String token = "config[\"mail.props.port\"]";
		assertEquals(new JsonPrimitive(587),
			this.extractor.retrieveElementFrom(token, TokenValueExtractor.splitPath(token), 0, obj));

		// Test with single quotes
		token = "config['mail.props.host']";
		assertEquals(new JsonPrimitive("smtp.example.com"),
			this.extractor.retrieveElementFrom(token, TokenValueExtractor.splitPath(token), 0, obj));

		// Test nested bracket notation with dots
		token = "config['api.key.secret']";
		assertEquals(new JsonPrimitive("secret123"),
			this.extractor.retrieveElementFrom(token, TokenValueExtractor.splitPath(token), 0, obj));

		// Test mix of dot and bracket notation
		JsonObject nested = new JsonObject();
		nested.add("field.with.dots", new JsonPrimitive("nestedValue"));
		config.add("nested", nested);

		token = "config.nested['field.with.dots']";
		assertEquals(new JsonPrimitive("nestedValue"),
			this.extractor.retrieveElementFrom(token, TokenValueExtractor.splitPath(token), 0, obj));

		// Test that regular dot notation still works
		token = "config.simple";
		assertEquals(new JsonPrimitive("value"),
			this.extractor.retrieveElementFrom(token, TokenValueExtractor.splitPath(token), 0, obj));
	}

	@Test
	void testSplitPath() {
		// Test that splitPath correctly handles bracket notation
		String[] parts;

		parts = TokenValueExtractor.splitPath("Context.obj['mail.props.port']");
		assertEquals(2, parts.length);
		assertEquals("Context", parts[0]);
		assertEquals("obj['mail.props.port']", parts[1]);

		parts = TokenValueExtractor.splitPath("Context.obj['mail.props.port'].value");
		assertEquals(3, parts.length);
		assertEquals("Context", parts[0]);
		assertEquals("obj['mail.props.port']", parts[1]);
		assertEquals("value", parts[2]);

		parts = TokenValueExtractor.splitPath("Steps.source.output['field.name']");
		assertEquals(3, parts.length);
		assertEquals("Steps", parts[0]);
		assertEquals("source", parts[1]);
		assertEquals("output['field.name']", parts[2]);

		// Test that range operator (..) is preserved
		parts = TokenValueExtractor.splitPath("array[0..5]");
		assertEquals(1, parts.length);
		assertEquals("array[0..5]", parts[0]);

		// Test multiple bracket notations
		parts = TokenValueExtractor.splitPath("obj['key.one']['key.two']");
		assertEquals(1, parts.length);
		assertEquals("obj['key.one']['key.two']", parts[0]);
	}

}

package com.fincity.nocode.kirun.engine.json.schema.reactive;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.google.gson.JsonParser;

import reactor.test.StepVerifier;

// The type adapter reads a type in any case (and "number" as DOUBLE), so the Schema meta-schema
// must accept the same spellings.
class ReactiveSchemaTypeSpellingTest {

	private final KIRunReactiveSchemaRepository repo = new KIRunReactiveSchemaRepository();

	@Test
	void acceptsTypeSpellings() {

		for (String type : new String[] { "OBJECT", "Object", "object", "Number", "number", "NUMBER" }) {

			var value = JsonParser
					.parseString("{\"type\":\"" + type + "\",\"properties\":{\"a\":{\"type\":\"String\"}}}");

			StepVerifier.create(ReactiveSchemaValidator.validate(null, Schema.SCHEMA, repo, value))
					.expectNextCount(1)
					.verifyComplete();
		}
	}

	@Test
	void acceptsMixedTypeArray() {

		var value = JsonParser.parseString("{\"type\":[\"Object\",\"NULL\",\"string\"]}");

		StepVerifier.create(ReactiveSchemaValidator.validate(null, Schema.SCHEMA, repo, value))
				.expectNextCount(1)
				.verifyComplete();
	}

	@Test
	void rejectsUnknownType() {

		var value = JsonParser.parseString("{\"type\":\"Thing\"}");

		StepVerifier.create(ReactiveSchemaValidator.validate(null, Schema.SCHEMA, repo, value))
				.expectError()
				.verify();
	}
}

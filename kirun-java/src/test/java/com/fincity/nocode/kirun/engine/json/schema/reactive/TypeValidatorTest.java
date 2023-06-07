package com.fincity.nocode.kirun.engine.json.schema.reactive;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.validator.BooleanValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.NumberValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.StringValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveArrayValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveTypeValidator;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

class TypeValidatorTest {

	@Test
	void testTypeValidator() {

		// For Number
		JsonObject element = new JsonObject();
		element.addProperty("value", 123);

		SchemaType type = SchemaType.INTEGER;
		Schema schema = new Schema();

		StepVerifier.create(ReactiveTypeValidator.validate(null, type, schema, null, element.get("value")))
		        .expectNext(NumberValidator.validate(type, null, schema, element.get("value")))
		        .verifyComplete();

		// For String
		type = SchemaType.STRING;
		JsonObject elementString = new JsonObject();
		elementString.addProperty("value", "string");

		StepVerifier.create(ReactiveTypeValidator.validate(null, type, schema, null, elementString.get("value")))
		        .expectNext(StringValidator.validate(null, schema, elementString.get("value")))
		        .verifyComplete();

		// For Boolean
		type = SchemaType.BOOLEAN;
		JsonObject elementBoolean = new JsonObject();
		elementBoolean.addProperty("value", Boolean.TRUE);

		StepVerifier.create(ReactiveTypeValidator.validate(null, type, schema, null, elementBoolean.get("value")))
		        .expectNext(BooleanValidator.validate(null, schema, elementBoolean.get("value")))
		        .verifyComplete();

		// For Array
		type = SchemaType.ARRAY;
		JsonArray array = new JsonArray();
		array.add("abc");

		StepVerifier
		        .create(Mono.zip(ReactiveTypeValidator.validate(null, type, schema, null, array),
		                ReactiveArrayValidator.validate(null, schema, null, array)))
		        .expectNext(Tuples.of(array, array))
		        .verifyComplete();

		// For Null
		type = SchemaType.NULL;

		StepVerifier.create(ReactiveTypeValidator.validate(null, type, schema, null, null))
		        .expectNext(JsonNull.INSTANCE)
		        .verifyComplete();

		// For Exception;
		type = null;

		StepVerifier.create(ReactiveTypeValidator.validate(null, null, schema, null, element))
		        .verifyErrorMessage("null is not a valid type.");
	}

}

package com.fincity.nocode.kirun.engine.json.schema.reactive;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ReactiveNotValidatorTest {

	@Test
	void notValidationTest() {
		var sch = Schema
		        .of("Not Schema", SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE,
		                SchemaType.STRING)
		        .setDefaultValue(new JsonPrimitive(1))
		        .setNot(Schema.of("Not Schema", SchemaType.STRING));

		var value = ReactiveSchemaValidator.validate(null, sch, null, new JsonPrimitive(0));

		StepVerifier.create(value)
		        .expectNext(new JsonPrimitive(0))
		        .verifyComplete();

		sch = Schema
		        .of("Not Schema", SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE,
		                SchemaType.STRING)
		        .setDefaultValue(new JsonPrimitive(1))
		        .setNot(Schema.of("Not Schema", SchemaType.INTEGER));

		value = ReactiveSchemaValidator.validate(null, sch, null, new JsonPrimitive(0));

		StepVerifier.create(value)
		        .verifyError();

		sch = Schema.of("Not Schema", SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT)
		        .setDefaultValue(new JsonPrimitive(1))
		        .setNot(new Schema().setConstant(new JsonPrimitive(0)));

		value = ReactiveSchemaValidator.validate(null, sch, null, new JsonPrimitive(0));

		StepVerifier.create(value)
		        .verifyError();

		StepVerifier.create(ReactiveSchemaValidator.validate(null, sch, null, null))
		        .expectNext(new JsonPrimitive(1))
		        .verifyComplete();

		StepVerifier.create(ReactiveSchemaValidator.validate(null, sch, null, JsonNull.INSTANCE))
		        .expectNext(new JsonPrimitive(1))
		        .verifyComplete();

		StepVerifier.create(ReactiveSchemaValidator.validate(null, sch, null, new JsonPrimitive(2)))
		        .expectNext(new JsonPrimitive(2))
		        .verifyComplete();
		;
	}

}

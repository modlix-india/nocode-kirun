package com.fincity.nocode.kirun.engine.json.schema.reactive;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ReactiveConstantValidatorTest {

	@Test
	void notValidationTest() {

		var sch = Schema.of("Constant Schema", SchemaType.INTEGER)
		        .setConstant(new JsonPrimitive(1));

		var value = ReactiveSchemaValidator.validate(null, sch, null, new JsonPrimitive(1));
		StepVerifier.create(value)
		        .expectNext(new JsonPrimitive(1))
		        .verifyComplete();

		StepVerifier.create(ReactiveSchemaValidator.validate(null, sch, null, new JsonPrimitive(0)))
		        .verifyError();

	}

}

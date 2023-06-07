package com.fincity.nocode.kirun.engine.json.schema.reactive;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveArrayValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ReactiveArrayValidatorTest {
	Schema schema = new Schema();

	@Test
	void ArrayValidatorValidateTestForNull() {

		JsonArray element = null;
		Schema schema = new Schema();

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectErrorMessage("Expected an array but found null")
		        .verify();

	}

	@Test
	void ArrayValidatorValidateTestForMinItems() {

		JsonArray element = new JsonArray();

		element.add("chile");
		element.add(16);
		element.add("San Francisco");
		element.add(800.0);
		element.add(18);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setUniqueItems(Boolean.TRUE);
		schema.setMaxItems(3);
		schema.setMinItems(5);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, element))
		        .expectErrorMessage("Value [\"chile\",16,\"San Francisco\",800.0,18] is not of valid type(s)\n"
		                + "Array can have  maximum of 3 elements")
		        .verify();
	}

	@Test
	void ArrayValidatorValidateTestForMaxItems() {

		JsonArray element = new JsonArray();

		element.add("chile");
		element.add(16);
		element.add("San Francisco");
		element.add(800.0);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setUniqueItems(Boolean.TRUE);
		schema.setMaxItems(3);
		schema.setMinItems(2);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectErrorMessage("Array can have  maximum of " + schema.getMaxItems() + " elements")
		        .verify();
	}

	@Test
	void ArrayValidatorValidateTestToCheckItems() {

		JsonArray element = new JsonArray();

		element.add("chile");
		element.add(13);
		element.add(13);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setUniqueItems(Boolean.TRUE);
		schema.setMaxItems(2);
		schema.setMinItems(1);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectErrorMessage("Array can have  maximum of 2 elements")
		        .verify();
	}

	@Test
	void ArrayValidatorValidateTestForUniqueItems() {

		JsonArray element = new JsonArray();

		element.add("chile");
		element.add(16);
		element.add("San Francisco");
		element.add(16);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setUniqueItems(Boolean.TRUE);
		schema.setMaxItems(7);
		schema.setMinItems(2);
		schema.setUniqueItems(Boolean.TRUE);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectErrorMessage("Items on the array are not unique")
		        .verify();
	}

	@Test
	void ArrayValidatorValidateTestForContainItems() {

		JsonArray element = new JsonArray();

		element.add("chiles");
		element.add(15);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setMaxItems(7);
		schema.setMinItems(2);

		JsonObject constantElement = new JsonObject();
		constantElement.addProperty("value", "constant");

		Schema schemaContains = new Schema();
		schemaContains.setType(Type.of(SchemaType.STRING));
		schemaContains.setConstant(new JsonPrimitive("chile"));

		schema.setContains(schemaContains);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectErrorMessage("None of the items are of type contains schema")
		        .verify();

	}
}

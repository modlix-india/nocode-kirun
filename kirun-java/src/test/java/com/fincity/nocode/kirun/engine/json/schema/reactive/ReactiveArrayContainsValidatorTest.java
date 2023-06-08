package com.fincity.nocode.kirun.engine.json.schema.reactive;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveArrayValidator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import reactor.test.StepVerifier;

class ReactiveArrayContainsValidatorTest {
	Schema schema = new Schema();

	@Test
	void ArrayValidatorTestForContainsSchema() {

		JsonObject job = new JsonObject();
		job.addProperty("name", "jimmy mcgill");

		JsonArray element = new JsonArray();
		element.add("texas");
		element.add("kentucky");
		element.add(job);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setContains(Schema.ofObject("object"));

		List<Schema> tupleSchema = new ArrayList<>();
		tupleSchema.add(Schema.ofString("item1"));
		tupleSchema.add(Schema.ofString("item2"));
		tupleSchema.add(Schema.ofObject("item3"));

		ArraySchemaType ast = new ArraySchemaType();
		ast.setTupleSchema(tupleSchema);
		schema.setItems(ast);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectNext(element)
		        .verifyComplete();
	}

	@Test
	void ArrayValidatorTestForContainsErrorSchema() {

		JsonObject job = new JsonObject();
		job.addProperty("name", "jimmy mcgill");

		JsonArray element = new JsonArray();
		element.add("texas");
		element.add("kentucky");
		element.add(1);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setContains(Schema.ofObject("object"));

		List<Schema> tupleSchema = new ArrayList<>();
		tupleSchema.add(Schema.ofString("item1"));
		tupleSchema.add(Schema.ofString("item2"));
		tupleSchema.add(Schema.ofInteger("item3"));

		ArraySchemaType ast = new ArraySchemaType();
		ast.setTupleSchema(tupleSchema);

		schema.setItems(ast);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectErrorMessage("None of the items are of type contains schema")
		        .verify();
	}

	@Test
	void ArrayValidatorTestForMinContainsSchema() {

		JsonObject job1 = new JsonObject();
		job1.addProperty("name", "jimmy mcgill");
		JsonObject job2 = new JsonObject();
		job2.addProperty("firm", "HMM");

		JsonArray element = new JsonArray();
		element.add("texas");
		element.add("kentucky");
		element.add(job1);
		element.add(true);
		element.add(job2);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setContains(Schema.ofObject("object"));
		schema.setMinContains(2);

		List<Schema> tupleSchema = new ArrayList<>();
		tupleSchema.add(Schema.ofString("item1"));
		tupleSchema.add(Schema.ofString("item2"));
		tupleSchema.add(Schema.ofObject("item3"));
		tupleSchema.add(Schema.ofBoolean("item4"));
		tupleSchema.add(Schema.ofObject("item5"));

		ArraySchemaType ast = new ArraySchemaType();
		ast.setTupleSchema(tupleSchema);
		schema.setItems(ast);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectNext(element)
		        .verifyComplete();
	}

	@Test
	void ArrayValidatorTestForMaxContainsSchema() {

		JsonObject job1 = new JsonObject();
		job1.addProperty("name", "jimmy mcgill");
		JsonObject job2 = new JsonObject();
		job2.addProperty("firm", "HMM");

		JsonArray element = new JsonArray();
		element.add("texas");
		element.add("kentucky");
		element.add(job1);
		element.add(true);
		element.add(job2);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setContains(Schema.ofObject("object"));
		schema.setMaxContains(2);

		List<Schema> tupleSchema = new ArrayList<>();
		tupleSchema.add(Schema.ofString("item1"));
		tupleSchema.add(Schema.ofString("item2"));
		tupleSchema.add(Schema.ofObject("item3"));
		tupleSchema.add(Schema.ofBoolean("item4"));
		tupleSchema.add(Schema.ofObject("item5"));

		ArraySchemaType ast = new ArraySchemaType();
		ast.setTupleSchema(tupleSchema);
		schema.setItems(ast);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectNext(element)
		        .verifyComplete();
	}

	@Test
	void ArrayValidatorTestForMinMaxContainsSchema() {

		JsonObject job1 = new JsonObject();
		job1.addProperty("name", "jimmy mcgill");
		JsonObject job2 = new JsonObject();
		job2.addProperty("firm", "HMM");
		JsonObject job3 = new JsonObject();
		job3.addProperty("city", "Alberqueue");

		JsonArray element = new JsonArray();
		element.add("texas");
		element.add("kentucky");
		element.add(job1);
		element.add(true);
		element.add(job2);
		element.add("Mcgill");
		element.add(job3);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setContains(Schema.ofObject("object"));
		schema.setMinContains(1);
		schema.setMaxContains(3);

		List<Schema> tupleSchema = new ArrayList<>();
		tupleSchema.add(Schema.ofString("item1"));
		tupleSchema.add(Schema.ofString("item2"));
		tupleSchema.add(Schema.ofObject("item3"));
		tupleSchema.add(Schema.ofBoolean("item4"));
		tupleSchema.add(Schema.ofObject("item5"));
		tupleSchema.add(Schema.ofString("item6"));
		tupleSchema.add(Schema.ofObject("item7"));

		ArraySchemaType ast = new ArraySchemaType();
		ast.setTupleSchema(tupleSchema);
		schema.setItems(ast);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectNext(element)
		        .verifyComplete();
	}

	@Test
	void ArrayValidatorTestForMinErrorContainsSchema() {

		JsonObject job1 = new JsonObject();
		job1.addProperty("name", "jimmy mcgill");
		JsonObject job2 = new JsonObject();
		job2.addProperty("firm", "HMM");
		JsonObject job3 = new JsonObject();
		job3.addProperty("city", "Alberqueue");

		JsonArray element = new JsonArray();
		element.add("texas");
		element.add("kentucky");
		element.add(job1);
		element.add(true);
		element.add(job2);
		element.add("Mcgill");
		element.add(job3);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setContains(Schema.ofObject("object"));
		schema.setMinContains(4);

		List<Schema> tupleSchema = new ArrayList<>();
		tupleSchema.add(Schema.ofString("item1"));
		tupleSchema.add(Schema.ofString("item2"));
		tupleSchema.add(Schema.ofObject("item3"));
		tupleSchema.add(Schema.ofBoolean("item4"));
		tupleSchema.add(Schema.ofObject("item5"));
		tupleSchema.add(Schema.ofString("item6"));
		tupleSchema.add(Schema.ofObject("item7"));

		ArraySchemaType ast = new ArraySchemaType();
		ast.setTupleSchema(tupleSchema);
		schema.setItems(ast);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectErrorMessage("The minimum number of the items of type contains schema should be "
		                + schema.getMinContains() + " but found 3")
		        .verify();
	}

	@Test
	void ArrayValidatorTestForMaxErrorContainsSchema() {

		JsonObject job1 = new JsonObject();
		job1.addProperty("name", "jimmy mcgill");
		JsonObject job2 = new JsonObject();
		job2.addProperty("firm", "HMM");
		JsonObject job3 = new JsonObject();
		job3.addProperty("city", "Alberqueue");

		JsonArray element = new JsonArray();
		element.add("texas");
		element.add("kentucky");
		element.add(job1);
		element.add(true);
		element.add(job2);
		element.add("Mcgill");
		element.add(job3);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setContains(Schema.ofObject("object"));
		schema.setMaxContains(1);

		List<Schema> tupleSchema = new ArrayList<>();
		tupleSchema.add(Schema.ofString("item1"));
		tupleSchema.add(Schema.ofString("item2"));
		tupleSchema.add(Schema.ofObject("item3"));
		tupleSchema.add(Schema.ofBoolean("item4"));
		tupleSchema.add(Schema.ofObject("item5"));
		tupleSchema.add(Schema.ofString("item6"));
		tupleSchema.add(Schema.ofObject("item7"));

		ArraySchemaType ast = new ArraySchemaType();
		ast.setTupleSchema(tupleSchema);
		schema.setItems(ast);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectErrorMessage("The maximum number of the items of type contains schema should be "
		                + schema.getMaxContains() + " but found 3")
		        .verify();
	}

	@Test
	void ArrayValidatorTestForMinMaxFErrorContainsSchema() {

		JsonObject job1 = new JsonObject();
		job1.addProperty("name", "jimmy mcgill");
		JsonObject job2 = new JsonObject();
		job2.addProperty("firm", "HMM");
		JsonObject job3 = new JsonObject();
		job3.addProperty("city", "Alberqueue");

		JsonArray element = new JsonArray();
		element.add("texas");
		element.add("kentucky");
		element.add(job1);
		element.add(true);
		element.add(job2);
		element.add("Mcgill");
		element.add(job3);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setContains(Schema.ofObject("object"));
		schema.setMaxContains(3);
		schema.setMinContains(4);

		List<Schema> tupleSchema = new ArrayList<>();
		tupleSchema.add(Schema.ofString("item1"));
		tupleSchema.add(Schema.ofString("item2"));
		tupleSchema.add(Schema.ofObject("item3"));
		tupleSchema.add(Schema.ofBoolean("item4"));
		tupleSchema.add(Schema.ofObject("item5"));
		tupleSchema.add(Schema.ofString("item6"));
		tupleSchema.add(Schema.ofObject("item7"));

		ArraySchemaType ast = new ArraySchemaType();
		ast.setTupleSchema(tupleSchema);
		schema.setItems(ast);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectErrorMessage("The minimum number of the items of type contains schema should be "
		                + schema.getMinContains() + " but found 3")
		        .verify();
	}

	@Test
	void ArrayValidatorTestForMinMaxSErrorContainsSchema() {

		JsonObject job1 = new JsonObject();
		job1.addProperty("name", "jimmy mcgill");
		JsonObject job2 = new JsonObject();
		job2.addProperty("firm", "HMM");
		JsonObject job3 = new JsonObject();
		job3.addProperty("city", "Alberqueue");

		JsonArray element = new JsonArray();
		element.add("texas");
		element.add("kentucky");
		element.add(true);
		element.add("Mcgill");

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setContains(Schema.ofObject("object"));
		schema.setMaxContains(2);
		schema.setMinContains(3);

		List<Schema> tupleSchema = new ArrayList<>();
		tupleSchema.add(Schema.ofString("item1"));
		tupleSchema.add(Schema.ofString("item2"));
		tupleSchema.add(Schema.ofBoolean("item4"));
		tupleSchema.add(Schema.ofString("item6"));

		ArraySchemaType ast = new ArraySchemaType();
		ast.setTupleSchema(tupleSchema);
		schema.setItems(ast);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectErrorMessage("None of the items are of type contains schema")
		        .verify();
	}

	@Test
	void ArrayValidatorTestForMinMaxWithoutContainsSchema() {

		JsonObject job1 = new JsonObject();
		job1.addProperty("name", "jimmy mcgill");
		JsonObject job2 = new JsonObject();
		job2.addProperty("firm", "HMM");
		JsonObject job3 = new JsonObject();
		job3.addProperty("city", "Alberqueue");

		JsonArray element = new JsonArray();
		element.add("texas");
		element.add("kentucky");
		element.add(job1);
		element.add(true);
		element.add(job2);
		element.add("Mcgill");
		element.add(job3);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setMaxContains(2);
		schema.setMinContains(3);

		List<Schema> tupleSchema = new ArrayList<>();
		tupleSchema.add(Schema.ofString("item1"));
		tupleSchema.add(Schema.ofString("item2"));
		tupleSchema.add(Schema.ofObject("item3"));
		tupleSchema.add(Schema.ofBoolean("item4"));
		tupleSchema.add(Schema.ofObject("item5"));
		tupleSchema.add(Schema.ofString("item6"));
		tupleSchema.add(Schema.ofObject("item7"));

		ArraySchemaType ast = new ArraySchemaType();
		ast.setTupleSchema(tupleSchema);
		schema.setItems(ast);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectNext(element)
		        .verifyComplete();
	}

	@Test
	void ArrayValidatorTestForMinMaxEContainsSchema() {

		JsonObject job1 = new JsonObject();
		job1.addProperty("name", "jimmy mcgill");
		JsonObject job2 = new JsonObject();
		job2.addProperty("firm", "HMM");
		JsonObject job3 = new JsonObject();
		job3.addProperty("city", "Alberqueue");

		JsonArray element = new JsonArray();
		element.add("texas");
		element.add("kentucky");
		element.add(job1);
		element.add(true);
		element.add(job2);
		element.add("Mcgill");
		element.add(job3);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setContains(Schema.ofObject("object"));
		schema.setMaxContains(1);
		schema.setMinContains(0);

		List<Schema> tupleSchema = new ArrayList<>();
		tupleSchema.add(Schema.ofString("item1"));
		tupleSchema.add(Schema.ofString("item2"));
		tupleSchema.add(Schema.ofObject("item3"));
		tupleSchema.add(Schema.ofBoolean("item4"));
		tupleSchema.add(Schema.ofObject("item5"));
		tupleSchema.add(Schema.ofString("item6"));
		tupleSchema.add(Schema.ofObject("item7"));

		ArraySchemaType ast = new ArraySchemaType();
		ast.setTupleSchema(tupleSchema);
		schema.setItems(ast);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectErrorMessage("The maximum number of the items of type contains schema should be "
		                + schema.getMaxContains() + " but found 3")
		        .verify();
	}

	@Test
	void ArrayValidatorTestForMinMaxErrorWithoutContainsSchema() {

		JsonObject job1 = new JsonObject();
		job1.addProperty("name", "jimmy mcgill");
		JsonObject job2 = new JsonObject();
		job2.addProperty("firm", "HMM");
		JsonObject job3 = new JsonObject();
		job3.addProperty("city", "Alberqueue");

		JsonArray element = new JsonArray();
		element.add("texas");
		element.add("kentucky");
		element.add(job1);
		element.add(true);
		element.add(job2);
		element.add("Mcgill");
		element.add(job3);

		Schema schema = new Schema();
		schema.setType(Type.of(SchemaType.ARRAY));
		schema.setMaxContains(0);
		schema.setMinContains(3);

		List<Schema> tupleSchema = new ArrayList<>();
		tupleSchema.add(Schema.ofString("item1"));
		tupleSchema.add(Schema.ofString("item2"));
		tupleSchema.add(Schema.ofObject("item3"));
		tupleSchema.add(Schema.ofBoolean("item4"));
		tupleSchema.add(Schema.ofObject("item5"));
		tupleSchema.add(Schema.ofString("item6"));
		tupleSchema.add(Schema.ofObject("item7"));

		ArraySchemaType ast = new ArraySchemaType();
		ast.setTupleSchema(tupleSchema);
		schema.setItems(ast);

		StepVerifier.create(ReactiveArrayValidator.validate(null, schema, null, element))
		        .expectNext(element)
		        .verifyComplete();
	}

}

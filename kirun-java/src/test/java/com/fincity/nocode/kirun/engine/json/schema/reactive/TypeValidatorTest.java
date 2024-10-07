package com.fincity.nocode.kirun.engine.json.schema.reactive;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.convertor.BooleanConvertor;
import com.fincity.nocode.kirun.engine.json.schema.convertor.NullConvertor;
import com.fincity.nocode.kirun.engine.json.schema.convertor.NumberConvertor;
import com.fincity.nocode.kirun.engine.json.schema.convertor.StringConvertor;
import com.fincity.nocode.kirun.engine.json.schema.convertor.enums.ConversionMode;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.validator.BooleanValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.NumberValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.StringValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveArrayValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveTypeValidator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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

		// For Exception
		type = null;

		StepVerifier.create(ReactiveTypeValidator.validate(null, type, schema, null, element))
		        .verifyErrorMessage("null is not a valid type.");
	}

	@Test
	void testTypeValidatorWithConversion() {
		Schema schema = new Schema();

		// Test for String conversion
		JsonObject stringElement = new JsonObject();
		stringElement.addProperty("value", "string");
		SchemaType stringType = SchemaType.STRING;

		StepVerifier.create(ReactiveTypeValidator.validate(null, stringType, schema, null, stringElement.get("value"), true, ConversionMode.STRICT))
				.expectNext(StringConvertor.convert(null, schema, ConversionMode.STRICT, stringElement.get("value")))
				.verifyComplete();

		// Test for Number conversion
		JsonObject numberElement = new JsonObject();
		numberElement.addProperty("12345", "12345");
		numberElement.addProperty("12345.12", "12345.12");
		numberElement.addProperty("1212", 1212);
		numberElement.addProperty("1212.12", 1212.12);
		SchemaType integer = SchemaType.INTEGER;
		SchemaType floating = SchemaType.FLOAT;

		StepVerifier.create(ReactiveTypeValidator.validate(null, integer, schema, null, numberElement.get("12345"), true, ConversionMode.STRICT))
				.expectNext(NumberConvertor.convert(null, integer, schema, ConversionMode.STRICT, numberElement.get("12345")))
				.verifyComplete();
		StepVerifier.create(ReactiveTypeValidator.validate(null, integer, schema, null, numberElement.get("1212"), true, ConversionMode.STRICT))
				.expectNext(NumberConvertor.convert(null, integer, schema, ConversionMode.STRICT, numberElement.get("1212")))
				.verifyComplete();
		StepVerifier.create(ReactiveTypeValidator.validate(null, floating, schema, null, numberElement.get("12345.12"), true, ConversionMode.STRICT))
				.expectNext(NumberConvertor.convert(null, floating, schema, ConversionMode.STRICT, numberElement.get("12345.12")))
				.verifyComplete();
		StepVerifier.create(ReactiveTypeValidator.validate(null, floating, schema, null, numberElement.get("1212.12"), true, ConversionMode.STRICT))
				.expectNext(NumberConvertor.convert(null, floating, schema, ConversionMode.STRICT, numberElement.get("1212.12")))
				.verifyComplete();

		// Test for Boolean conversion
		JsonObject booleanElement = new JsonObject();
		booleanElement.addProperty("0", "0");
		booleanElement.addProperty("true", "true");
		booleanElement.addProperty("y", "y");
		booleanElement.addProperty("yEs", "yEs");
		SchemaType booleanType = SchemaType.BOOLEAN;

		StepVerifier.create(ReactiveTypeValidator.validate(null, booleanType, schema, null, booleanElement.get("0"), true, ConversionMode.STRICT))
				.expectNext(BooleanConvertor.convert(null, schema, ConversionMode.STRICT, booleanElement.get("0")))
				.verifyComplete();

		StepVerifier.create(ReactiveTypeValidator.validate(null, booleanType, schema, null, booleanElement.get("true"), true, ConversionMode.STRICT))
				.expectNext(BooleanConvertor.convert(null, schema, ConversionMode.STRICT, booleanElement.get("true")))
				.verifyComplete();

		StepVerifier.create(ReactiveTypeValidator.validate(null, booleanType, schema, null, booleanElement.get("y"), true, ConversionMode.STRICT))
				.expectNext(BooleanConvertor.convert(null, schema, ConversionMode.STRICT, booleanElement.get("y")))
				.verifyComplete();

		StepVerifier.create(ReactiveTypeValidator.validate(null, booleanType, schema, null, booleanElement.get("yEs"), true, ConversionMode.STRICT))
				.expectNext(BooleanConvertor.convert(null, schema, ConversionMode.STRICT, booleanElement.get("yEs")))
				.verifyComplete();

		// Test for Null conversion
		JsonElement nullElement = JsonNull.INSTANCE;
		SchemaType nullType = SchemaType.NULL;

		StepVerifier.create(ReactiveTypeValidator.validate(null, nullType, schema, null, nullElement, true, ConversionMode.STRICT))
				.expectNext(NullConvertor.convert(null, schema, ConversionMode.STRICT, nullElement))
				.verifyComplete();
	}

	@Test
	void testBooleanConvertorArray() {
		Schema schema = Schema.ofArray("boolean", Schema.ofBoolean("boolean"));

		// Create an array of boolean representations
		JsonArray booleanArray = new JsonArray();
		booleanArray.add(new JsonPrimitive(true));
		booleanArray.add(new JsonPrimitive(false));
		booleanArray.add(new JsonPrimitive("yes"));
		booleanArray.add(new JsonPrimitive("no"));
		booleanArray.add(new JsonPrimitive("y"));
		booleanArray.add(new JsonPrimitive("n"));
		booleanArray.add(new JsonPrimitive(1));
		booleanArray.add(new JsonPrimitive(0));

		// Create an expected array of converted boolean values
		JsonArray expectedArray = new JsonArray();
		expectedArray.add(new JsonPrimitive(true));  // for true
		expectedArray.add(new JsonPrimitive(false)); // for false
		expectedArray.add(new JsonPrimitive(true));  // for "yes"
		expectedArray.add(new JsonPrimitive(false)); // for "no"
		expectedArray.add(new JsonPrimitive(true)); // for "y"
		expectedArray.add(new JsonPrimitive(false)); // for "n"
		expectedArray.add(new JsonPrimitive(true));  // for 1
		expectedArray.add(new JsonPrimitive(false)); // for 0

		StepVerifier
				.create(ReactiveTypeValidator.validate(null, SchemaType.ARRAY, schema, null, booleanArray, true, ConversionMode.STRICT))
				.expectNext(expectedArray)
				.verifyComplete();
	}

	@Test
	void testNumberConvertorArray() {
		Schema schema = Schema.ofArray("number", Schema.ofNumber("number"));

		// Create an array of boolean representations
		JsonArray numberArray = new JsonArray();
		numberArray.add(new JsonPrimitive("11"));
		numberArray.add(new JsonPrimitive("11.12"));
		numberArray.add(new JsonPrimitive("11192371231"));
		numberArray.add(new JsonPrimitive("1123.123"));
		numberArray.add(new JsonPrimitive("0"));

		// Create an expected array of converted boolean values
		JsonArray expectedArray = new JsonArray();
		expectedArray.add(new JsonPrimitive(11));
		expectedArray.add(new JsonPrimitive(11.12));
		expectedArray.add(new JsonPrimitive(11192371231L));
		expectedArray.add(new JsonPrimitive(1123.123));
		expectedArray.add(new JsonPrimitive(0));

		StepVerifier
				.create(ReactiveTypeValidator.validate(null, SchemaType.ARRAY, schema, null, numberArray, true, ConversionMode.LENIENT))
				.expectNext(expectedArray)
				.verifyComplete();
	}


	@Test
	void testObjectConvertor() {

		JsonArray numberArray = new JsonArray();
		numberArray.add(new JsonPrimitive("11"));
		numberArray.add(new JsonPrimitive("11.12"));
		numberArray.add(new JsonPrimitive("11192371231"));
		numberArray.add(new JsonPrimitive("1123.123"));
		numberArray.add(new JsonPrimitive("0"));

		JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty("int", "1997");
		jsonObject.addProperty("long", "123123123");
		jsonObject.addProperty("float", "123.12");
		jsonObject.addProperty("double", "123.1232");
		jsonObject.addProperty("booleanTrue", "true");
		jsonObject.addProperty("booleanFalse", "false");
		jsonObject.addProperty("string", 12314);
		jsonObject.add("numberArray", numberArray);


		Map<String, Schema> props = new HashMap<>();

		props.put("int", Schema.ofNumber("int"));
		props.put("long", Schema.ofLong("long"));
		props.put("float", Schema.ofFloat("float"));
		props.put("double", Schema.ofNumber("double"));
		props.put("booleanTrue", Schema.ofBoolean("booleanTrue"));
		props.put("booleanFalse", Schema.ofBoolean("booleanFalse"));
		props.put("string", Schema.ofString("string"));
		props.put("number", Schema.ofArray("number", Schema.ofNumber("number")));

		Schema schema = Schema.ofObject("jsonObject")
				.setProperties(props);

		JsonArray expectedArray = new JsonArray();
		expectedArray.add(new JsonPrimitive(11));
		expectedArray.add(new JsonPrimitive(11.12));
		expectedArray.add(new JsonPrimitive(11192371231L));
		expectedArray.add(new JsonPrimitive(1123.123));
		expectedArray.add(new JsonPrimitive(0));

		JsonObject expectedObject = new JsonObject();
		expectedObject.addProperty("int", 1997);
		expectedObject.addProperty("long", 123123123L);
		expectedObject.addProperty("float", 123.12F);
		expectedObject.addProperty("double", 123.1232D);
		expectedObject.addProperty("booleanTrue", true);
		expectedObject.addProperty("booleanFalse", false);
		expectedObject.addProperty("string", "12314");
		expectedObject.add("numberArray", numberArray);

		StepVerifier
				.create(ReactiveTypeValidator.validate(null, SchemaType.OBJECT, schema, null, jsonObject, true, ConversionMode.STRICT))
				.expectNext(expectedObject)
				.verifyComplete();
	}

	@Test
	void testObjectOfObjectConvertor() {

		JsonObject innerObject = new JsonObject();
		innerObject.addProperty("innerInt", "42");
		innerObject.addProperty("innerString", "innerValue");

		JsonObject outerObject = new JsonObject();
		outerObject.add("innerObject", innerObject);
		outerObject.addProperty("outerInt", "100");

		Map<String, Schema> innerProps = new HashMap<>();
		innerProps.put("innerInt", Schema.ofNumber("innerInt"));
		innerProps.put("innerString", Schema.ofString("innerString"));

		Schema innerSchema = Schema.ofObject("innerObject")
				.setProperties(innerProps);

		Map<String, Schema> outerProps = new HashMap<>();
		outerProps.put("innerObject", innerSchema);
		outerProps.put("outerInt", Schema.ofNumber("outerInt"));

		Schema outerSchema = Schema.ofObject("outerObject")
				.setProperties(outerProps);

		JsonObject expectedOuterObject = new JsonObject();
		expectedOuterObject.add("innerObject", new JsonObject());
		expectedOuterObject.getAsJsonObject("innerObject").addProperty("innerInt", 42);
		expectedOuterObject.getAsJsonObject("innerObject").addProperty("innerString", "innerValue");
		expectedOuterObject.addProperty("outerInt", 100);

		StepVerifier
				.create(ReactiveTypeValidator.validate(null, SchemaType.OBJECT, outerSchema, null, outerObject, true, ConversionMode.STRICT))
				.expectNext(expectedOuterObject)
				.verifyComplete();
	}

}

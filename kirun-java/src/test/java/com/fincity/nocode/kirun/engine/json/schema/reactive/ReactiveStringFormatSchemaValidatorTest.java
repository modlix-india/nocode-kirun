package com.fincity.nocode.kirun.engine.json.schema.reactive;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.string.StringFormat;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.google.gson.JsonObject;

import reactor.test.StepVerifier;

class ReactiveStringFormatSchemaValidatorTest {

	@Test
	void schemaVerifyForStringFormatInObject() {

		JsonObject def = new JsonObject();
		def.addProperty("value", "1997-11-21");
		Schema dateSchema = new Schema().setFormat(StringFormat.DATE)
		        .setName("dateSchema");

		Schema schema = Schema.ofObject("dateTest")
		        .setProperties(Map.of("value", dateSchema, "intSc", Schema.ofInteger("intSchema")))
		        .setRequired(List.of("value"));

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, def))
		        .verifyErrorMessage( "dateTest - dateTest.dateSchema - Type is missing in schema for declared DATE format.");
	}

	@Test
	void schemaVerifyForIntegerInObject() {

		JsonObject def = new JsonObject();
		def.addProperty("value", "1997");

		Schema schema = Schema.ofObject("intTest")
		        .setProperties(Map.of("value", Schema.ofInteger("intSchema")));

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, def))
		        .verifyErrorMessage("intTest - intTest.intSchema - intTest.intSchema - \"1997\" is not a Integer");
	}

	@Test
	void schemaVerifyForInteger() {

		JsonObject def = new JsonObject();
		def.addProperty("value", 1997);

		Schema schema = Schema.ofInteger("intTest")
		        .setMinimum(2000);

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, def))
		        .verifyErrorMessage("intTest - intTest - {\"value\":1997} is not a Integer");
	}

	@Test
	void schemaVerifyForStringFormatIntInObject() {

		JsonObject def = new JsonObject();
		def.addProperty("value", "1997-11-21");
		def.addProperty("intSc", 4);
		Schema dateSchema = new Schema().setFormat(StringFormat.DATE)
		        .setName("dateSchema")
		        .setType(Type.of(SchemaType.STRING));

		Schema schema = Schema.ofObject("dateTest")
		        .setProperties(Map.of("value", dateSchema, "intSc", Schema.ofInteger("intSchema")
		                .setMaximum(12)
		                .setMultipleOf(2l)))
		        .setRequired(List.of("value"));

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, def))
		        .expectNext(def)
		        .verifyComplete();
	}

	@Test
	void schemaVerifyForStringFormatEmailStringMissingInTypeObject() {

		JsonObject def = new JsonObject();
		def.addProperty("email", "iosdjfdf123--@gmail.com");
		Schema emailSchema = new Schema().setFormat(StringFormat.EMAIL)
		        .setName("emailSchema");

		Schema schema = Schema.ofObject("emailTest")
		        .setProperties(Map.of("email", emailSchema))
		        .setRequired(List.of("email"));

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, def))
		        .verifyErrorMessage(
		                "emailTest - emailTest.emailSchema - Type is missing in schema for declared EMAIL format.");
	}

	@Test
	void schemaVerifyForStringFormatEmailWrongRegexInObject() {

		JsonObject def = new JsonObject();
		def.addProperty("email", "iosdjfdf123--@@gmail.com");
		Schema emailSchema = new Schema().setFormat(StringFormat.EMAIL)
		        .setName("emailSchema")
		        .setType(Type.of(SchemaType.STRING));

		Schema schema = Schema.ofObject("emailTest")
		        .setProperties(Map.of("email", emailSchema, "intSc", Schema.ofInteger("intSchema")
		                .setMaximum(12)
		                .setMultipleOf(2l)))
		        .setRequired(List.of("email"));

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, def))
		        .verifyErrorMessage("emailTest - emailTest.emailSchema - emailTest.emailSchema - \"iosdjfdf123--@@gmail.com\" is not matched with the email pattern");
	}

	@Test
	void schemaVerifyForStringFormatEmailType() {

		JsonObject def = new JsonObject();
		def.addProperty("email", "iosdjfdf123--@gmail.com");
		Schema emailSchema = new Schema().setFormat(StringFormat.EMAIL)
		        .setName("emailSchema")
		        .setType(Type.of(SchemaType.STRING));

		Schema schema = Schema.ofObject("emailTest")
		        .setProperties(Map.of("email", emailSchema, "intSc", Schema.ofInteger("intSchema")
		                .setMaximum(12)
		                .setMultipleOf(2l)))
		        .setRequired(List.of("email"));
		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, def))
		        .expectNext(def)
		        .verifyComplete();
	}

	@Test
	void schemaVerifyForStringFormatTimeStringMissingInTypeObject() {

		JsonObject def = new JsonObject();
		def.addProperty("time", "23:12:43");
		Schema timeSchema = new Schema().setFormat(StringFormat.TIME)
		        .setName("timeSchema");

		Schema schema = Schema.ofObject("timeTest")
		        .setProperties(Map.of("time", timeSchema))
		        .setRequired(List.of("time"));

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, def))
		        .verifyErrorMessage("timeTest - timeTest.timeSchema - Type is missing in schema for declared TIME format.");
	}

	@Test
	void schemaVerifyForStringFormatTimeWrongRegexInObject() {

		JsonObject def = new JsonObject();
		def.addProperty("time", "24:12:23");
		Schema timeSchema = new Schema().setFormat(StringFormat.TIME)
		        .setName("timeSchema")
		        .setType(Type.of(SchemaType.STRING));

		Schema schema = Schema.ofObject("timeTest")
		        .setProperties(Map.of("time", timeSchema, "intSc", Schema.ofInteger("intSchema")
		                .setMaximum(12)
		                .setMultipleOf(2l)))
		        .setRequired(List.of("time"));

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, def))
		        .verifyErrorMessage("timeTest - timeTest.timeSchema - timeTest.timeSchema - \"24:12:23\" is not matched with the time pattern");
	}

	@Test
	void schemaVerifyForStringFormatTimeType() {

		JsonObject def = new JsonObject();
		def.addProperty("time", "14:34:45");
		Schema emailSchema = new Schema().setFormat(StringFormat.TIME)
		        .setName("timeSchema")
		        .setType(Type.of(SchemaType.STRING));

		Schema schema = Schema.ofObject("timeTest")
		        .setProperties(Map.of("time", emailSchema, "intSc", Schema.ofInteger("intSchema")
		                .setMaximum(12)
		                .setMultipleOf(2l)))
		        .setRequired(List.of("time"));

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, def))
		        .expectNext(def)
		        .verifyComplete();
	}

	@Test
	void schemaVerifyForStringFormatDateTimeStringMissingInTypeObject() {

		JsonObject def = new JsonObject();
		def.addProperty("datetime", "2023-08-21T07:56:45+12:12");
		Schema datetimeSchema = new Schema().setFormat(StringFormat.DATETIME)
		        .setName("dateTimeSchema");

		Schema schema = Schema.ofObject("datetimeTest")
		        .setProperties(Map.of("datetime", datetimeSchema))
		        .setRequired(List.of("datetime"));

		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, def))
		        .verifyErrorMessage("datetimeTest - datetimeTest.dateTimeSchema - Type is missing in schema for declared DATETIME format.");
	}

	@Test
	void schemaVerifyForStringFormatDateTimeWrongRegexInObject() {

		JsonObject def = new JsonObject();
		def.addProperty("datetime", "2023-08-21T07:56:45s+12:12");
		Schema datetimeSchema = new Schema().setFormat(StringFormat.DATETIME)
		        .setName("datetimeSchema")
		        .setType(Type.of(SchemaType.STRING));

		Schema schema = Schema.ofObject("datetimeTest")
		        .setProperties(Map.of("datetime", datetimeSchema, "intSc", Schema.ofInteger("intSchema")
		                .setMaximum(12)
		                .setMultipleOf(2l)))
		        .setRequired(List.of("datetime"));
		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, def))
		        .verifyErrorMessage("datetimeTest - datetimeTest.datetimeSchema - datetimeTest.datetimeSchema - \"2023-08-21T07:56:45s+12:12\" is not matched with the date time pattern");
	}

	@Test
	void schemaVerifyForStringFormatDateTimeType() {

		JsonObject def = new JsonObject();
		def.addProperty("datetime", "2023-08-21T07:56:45+12:12");
		Schema emailSchema = new Schema().setFormat(StringFormat.DATETIME)
		        .setName("datetimeSchema")
		        .setType(Type.of(SchemaType.STRING));

		Schema schema = Schema.ofObject("datetimeTest")
		        .setProperties(Map.of("datetime", emailSchema, "intSc", Schema.ofInteger("intSchema")
		                .setMaximum(12)
		                .setMultipleOf(2l)))
		        .setRequired(List.of("datetime"));
		
		StepVerifier.create(ReactiveSchemaValidator.validate(null, schema, null, def))
		        .expectNext(def)
		        .verifyComplete();
	}
}

package com.fincity.nocode.kirun.engine.json.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.string.StringFormat;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.google.gson.JsonObject;

class StringFormatSchemaValidatorTest {

    @Test
    void schemaVerifyForStringFormatInObject() {

        JsonObject def = new JsonObject();
        def.addProperty("value", "1997-11-21");
        Schema dateSchema = new Schema().setFormat(StringFormat.DATE).setName("dateSchema");
// as String schema type was not declared it will throw error

        Schema schema = Schema.ofObject("dateTest")
                .setProperties(Map.of("value", dateSchema, "intSc", Schema.ofInteger("intSchema")))
                .setRequired(List.of("value"));

        SchemaValidationException schemaEx = assertThrows(SchemaValidationException.class,
                () -> SchemaValidator.validate(null, schema, null, def));

        assertEquals("dateTest - Value {\"value\":\"1997-11-21\"} is not of valid type(s)\n"
                + "dateTest.dateSchema - Type is missing in schema for declared " + dateSchema.getFormat().toString()
                + " format.", schemaEx.getMessage());
    }

    @Test
    void schemaVerifyForIntegerInObject() {

        JsonObject def = new JsonObject();
        def.addProperty("value", "1997");

        Schema schema = Schema.ofObject("intTest")
                .setProperties(Map.of("value", Schema.ofInteger("intSchema")));

        assertThrows(SchemaValidationException.class, () -> SchemaValidator.validate(null, schema, null, def));
    }

    @Test
    void schemaVerifyForInteger() {

        JsonObject def = new JsonObject();
        def.addProperty("value", 1997);

        Schema schema = Schema.ofInteger("intTest")
                .setMinimum(2000);

        assertThrows(SchemaValidationException.class, () -> SchemaValidator.validate(null, schema, null, def));
    }

    @Test
    void schemaVerifyForStringFormatIntInObject() {

        JsonObject def = new JsonObject();
        def.addProperty("value", "1997-11-21");
        def.addProperty("intSc", 4);
        Schema dateSchema = new Schema().setFormat(StringFormat.DATE).setName("dateSchema")
                .setType(Type.of(SchemaType.STRING));

        Schema schema = Schema.ofObject("dateTest")
                .setProperties(Map.of("value", dateSchema, "intSc",
                        Schema.ofInteger("intSchema").setMaximum(12).setMultipleOf(2l)))
                .setRequired(List.of("value"));

        assertEquals(SchemaValidator.validate(null, schema, null, def), def);
    }

    @Test
    void schemaVerifyForStringFormatEmailStringMissingInTypeObject() {

        JsonObject def = new JsonObject();
        def.addProperty("email", "iosdjfdf123--@gmail.com");
        Schema emailSchema = new Schema().setFormat(StringFormat.EMAIL).setName("emailSchema");

        Schema schema = Schema.ofObject("emailTest")
                .setProperties(Map.of("email", emailSchema))
                .setRequired(List.of("email"));

        SchemaValidationException emailExc = assertThrows(SchemaValidationException.class,
                () -> SchemaValidator.validate(null, schema, null, def));
        var msg = schema.getName() + " - " + "Value {\"email\":\"iosdjfdf123--@gmail.com\"} is not of valid type(s)\n" +
                schema.getName() + "." + emailSchema.getName() + " - "
                + "Type is missing in schema for declared EMAIL format.";
        assertEquals(msg, emailExc.getMessage());
    }

    @Test
    void schemaVerifyForStringFormatEmailWrongRegexInObject() {

        JsonObject def = new JsonObject();
        def.addProperty("email", "iosdjfdf123--@@gmail.com");
        Schema emailSchema = new Schema().setFormat(StringFormat.EMAIL).setName("emailSchema")
                .setType(Type.of(SchemaType.STRING));

        Schema schema = Schema.ofObject("emailTest")
                .setProperties(Map.of("email", emailSchema, "intSc",
                        Schema.ofInteger("intSchema").setMaximum(12).setMultipleOf(2l)))
                .setRequired(List.of("email"));

        SchemaValidationException emailExc = assertThrows(SchemaValidationException.class,
                () -> SchemaValidator.validate(null, schema, null, def));
        var msg = schema.getName() + " - " + "Value {\"email\":\"iosdjfdf123--@@gmail.com\"} is not of valid type(s)\n"
                +
                schema.getName() + "." + emailSchema.getName() + " - "
                + "Value \"iosdjfdf123--@@gmail.com\" is not of valid type(s)\n" +
                schema.getName() + "." + emailSchema.getName() + " - "
                + "\"iosdjfdf123--@@gmail.com\" is not matched with the email pattern";
        assertEquals(msg, emailExc.getMessage());
    }

    @Test
    void schemaVerifyForStringFormatEmailType() {

        JsonObject def = new JsonObject();
        def.addProperty("email", "iosdjfdf123--@gmail.com");
        Schema emailSchema = new Schema().setFormat(StringFormat.EMAIL).setName("emailSchema")
                .setType(Type.of(SchemaType.STRING));

        Schema schema = Schema.ofObject("emailTest")
                .setProperties(Map.of("email", emailSchema, "intSc",
                        Schema.ofInteger("intSchema").setMaximum(12).setMultipleOf(2l)))
                .setRequired(List.of("email"));
        assertEquals(def, SchemaValidator.validate(null, schema, null, def));
    }

    @Test
    void schemaVerifyForStringFormatTimeStringMissingInTypeObject() {

        JsonObject def = new JsonObject();
        def.addProperty("time", "23:12:43");
        Schema timeSchema = new Schema().setFormat(StringFormat.TIME).setName("timeSchema");

        Schema schema = Schema.ofObject("timeTest")
                .setProperties(Map.of("time", timeSchema))
                .setRequired(List.of("time"));

        SchemaValidationException timeExc = assertThrows(SchemaValidationException.class,
                () -> SchemaValidator.validate(null, schema, null, def));
        var msg = schema.getName() + " - " + "Value {\"time\":\"23:12:43\"} is not of valid type(s)\n" +
                schema.getName() + "." + timeSchema.getName() + " - "
                + "Type is missing in schema for declared TIME format.";
        assertEquals(msg, timeExc.getMessage());
    }

    @Test
    void schemaVerifyForStringFormatTimeWrongRegexInObject() {

        JsonObject def = new JsonObject();
        def.addProperty("time", "24:12:23");
        Schema timeSchema = new Schema().setFormat(StringFormat.TIME).setName("timeSchema")
                .setType(Type.of(SchemaType.STRING));

        Schema schema = Schema.ofObject("timeTest")
                .setProperties(Map.of("time", timeSchema, "intSc",
                        Schema.ofInteger("intSchema").setMaximum(12).setMultipleOf(2l)))
                .setRequired(List.of("time"));

        SchemaValidationException emailExc = assertThrows(SchemaValidationException.class,
                () -> SchemaValidator.validate(null, schema, null, def));
        var msg = schema.getName() + " - " + "Value {\"time\":\"24:12:23\"} is not of valid type(s)\n"
                +
                schema.getName() + "." + timeSchema.getName() + " - "
                + "Value \"24:12:23\" is not of valid type(s)\n" +
                schema.getName() + "." + timeSchema.getName() + " - "
                + "\"24:12:23\" is not matched with the time pattern";
        assertEquals(msg, emailExc.getMessage());
    }

    @Test
    void schemaVerifyForStringFormatTimeType() {

        JsonObject def = new JsonObject();
        def.addProperty("time", "14:34:45");
        Schema emailSchema = new Schema().setFormat(StringFormat.TIME).setName("timeSchema")
                .setType(Type.of(SchemaType.STRING));

        Schema schema = Schema.ofObject("timeTest")
                .setProperties(Map.of("time", emailSchema, "intSc",
                        Schema.ofInteger("intSchema").setMaximum(12).setMultipleOf(2l)))
                .setRequired(List.of("time"));
        assertEquals(def, SchemaValidator.validate(null, schema, null, def));
    }

    @Test
    void schemaVerifyForStringFormatDateTimeStringMissingInTypeObject() {

        JsonObject def = new JsonObject();
        def.addProperty("datetime", "2023-08-21T07:56:45+12:12");
        Schema datetimeSchema = new Schema().setFormat(StringFormat.DATETIME).setName("dateTimeSchema");

        Schema schema = Schema.ofObject("datetimeTest")
                .setProperties(Map.of("datetime", datetimeSchema))
                .setRequired(List.of("datetime"));

        SchemaValidationException timeExc = assertThrows(SchemaValidationException.class,
                () -> SchemaValidator.validate(null, schema, null, def));
        var msg = schema.getName() + " - "
                + "Value {\"datetime\":\"2023-08-21T07:56:45+12:12\"} is not of valid type(s)\n" +
                schema.getName() + "." + datetimeSchema.getName() + " - "
                + "Type is missing in schema for declared DATETIME format.";
        assertEquals(msg, timeExc.getMessage());
    }

    @Test
    void schemaVerifyForStringFormatDateTimeWrongRegexInObject() {

        JsonObject def = new JsonObject();
        def.addProperty("datetime", "2023-08-21T07:56:45s+12:12");
        Schema datetimeSchema = new Schema().setFormat(StringFormat.DATETIME).setName("datetimeSchema")
                .setType(Type.of(SchemaType.STRING));

        Schema schema = Schema.ofObject("datetimeTest")
                .setProperties(Map.of("datetime", datetimeSchema, "intSc",
                        Schema.ofInteger("intSchema").setMaximum(12).setMultipleOf(2l)))
                .setRequired(List.of("datetime"));

        SchemaValidationException emailExc = assertThrows(SchemaValidationException.class,
                () -> SchemaValidator.validate(null, schema, null, def));
        var msg = schema.getName() + " - "
                + "Value {\"datetime\":\"2023-08-21T07:56:45s+12:12\"} is not of valid type(s)\n"
                +
                schema.getName() + "." + datetimeSchema.getName() + " - "
                + "Value \"2023-08-21T07:56:45s+12:12\" is not of valid type(s)\n" +
                schema.getName() + "." + datetimeSchema.getName() + " - "
                + "\"2023-08-21T07:56:45s+12:12\" is not matched with the date time pattern";
        assertEquals(msg, emailExc.getMessage());
    }

    @Test
    void schemaVerifyForStringFormatDateTimeType() {

        JsonObject def = new JsonObject();
        def.addProperty("datetime", "2023-08-21T07:56:45+12:12");
        Schema emailSchema = new Schema().setFormat(StringFormat.DATETIME).setName("datetimeSchema")
                .setType(Type.of(SchemaType.STRING));

        Schema schema = Schema.ofObject("datetimeTest")
                .setProperties(Map.of("datetime", emailSchema, "intSc",
                        Schema.ofInteger("intSchema").setMaximum(12).setMultipleOf(2l)))
                .setRequired(List.of("datetime"));
        assertEquals(def, SchemaValidator.validate(null, schema, null, def));
    }
}

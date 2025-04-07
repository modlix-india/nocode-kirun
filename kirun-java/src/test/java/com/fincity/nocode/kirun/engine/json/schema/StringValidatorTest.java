package com.fincity.nocode.kirun.engine.json.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.string.StringFormat;
import com.fincity.nocode.kirun.engine.json.schema.validator.StringValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class StringValidatorTest {

    @Test
    public void StringValidatorTestForValidation() {
        Schema schema = new Schema();
        JsonElement element = null;

        //Null Check
        SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
                () -> StringValidator.validate(null, schema, element));

        assertEquals("Expected a string but found null", schemaValidationException.getMessage());
    }

    @Test
    public void StringValidatorTestForValidationIfString() {

        //Is not Json primitive
        Schema schema = new Schema();
        JsonObject elementJsonPrimitive = new JsonObject();
        elementJsonPrimitive.addProperty("value", 123);

        SchemaValidationException schemaValidationExceptionForJsonPrimitive = assertThrows(SchemaValidationException.class,
                () -> StringValidator.validate(null, schema, elementJsonPrimitive));

        assertEquals(elementJsonPrimitive.toString() + " is not String", schemaValidationExceptionForJsonPrimitive.getMessage());

    }

    @Test
    void StringValidatorTestForMinLengthIfStringException() {

        Schema schema = new Schema();
        schema.setMinLength(13);
        JsonObject stringObj = new JsonObject();
        stringObj.addProperty("value", "SURENdHar.S");

        SchemaValidationException schemaValidationExceptionOfMinString = assertThrows(SchemaValidationException.class,
                () -> StringValidator.validate(null, schema, stringObj.get("value")));
        assertEquals("Expected a minimum of " + schema.getMinLength() + " characters",
                schemaValidationExceptionOfMinString.getMessage());
    }

    @Test
    void StringValidatorTestForMinLengthIfString() {

        Schema schema = new Schema();
        schema.setMinLength(7);
        JsonObject stringObj = new JsonObject();
        stringObj.addProperty("value", "Fincity");

        assertEquals(stringObj.get("value"),
                StringValidator.validate(null, schema, stringObj.get("value")));
    }

    @Test
    void StringValidatorTestForMaxLengthIfStringException() {

        Schema schema = new Schema();
        schema.setMaxLength(10);
        JsonObject stringObj = new JsonObject();
        stringObj.addProperty("value", "SURENdHar.S");

        SchemaValidationException schemaValidationExceptionOfMaxString = assertThrows(SchemaValidationException.class,
                () -> StringValidator.validate(null, schema, stringObj.get("value")));
        assertEquals("Expected a maximum of " + schema.getMaxLength() + " characters",
                schemaValidationExceptionOfMaxString.getMessage());
    }

    @Test
    void StringValidatorTestForMaxLengthIfString() {

        Schema schema = new Schema();
        schema.setMaxLength(9);
        JsonObject stringObj = new JsonObject();
        stringObj.addProperty("value", "SURENdHar");

        assertEquals(stringObj.get("value"),
                StringValidator.validate(null, schema, stringObj.get("value")));
    }

    @Test
    public void StringValidatorTestForValidationIfTimePatternMatched() {

        //String format is Time
        Schema schema = new Schema();
        schema.setFormat(StringFormat.TIME);

        JsonObject formatElement = new JsonObject();
        formatElement.addProperty("value", "10-Dec-198 10:19:59");

        SchemaValidationException schemaValidationExceptionTimeFormat = assertThrows(SchemaValidationException.class,
                () -> StringValidator.validate(null, schema, formatElement.get("value")));

        assertEquals(formatElement.get("value").toString() + " is not matched with the " + "time pattern", schemaValidationExceptionTimeFormat.getMessage());

    }

    @Test
    public void StringValidatorTestForValidationIfDatePatternMatched() {

        //String format is Time
        Schema schema = new Schema();
        schema.setFormat(StringFormat.DATE);

        JsonObject formatElement = new JsonObject();
        formatElement.addProperty("value", "1998-20-12");

        SchemaValidationException schemaValidationExceptionTimeFormat = assertThrows(SchemaValidationException.class,
                () -> StringValidator.validate(null, schema, formatElement.get("value")));

        assertEquals(formatElement.get("value").toString() + " is not matched with the " + "date pattern", schemaValidationExceptionTimeFormat.getMessage());

    }

    @Test
    public void StringValidatorTestForValidationIfDateTimePatternMatched() {

        //String format is Time
        Schema schema = new Schema();
        schema.setFormat(StringFormat.DATETIME);

        JsonObject formatElement = new JsonObject();
        formatElement.addProperty("value", "2018-12-25 23:50:55.999");

        SchemaValidationException schemaValidationExceptionTimeFormat = assertThrows(SchemaValidationException.class,
                () -> StringValidator.validate(null, schema, formatElement.get("value")));

        assertEquals(formatElement.get("value").toString() + " is not matched with the " + "date time pattern", schemaValidationExceptionTimeFormat.getMessage());

    }

    @Test
    void StringValidatorTestForValidationIfEmailPatternNotMatched() {

        Schema schema = new Schema();
        schema.setFormat(StringFormat.EMAIL);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", "testemail fai%6&8ls@gmail.com");

        SchemaValidationException schemaValidationExceptionEx = assertThrows(SchemaValidationException.class,
                () -> StringValidator.validate(null, schema, jsonObject.get("value")));

        assertEquals(jsonObject.get("value").toString() + " is not matched with the " + "email pattern",
                schemaValidationExceptionEx.getMessage());
    }

    @Test
    void StringValidatorTestForValidationIfEmailPatternMatched() {

        Schema schema = new Schema();
        schema.setFormat(StringFormat.EMAIL);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", "testemai_fai%6&8lworkings@gmagil.com");

        assertEquals(jsonObject.get("value").toString(),
                StringValidator.validate(null, schema, jsonObject.get("value")).toString());
    }

    @Test
    void testStringValidatorMaxLengthCustomMessage() {
        Schema schema = new Schema();
        schema.setMaxLength(5);

        Map<String, Object> uiHelper = new HashMap<>();
        uiHelper.put("maxLength", "The input {value} is too long! Max allowed is 5.");
        schema.setUiHelper(uiHelper);

        JsonElement element = new JsonPrimitive("TooLongText");

        SchemaValidationException ex = assertThrows(SchemaValidationException.class,
                () -> StringValidator.validate(new ArrayList<>(), schema, element));

        assertEquals("The input TooLongText is too long! Max allowed is 5.", ex.getMessage());
    }

    @Test
    void testStringValidatorMinLengthCustomMessage() {
        Schema schema = new Schema();
        schema.setMinLength(5);

        Map<String, Object> uiHelper = new HashMap<>();
        uiHelper.put("minLength", "The input {value} is too short! Min required is 5.");
        schema.setUiHelper(uiHelper);

        JsonElement element = new JsonPrimitive("123");

        SchemaValidationException ex = assertThrows(SchemaValidationException.class,
                () -> StringValidator.validate(new ArrayList<>(), schema, element));

        assertEquals("The input 123 is too short! Min required is 5.", ex.getMessage());
    }

    @Test
    void testStringValidatorPatternCustomMessage() {
        Schema schema = new Schema();
        schema.setPattern("^[A-Z]+$");

        Map<String, Object> uiHelper = new HashMap<>();
        uiHelper.put("pattern", "The value {value} does not match the required pattern!");
        schema.setUiHelper(uiHelper);

        JsonPrimitive invalidInput = new JsonPrimitive("invalid123");

        SchemaValidationException exception = assertThrows(SchemaValidationException.class,
                () -> StringValidator.validate(List.of(schema), schema, invalidInput));

        assertEquals("The value invalid123 does not match the required pattern!", exception.getMessage());
    }
}

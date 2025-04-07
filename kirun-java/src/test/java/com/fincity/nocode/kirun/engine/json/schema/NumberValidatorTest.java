package com.fincity.nocode.kirun.engine.json.schema;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.validator.NumberValidator;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.google.gson.JsonObject;

class NumberValidatorTest {

    @Test
    void NumberValidatorValidateTestForNull() {

        JsonObject element = null;
        Schema schema = new Schema();
        SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
                () -> NumberValidator.validate(null, null, schema, element));

        assertEquals("Expected a number but found null", schemaValidationException.getMessage());
    }

    @Test
    void NumberValidatorValidateTestForIsNumber() {

        JsonObject element = new JsonObject();
        element.addProperty("value", "123");

        SchemaType type = SchemaType.INTEGER;

        Schema schema = new Schema();

        SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
                () -> NumberValidator.validate(type, null, schema, element));

        assertEquals(element.toString() + " is not a " + type.getPrintableName(),
                schemaValidationException.getMessage());
    }

    @Test
    void NumberValidatorValidateTestForRangeCheckMinimum() {

        JsonObject element = new JsonObject();
        element.addProperty("value", 123);

        SchemaType type = SchemaType.INTEGER;

        Schema schema = new Schema();
        schema.setType(Type.of(SchemaType.INTEGER));
        schema.setMinimum(124);

        SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
                () -> NumberValidator.validate(type, null, schema, element.get("value")));

        assertEquals(element.get("value").toString() + " should be greater than or equal to " + schema.getMinimum(),
                schemaValidationException.getMessage());
    }

    @Test
    void NumberValidatorValidateTestForRangeCheckMaximum() {

        JsonObject element = new JsonObject();
        element.addProperty("value", 129);

        SchemaType type = SchemaType.INTEGER;

        Schema schema = new Schema();
        schema.setType(Type.of(SchemaType.INTEGER));
        schema.setMaximum(124);

        SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
                () -> NumberValidator.validate(type, null, schema, element.get("value")));

        assertEquals(element.get("value").toString() + " should be less than or equal to " + schema.getMaximum(),
                schemaValidationException.getMessage());
    }

    @Test
    void NumberValidatorValidateTestForRangeCheckExclusiveMaximum() {

        JsonObject element = new JsonObject();
        element.addProperty("value", 10.7);

        SchemaType type = SchemaType.FLOAT;

        Schema schema = new Schema();
        schema.setType(Type.of(SchemaType.FLOAT));
        schema.setExclusiveMaximum(9);

        SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
                () -> NumberValidator.validate(type, null, schema, element.get("value")));

        assertEquals(element.get("value") + " should be less than " + schema.getExclusiveMaximum(),
                schemaValidationException.getMessage());
    }

    @Test
    void NumberValidatorValidateTestForRangeCheckExclusiveMinimum() {

        JsonObject element = new JsonObject();
        element.addProperty("value", 4);

        SchemaType type = SchemaType.FLOAT;

        Schema schema = new Schema();
        schema.setType(Type.of(SchemaType.FLOAT));
        schema.setExclusiveMinimum(9);

        SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
                () -> NumberValidator.validate(type, null, schema, element.get("value")));

        assertEquals(element.get("value") + " should be greater than " + schema.getExclusiveMinimum(),
                schemaValidationException.getMessage());
    }

    @Test
    void NumberValidatorValidateTestForMultipleCheck() {

        JsonObject element = new JsonObject();
        element.addProperty("value", 154774374);

        SchemaType type = SchemaType.LONG;

        Schema schema = new Schema();
        Long value = (long) 7738718;
        schema.setType(Type.of(SchemaType.LONG));
        schema.setMultipleOf(value);

        SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
                () -> NumberValidator.validate(type, null, schema, element.get("value")));

        assertEquals(element.get("value").toString() + " is not multiple of " + schema.getMultipleOf(),
                schemaValidationException.getMessage());
    }

    @Test
    void NumberValidatorValidateTestForMultipleCheckForDoubleValue() {

        JsonObject element = new JsonObject();
        element.addProperty("value", 10.7);

        SchemaType type = SchemaType.FLOAT;

        Schema schema = new Schema();
        float number = 444.33f;
        long value = (long) number;

        schema.setType(Type.of(SchemaType.FLOAT));
        schema.setMultipleOf(value);

        SchemaValidationException schemaValidationException = assertThrows(SchemaValidationException.class,
                () -> NumberValidator.validate(type, null, schema, element.get("value")));

        assertEquals(element.get("value").toString() + " is not multiple of " + schema.getMultipleOf(),
                schemaValidationException.getMessage());
    }

    @Test
    void NumberValidatorValidateTestForCustomMultipleOfMessage() {
        JsonObject element = new JsonObject();
        element.addProperty("value", 10);

        Schema schema = new Schema();
        schema.setType(Type.of(SchemaType.INTEGER));
        schema.setMultipleOf(3L);

        Map<String, Object> uiHelper = new HashMap<>();
        uiHelper.put("multipleOf", "Custom error: not a multiple! {value}");

        schema.setUiHelper(uiHelper);

        SchemaValidationException ex = assertThrows(SchemaValidationException.class,
                () -> NumberValidator.validate(SchemaType.INTEGER, null, schema, element.get("value")));

        assertEquals("Custom error: not a multiple! 10", ex.getMessage());
    }

    @Test
    void NumberValidatorValidateTestWithCustomMinimumMessage() {
        JsonObject element = new JsonObject();
        element.addProperty("value", 5);

        Schema schema = new Schema();
        schema.setType(Type.of(SchemaType.INTEGER));
        schema.setMinimum(10);

        Map<String, Object> uiHelper = new HashMap<>();
        uiHelper.put("minimum", "Minimum custom error for {value}");
        schema.setUiHelper(uiHelper);

        SchemaValidationException ex = assertThrows(SchemaValidationException.class,
                () -> NumberValidator.validate(SchemaType.INTEGER, null, schema, element.get("value")));

        assertEquals("Minimum custom error for 5", ex.getMessage());
    }

    @Test
    void NumberValidatorValidateTestWithCustomMaximumMessage() {
        JsonObject element = new JsonObject();
        element.addProperty("value", 20);

        Schema schema = new Schema();
        schema.setType(Type.of(SchemaType.INTEGER));
        schema.setMaximum(15);

        Map<String, Object> uiHelper = new HashMap<>();
        uiHelper.put("maximum", "Maximum custom error for {value}");
        schema.setUiHelper(uiHelper);

        SchemaValidationException ex = assertThrows(SchemaValidationException.class,
                () -> NumberValidator.validate(SchemaType.INTEGER, null, schema, element.get("value")));

        assertEquals("Maximum custom error for 20", ex.getMessage());
    }

    @Test
    void NumberValidatorValidateTestWithCustomExclusiveMinimumMessage() {
        JsonObject element = new JsonObject();
        element.addProperty("value", 10);

        Schema schema = new Schema();
        schema.setType(Type.of(SchemaType.INTEGER));
        schema.setExclusiveMinimum(10);

        Map<String, Object> uiHelper = new HashMap<>();
        uiHelper.put("exclusiveMinimum", "Exclusive min custom error for {value}");
        schema.setUiHelper(uiHelper);

        SchemaValidationException ex = assertThrows(SchemaValidationException.class,
                () -> NumberValidator.validate(SchemaType.INTEGER, null, schema, element.get("value")));

        assertEquals("Exclusive min custom error for 10", ex.getMessage());
    }

    @Test
    void NumberValidatorValidateTestWithCustomExclusiveMaximumMessage() {
        JsonObject element = new JsonObject();
        element.addProperty("value", 11);

        Schema schema = new Schema();
        schema.setType(Type.of(SchemaType.INTEGER));
        schema.setExclusiveMaximum(10);

        Map<String, Object> uiHelper = new HashMap<>();
        uiHelper.put("exclusiveMaximum", "Exclusive max custom error for {value}");
        schema.setUiHelper(uiHelper);

        SchemaValidationException ex = assertThrows(SchemaValidationException.class,
                () -> NumberValidator.validate(SchemaType.INTEGER, null, schema, element.get("value")));

        assertEquals("Exclusive max custom error for 11", ex.getMessage());
    }

}

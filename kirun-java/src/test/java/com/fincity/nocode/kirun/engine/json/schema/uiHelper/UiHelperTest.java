package com.fincity.nocode.kirun.engine.json.schema.uiHelper;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class UiHelperTest {

    @Test
    void testDefaultConstructor() {
        UiHelper uiHelper = new UiHelper();
        assertNotNull(uiHelper);
        assertNotNull("", uiHelper.getValidationMessage("any"));
        assertNull(uiHelper.getComponentPreferred());
    }

    @Test
    void testParameterizedConstructor() {
        Map<String, String> messages = new HashMap<>();
        messages.put("required", "This field is required");
        UiHelper uiHelper = new UiHelper(messages, "Dropdown");

        assertEquals("This field is required", uiHelper.getRequiredMessage());
        assertEquals("Dropdown", uiHelper.getComponentPreferred());
    }

    @Test
    void testSetAndGetValidationMessage() {
        UiHelper uiHelper = new UiHelper();
        uiHelper.setValidationMessage("custom", "Custom validation message");

        assertEquals("Custom validation message", uiHelper.getValidationMessage("custom"));
    }

    @Test
    void testRequiredMessage() {
        UiHelper uiHelper = new UiHelper();
        uiHelper.setRequiredMessage("Field is required");

        assertEquals("Field is required", uiHelper.getRequiredMessage());
    }

    @Test
    void testMinLengthMessage() {
        UiHelper uiHelper = new UiHelper();
        uiHelper.setMinLengthMessage("Minimum length is 5");

        assertEquals("Minimum length is 5", uiHelper.getMinLengthMessage());
    }

    @Test
    void testMaxLengthMessage() {
        UiHelper uiHelper = new UiHelper();
        uiHelper.setMaxLengthMessage("Maximum length is 10");

        assertEquals("Maximum length is 10", uiHelper.getMaxLengthMessage());
    }

    @Test
    void testPatternMessage() {
        UiHelper uiHelper = new UiHelper();
        uiHelper.setPatternMessage("Invalid pattern");

        assertEquals("Invalid pattern", uiHelper.getPatternMessage());
    }

    @Test
    void testMinValueMessage() {
        UiHelper uiHelper = new UiHelper();
        uiHelper.setMinValueMessage("Value must be at least 1");

        assertEquals("Value must be at least 1", uiHelper.getMinValueMessage());
    }

    @Test
    void testMaxValueMessage() {
        UiHelper uiHelper = new UiHelper();
        uiHelper.setMaxValueMessage("Value cannot exceed 100");

        assertEquals("Value cannot exceed 100", uiHelper.getMaxValueMessage());
    }

    @Test
    void testSetAndGetComponentPreferred() {
        UiHelper uiHelper = new UiHelper();
        uiHelper.setComponentPreferred("RadioButton");

        assertEquals("RadioButton", uiHelper.getComponentPreferred());
    }

    @Test
    void testFromMethod() {
        Map<String, Object> inputMap = new HashMap<>();
        Map<String, String> messages = new HashMap<>();
        messages.put("required", "This field is required");
        inputMap.put("validationMessages", messages);
        inputMap.put("componentPreferred", "RadioButton");

        UiHelper uiHelper = UiHelper.from(inputMap);

        assertNotNull(uiHelper);
        assertEquals("This field is required", uiHelper.getRequiredMessage());
        assertEquals("RadioButton", uiHelper.getComponentPreferred());
    }

    @Test
    void testFromMethodWithNull() {
        assertNull(UiHelper.from(null));
    }
}

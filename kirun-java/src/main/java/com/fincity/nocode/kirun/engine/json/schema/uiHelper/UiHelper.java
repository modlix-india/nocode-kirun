package com.fincity.nocode.kirun.engine.json.schema.uiHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UiHelper {

    private Map<String, String> validationMessages;
    private String componentPreferred;

    public UiHelper() {
        this.validationMessages = new HashMap<>();
        this.componentPreferred = null;
    }

    public UiHelper(Map<String, String> validationMessages, String componentPreferred) {
        this.validationMessages = Optional.ofNullable(validationMessages).orElse(new HashMap<>());
        this.componentPreferred = componentPreferred;
    }

    public UiHelper setValidationMessage(String type, String message) {
        this.validationMessages.put(type, message);
        return this;
    }

    public String getValidationMessage(String type) {
        return this.validationMessages.getOrDefault(type, "");
    }

    public UiHelper setRequiredMessage(String message) {
        return this.setValidationMessage("required", message);
    }

    public String getRequiredMessage() {
        return this.getValidationMessage("required");
    }

    public UiHelper setMinLengthMessage(String message) {
        return this.setValidationMessage("minLength", message);
    }

    public String getMinLengthMessage() {
        return this.getValidationMessage("minLength");
    }

    public UiHelper setMaxLengthMessage(String message) {
        return this.setValidationMessage("maxLength", message);
    }

    public String getMaxLengthMessage() {
        return this.getValidationMessage("maxLength");
    }

    public UiHelper setPatternMessage(String message) {
        return this.setValidationMessage("pattern", message);
    }

    public String getPatternMessage() {
        return this.getValidationMessage("pattern");
    }

    public UiHelper setMinValueMessage(String message) {
        return this.setValidationMessage("minimum", message);
    }

    public String getMinValueMessage() {
        return this.getValidationMessage("minimum");
    }

    public UiHelper setMaxValueMessage(String message) {
        return this.setValidationMessage("maximum", message);
    }

    public String getMaxValueMessage() {
        return this.getValidationMessage("maximum");
    }

    public UiHelper setExclusiveMinimumMessage(String message) {
        return this.setValidationMessage("exclusiveMinimum", message);
    }

    public String getExclusiveMinimumMessage() {
        return this.getValidationMessage("exclusiveMinimum");
    }

    public UiHelper setExclusiveMaximumMessage(String message) {
        return this.setValidationMessage("exclusiveMaximum", message);
    }

    public String getExclusiveMaximumMessage() {
        return this.getValidationMessage("exclusiveMaximum");
    }

    public UiHelper setMultipleOfMessage(String message) {
        return this.setValidationMessage("multipleOf", message);
    }

    public String getMultipleOfMessage() {
        return this.getValidationMessage("multipleOf");
    }

    public String getComponentPreferred() {
        return componentPreferred;
    }

    public UiHelper setComponentPreferred(String componentPreferred) {
        this.componentPreferred = componentPreferred;
        return this;
    }

    public static UiHelper from(Map<String, Object> obj) {
        if (obj == null) {
            return null;
        }

        Map<String, String> validationMessages = new HashMap<>();
        Object messagesObj = obj.get("validationMessages");

        if (messagesObj instanceof Map<?, ?>) {
            Map<?, ?> messages = (Map<?, ?>) messagesObj;
            messages.forEach((key, value) -> {
                if (key instanceof String && value instanceof String) {
                    validationMessages.put((String) key, (String) value);
                }
            });
        }

        String componentPreferred = obj.get("componentPreferred") instanceof String
                ? (String) obj.get("componentPreferred")
                : null;
        return new UiHelper(validationMessages, componentPreferred);
    }
}

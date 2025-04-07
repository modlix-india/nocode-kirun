package com.fincity.nocode.kirun.engine.json.schema.uiHelper;

import java.util.HashMap;
import java.util.Map;

public class UiHelper {

    private Map<String, String> validationMessages = new HashMap<>();
    private String componentPreferred;

    public UiHelper() {
    }

    public UiHelper(Map<String, String> validationMessages, String componentPreferred) {
        this.validationMessages = validationMessages != null ? validationMessages : new HashMap<>();
        this.componentPreferred = componentPreferred;
    }

    public UiHelper(Map<String, Object> obj) {
        Object messagesObj = obj.get("validationMessages");

        if (messagesObj instanceof Map) {
            this.validationMessages.putAll((Map<String, String>) messagesObj);
        }

        Object componentPreferredObj = obj.get("componentPreferred");
        if (componentPreferredObj instanceof String string) {
            this.componentPreferred = string;
        }
    }

    public UiHelper setValidationMessage(String type, String message) {
        this.validationMessages.put(type, message);
        return this;
    }

    public String getValidationMessage(String type) {
        return this.validationMessages.get(type);
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
        return obj != null ? new UiHelper(obj) : null;
    }
}

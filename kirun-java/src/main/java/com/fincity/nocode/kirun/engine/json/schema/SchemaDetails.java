package com.fincity.nocode.kirun.engine.json.schema;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class SchemaDetails {

    public static final String MINIMUM = "minimum";
    public static final String MAXIMUM = "maximum";
    public static final String MULTIPLE_OF = "multipleOf";
    public static final String EXCLUSIVE_MINIMUM = "exclusiveMinimum";
    public static final String EXCLUSIVE_MAXIMUM = "exclusiveMaximum";

    public static final String MANDATORY = "mandatory";

    public static final String MIN_LENGTH = "minLength";
    public static final String MAX_LENGTH = "maxLength";
    public static final String PATTERN = "pattern";

    private String preferredComponent;
    private Map<String, String> validationMessages;
    private Map<String, Object> properties;
    private Map<String, Object> styleProperties;

    public SchemaDetails(SchemaDetails schemaDetails) {
        this.preferredComponent = schemaDetails.preferredComponent;
        this.validationMessages = schemaDetails.validationMessages;
        this.properties = schemaDetails.properties;
        this.styleProperties = schemaDetails.styleProperties;
    }

    public String getValidationMessage(String validationType, String defaultMessage) {

        if (validationType == null || validationMessages == null) return defaultMessage;

        final String msg = validationMessages.get(validationType);
        if (msg == null || msg.isBlank()) return defaultMessage;

        return msg;
    }

    public static String getValidationMessage(Schema schema, String validationType, String defaultMessage) {

        if (schema == null  || schema.getDetails() == null || validationType == null) return defaultMessage;
        return schema.getDetails().getValidationMessage(validationType, defaultMessage);
    }


}
package com.fincity.nocode.kirun.engine.json.schema.validator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.reactive.ReactiveSchemaUtil;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import static com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator.path;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NumberValidator {

    public static JsonElement validate(SchemaType type, List<Schema> parents, Schema schema, JsonElement element) {

        if (element == null || element.isJsonNull()) {
            throw new SchemaValidationException(path(parents), "Expected a number but found null");
        }

        if (!element.isJsonPrimitive() || !((JsonPrimitive) element).isNumber()) {
            throw new SchemaValidationException(path(parents),
                    element.toString() + " is not a " + type.getPrintableName());
        }

        JsonPrimitive jp = (JsonPrimitive) element;

        Number n = extractNumber(type, parents, schema, element, jp);

        checkRange(parents, schema, element, n);

        checkMultipleOf(parents, schema, element, n);

        return element;

    }

    private static void checkMultipleOf(List<Schema> parents, Schema schema, JsonElement element, Number n) {
        if (schema.getMultipleOf() == null) {
            return;
        }

        boolean invalid = false;

        if (n instanceof Float || n instanceof Double || n instanceof BigDecimal) {
            Double d1 = Double.valueOf(n.toString());
            Double d2 = Double.valueOf(schema.getMultipleOf().toString());
            Double d = d1 / d2;
            invalid = !Double.valueOf("" + d.intValue()).equals(d);
        } else {
            Long l1 = Long.valueOf(n.toString());
            Long l2 = Long.valueOf(schema.getMultipleOf().toString());
            invalid = (l1 % l2 != 0);
        }

        if (invalid) {
            String customMessage = null;

            if (schema.getUiHelper() != null && schema.getUiHelper().get("multipleOf") instanceof String) {
                customMessage = (String) schema.getUiHelper().get("multipleOf");
            }

            String msg = ReactiveSchemaUtil.customMessageProcessor(customMessage, element);

            throw new SchemaValidationException(path(parents),
                    msg != null ? msg : element.toString() + " is not multiple of " + schema.getMultipleOf());
        }
    }

    private static void checkRange(List<Schema> parents, Schema schema, JsonElement element, Number n) {

        Map<String, Object> uiHelper = schema.getUiHelper();

        if (schema.getMinimum() != null && numberCompare(n, schema.getMinimum()) < 0) {
            String msg = ReactiveSchemaUtil.customMessageProcessor(
                    uiHelper != null ? (String) uiHelper.get("minimum") : null, element);

            throw new SchemaValidationException(path(parents),
                    msg != null ? msg : element.toString() + " should be greater than or equal to " + schema.getMinimum());
        }

        if (schema.getMaximum() != null && numberCompare(n, schema.getMaximum()) > 0) {
            String msg = ReactiveSchemaUtil.customMessageProcessor(
                    uiHelper != null ? (String) uiHelper.get("maximum") : null, element);

            throw new SchemaValidationException(path(parents),
                    msg != null ? msg : element.toString() + " should be less than or equal to " + schema.getMaximum());
        }

        if (schema.getExclusiveMinimum() != null && numberCompare(n, schema.getExclusiveMinimum()) <= 0) {
            String msg = ReactiveSchemaUtil.customMessageProcessor(
                    uiHelper != null ? (String) uiHelper.get("exclusiveMinimum") : null, element);

            throw new SchemaValidationException(path(parents),
                    msg != null ? msg : element.toString() + " should be greater than " + schema.getExclusiveMinimum());
        }

        if (schema.getExclusiveMaximum() != null && numberCompare(n, schema.getExclusiveMaximum()) > 0) {
            String msg = ReactiveSchemaUtil.customMessageProcessor(
                    uiHelper != null ? (String) uiHelper.get("exclusiveMaximum") : null, element);

            throw new SchemaValidationException(path(parents),
                    msg != null ? msg : element.toString() + " should be less than " + schema.getExclusiveMaximum());
        }
    }

    private static Number extractNumber(SchemaType type, List<Schema> parents, Schema schema, JsonElement element,
            JsonPrimitive jp) {
        Number n = null;

        try {
            if (type == SchemaType.LONG) {
                n = jp.getAsLong();
            }
            if (type == SchemaType.INTEGER) {
                n = jp.getAsInt();
            }
            if (type == SchemaType.FLOAT) {
                n = jp.getAsFloat();
            }
            if (type == SchemaType.DOUBLE) {
                n = jp.getAsDouble();
            }
        } catch (Exception ex) {
            throw new SchemaValidationException(path(parents),
                    element.toString() + " is not a number of type " + type.getPrintableName());
        }

        if (n == null || (type == SchemaType.LONG || type == SchemaType.INTEGER) && n.doubleValue() != jp.getAsDouble()) {

            throw new SchemaValidationException(path(parents),
                    element.toString() + " is not a number of type " + type.getPrintableName());
        }

        return n;
    }

    private static int numberCompare(Number n1, Number n2) {

        if (n1 instanceof Float || n1 instanceof Double || n1 instanceof BigDecimal) {
            Double d1 = Double.valueOf(n1.toString());
            Double d2 = Double.valueOf(n2.toString());

            return d1.compareTo(d2);
        } else {
            Long l1 = Long.valueOf(n1.toString());
            Long l2 = Long.valueOf(n2.toString());

            return l1.compareTo(l2);
        }
    }

    private NumberValidator() {
    }
}

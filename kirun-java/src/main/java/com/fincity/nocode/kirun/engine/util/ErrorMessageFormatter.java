package com.fincity.nocode.kirun.engine.util;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * Utility class for formatting error messages with proper value representation.
 * Provides methods to format values, function names, statement names, and schema definitions
 * for consistent error messaging across the KIRun runtime.
 */
public class ErrorMessageFormatter {

    private static final int DEFAULT_MAX_LENGTH = 200;
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private ErrorMessageFormatter() {
        // Private constructor to prevent instantiation
    }

    /**
     * Formats a value for display in error messages with default max length.
     *
     * @param value The value to format
     * @return Formatted string representation
     */
    public static String formatValue(Object value) {
        return formatValue(value, DEFAULT_MAX_LENGTH);
    }

    /**
     * Formats a value for display in error messages.
     * - JSON representation for objects and arrays
     * - toString() for primitives
     * - Handles circular references
     *
     * @param value     The value to format
     * @param maxLength Maximum length of the formatted string
     * @return Formatted string representation
     */
    public static String formatValue(Object value, int maxLength) {
        if (value == null)
            return "null";

        // Handle primitives
        if (value instanceof String)
            return "\"" + value + "\"";
        if (value instanceof Number || value instanceof Boolean)
            return String.valueOf(value);

        // For objects and arrays, use JSON with circular reference handling
        try {
            String json = toJsonString(value);

            // Add minimal spacing for readability
            String formatted = json;
            if (json.length() > 2) {
                formatted = json.replaceAll(",", ", ")
                        .replaceAll(":", ": ")
                        .replaceAll("\\s+", " ");
            }

            // Truncate if too long
            if (formatted.length() > maxLength) {
                return formatted.substring(0, maxLength) + "...";
            }
            return formatted;
        } catch (Exception e) {
            // Fallback if JSON serialization fails
            return "[" + value.getClass().getSimpleName() + "]";
        }
    }

    /**
     * Converts an object to JSON string with circular reference handling.
     *
     * @param value The value to serialize
     * @return JSON string representation
     */
    private static String toJsonString(Object value) {
        IdentityHashMap<Object, Boolean> seen = new IdentityHashMap<>();
        return toJsonStringInternal(value, seen);
    }

    /**
     * Internal method to convert object to JSON with circular reference tracking.
     *
     * @param value The value to serialize
     * @param seen  Map of already seen objects for circular reference detection
     * @return JSON string representation
     */
    private static String toJsonStringInternal(Object value, IdentityHashMap<Object, Boolean> seen) {
        if (value == null)
            return "null";

        // Primitives - no circular reference possible
        if (value instanceof String)
            return GSON.toJson(value);
        if (value instanceof Number || value instanceof Boolean)
            return String.valueOf(value);

        // Check for circular reference before processing
        if (seen.containsKey(value)) {
            return "\"[Circular]\"";
        }

        // Mark as seen
        seen.put(value, Boolean.TRUE);

        try {
            // Handle Map
            if (value instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) value;
                StringBuilder sb = new StringBuilder("{");
                boolean first = true;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append(GSON.toJson(String.valueOf(entry.getKey())));
                    sb.append(":");
                    sb.append(toJsonStringInternal(entry.getValue(), seen));
                }
                sb.append("}");
                return sb.toString();
            }

            // Handle Iterable (List, Set, etc.)
            if (value instanceof Iterable) {
                Iterable<?> iterable = (Iterable<?>) value;
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                for (Object item : iterable) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append(toJsonStringInternal(item, seen));
                }
                sb.append("]");
                return sb.toString();
            }

            // For other objects, use Gson (no circular refs in simple objects)
            return GSON.toJson(value);
        } finally {
            // Remove from seen after processing
            seen.remove(value);
        }
    }

    /**
     * Formats a function identifier (namespace.name) with proper handling of undefined values.
     *
     * @param namespace Function namespace (can be null)
     * @param name      Function name
     * @return Formatted function identifier
     */
    public static String formatFunctionName(String namespace, String name) {
        if (namespace == null || namespace.isEmpty() ||
            "undefined".equals(namespace) || "null".equals(namespace)) {
            return name;
        }
        return namespace + "." + name;
    }

    /**
     * Formats a statement name with fallback for unknown statements.
     *
     * @param statementName The statement name (can be null)
     * @return Formatted statement name or null if not available
     */
    public static String formatStatementName(String statementName) {
        if (statementName == null || statementName.isEmpty() ||
            "undefined".equals(statementName) || "null".equals(statementName)) {
            return null;
        }
        return "'" + statementName + "'";
    }

    /**
     * Formats a schema definition for display in error messages.
     *
     * @param schema The schema to format
     * @return Formatted schema description
     */
    public static String formatSchemaDefinition(Schema schema) {
        if (schema == null)
            return "any";

        var type = schema.getType();
        if (type == null)
            return "any";

        // Get all allowed types
        Set<SchemaType> allowedTypes = type.getAllowedSchemaTypes();

        if (allowedTypes == null || allowedTypes.isEmpty())
            return "any";
        if (allowedTypes.size() == 1) {
            SchemaType singleType = allowedTypes.iterator().next();
            return formatSingleSchemaType(schema, singleType);
        }

        // Multiple types
        return allowedTypes.stream()
                .map(SchemaType::getPrintableName)
                .collect(Collectors.joining(" | "));
    }

    /**
     * Formats a single schema type with additional constraints.
     *
     * @param schema     The schema
     * @param schemaType The schema type
     * @return Formatted type description
     */
    private static String formatSingleSchemaType(Schema schema, SchemaType schemaType) {
        String typeName = schemaType.getPrintableName();

        // Add array item type if applicable
        if (schemaType == SchemaType.ARRAY) {
            ArraySchemaType items = schema.getItems();
            if (items != null) {
                Schema singleSchema = items.getSingleSchema();
                if (singleSchema != null) {
                    String itemType = formatSchemaDefinition(singleSchema);
                    return "Array<" + itemType + ">";
                }
                List<Schema> tupleSchemas = items.getTupleSchema();
                if (tupleSchemas != null && !tupleSchemas.isEmpty()) {
                    String tupleTypes = tupleSchemas.stream()
                            .map(ErrorMessageFormatter::formatSchemaDefinition)
                            .collect(Collectors.joining(", "));
                    return "[" + tupleTypes + "]";
                }
            }
            return "Array";
        }

        // Add enum constraint if applicable
        List<JsonElement> enums = schema.getEnums();
        if (enums != null && !enums.isEmpty()) {
            String enumValues = enums.stream()
                    .limit(5)
                    .map(e -> formatValue(GSON.fromJson(e, Object.class), 50))
                    .collect(Collectors.joining(" | "));
            String more = enums.size() > 5 ? " | ..." : "";
            return typeName + "(" + enumValues + more + ")";
        }

        return typeName;
    }

    /**
     * Builds an error message for function execution with optional statement name and parameter definition.
     *
     * @param functionName    The formatted function name
     * @param statementName   The formatted statement name (or null)
     * @param errorMessage    The error message
     * @param parameterName   Optional parameter name for parameter validation errors
     * @param parameterSchema Optional parameter schema for showing definition
     * @return Complete error message
     */
    public static String buildFunctionExecutionError(
            String functionName,
            String statementName,
            String errorMessage,
            String parameterName,
            Schema parameterSchema) {

        String parameterPart = parameterName != null ? "'s parameter " + parameterName : "";
        String statementPart = statementName != null ? " in statement " + statementName : "";
        String definitionPart = parameterSchema != null
                ? " [Expected: " + formatSchemaDefinition(parameterSchema) + "]"
                : "";

        // If the error message is already a nested error (starts with "Error while executing"),
        // add a newline before it for better readability
        String separator = errorMessage.startsWith("Error while executing the function ") ? "\n" : "";

        return "Error while executing the function " + functionName + parameterPart + statementPart
                + definitionPart + ": " + separator + errorMessage;
    }

    /**
     * Builds an error message for function execution without parameter schema.
     *
     * @param functionName  The formatted function name
     * @param statementName The formatted statement name (or null)
     * @param errorMessage  The error message
     * @param parameterName Optional parameter name for parameter validation errors
     * @return Complete error message
     */
    public static String buildFunctionExecutionError(
            String functionName,
            String statementName,
            String errorMessage,
            String parameterName) {
        return buildFunctionExecutionError(functionName, statementName, errorMessage, parameterName, null);
    }

    /**
     * Builds an error message for function execution without parameter information.
     *
     * @param functionName  The formatted function name
     * @param statementName The formatted statement name (or null)
     * @param errorMessage  The error message
     * @return Complete error message
     */
    public static String buildFunctionExecutionError(
            String functionName,
            String statementName,
            String errorMessage) {
        return buildFunctionExecutionError(functionName, statementName, errorMessage, null, null);
    }

    /**
     * Extracts and formats error message from various error types.
     *
     * @param error The error object
     * @return Formatted error message
     */
    public static String formatErrorMessage(Throwable error) {
        if (error == null)
            return "Unknown error";

        // Extract message from throwable
        String message = error.getMessage();
        if (message == null || message.isEmpty())
            return "Unknown error";

        // Check if the message contains object representations that need formatting
        if (message.contains("[object Object]")) {
            // Try to replace [object Object] with actual object representation
            return message.replaceAll("\\[object Object\\]", formatValue(error));
        }

        return message;
    }

    /**
     * Extracts and formats error message from Object (for compatibility with dynamic errors).
     *
     * @param error The error object
     * @return Formatted error message
     */
    public static String formatErrorMessage(Object error) {
        if (error == null)
            return "Unknown error";

        if (error instanceof String)
            return (String) error;

        if (error instanceof Throwable)
            return formatErrorMessage((Throwable) error);

        // For other objects, try to extract message or convert to string
        return formatValue(error);
    }
}

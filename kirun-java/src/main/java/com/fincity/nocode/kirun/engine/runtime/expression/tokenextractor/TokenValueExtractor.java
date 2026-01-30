package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.runtime.expression.exception.ExpressionEvaluationException;
import com.fincity.nocode.kirun.engine.util.stream.StreamUtil;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.fincity.nocode.kirun.engine.util.string.StringUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public abstract class TokenValueExtractor {

    public static final String REGEX_SQUARE_BRACKETS = "[\\[\\]]";
    public static final String REGEX_DOT = "(?<!\\.)\\.(?!\\.)";

    // Cache for split paths to avoid repeated regex splitting
    private static final Map<String, String[]> pathCache = new ConcurrentHashMap<>();

    // Cache for bracket segments
    private static final Map<String, String[]> bracketCache = new ConcurrentHashMap<>();

    private static final String LENGTH = "length";

    /**
     * Split a token by dots and cache the result.
     * Enhanced to handle bracket notation with keys containing dots.
     */
    public static String[] splitPath(String token) {
        return pathCache.computeIfAbsent(token, TokenValueExtractor::splitPathInternal);
    }

    private static String[] splitPathInternal(String token) {
        List<String> parts = new ArrayList<>();
        int start = 0;
        boolean inBracket = false;

        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);

            if (c == '[') {
                inBracket = true;
            } else if (c == ']') {
                inBracket = false;
            } else if (c == '.' && !isDoubleDot(token, i)) {
                // Never split on dots inside brackets, as they may contain:
                // - Quoted keys with dots: ["mail.props.port"]
                // - Dynamic expressions: [Steps.loop.iteration.index]
                if (!inBracket) {
                    // Found a separator dot; trim segments so "value. Percentage" resolves to "Percentage"
                    if (i > start) {
                        String segment = token.substring(start, i).trim();
                        parts.add(segment);
                    }
                    start = i + 1;
                }
            }
        }

        // Add the last part
        if (start < token.length()) {
            parts.add(token.substring(start).trim());
        }

        return parts.toArray(new String[0]);
    }

    private static boolean isDoubleDot(String str, int pos) {
        // Check if this dot is part of a ".." range operator
        return (pos > 0 && str.charAt(pos - 1) == '.') ||
               (pos < str.length() - 1 && str.charAt(pos + 1) == '.');
    }
    

    public JsonElement getValue(String token) {

        String prefix = this.getPrefix();

        if (!token.startsWith(prefix))
            throw new KIRuntimeException(StringFormatter.format("Token $ doesn't start with $", token, prefix));

        return this.getValueInternal(token);
    }

    protected JsonElement retrieveElementFrom(String token, String[] parts, int partNumber, JsonElement jsonElement) {
        // Iterative version - avoids recursive call overhead
        JsonElement current = jsonElement;
        
        for (int partIdx = partNumber; partIdx < parts.length; partIdx++) {
            if (current == null || current == JsonNull.INSTANCE) return null;
            
            // Use cached bracket segment parsing
            String[] segments = parseBracketSegment(parts[partIdx]);
            
            for (String segment : segments) {
                current = resolveSegmentFast(token, parts, partIdx, segment, current);
                if (current == null) return null;
            }
        }
        
        return current;
    }
    
    // Fast path for common cases - inline to avoid function call overhead
    private JsonElement resolveSegmentFast(String token, String[] parts, int partNumber, String segment,
                                          JsonElement element) {
        if (element == null || element == JsonNull.INSTANCE) return null;

        // Skip fast path for quoted segments - they need quote stripping
        if (segment.startsWith("\"") || segment.startsWith("'")) {
            return resolveForEachPartOfTokenWithBrackets(token, parts, partNumber, segment, element);
        }

        // Fast path: simple property access on object (most common case)
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            // For 'length' on objects, check if there's a length property
            // If it's a primitive (number, string, boolean), use it
            // If it's an object/array, use Object.keys length to avoid bugs
            if (LENGTH.equals(segment)) {
                if (obj.has(LENGTH)) {
                    JsonElement lengthValue = obj.get(LENGTH);
                    // If length property is a primitive, use it; otherwise use Object.keys length
                    if (lengthValue.isJsonObject() || lengthValue.isJsonArray()) {
                        return new JsonPrimitive(obj.size());
                    }
                    return lengthValue;
                }
                return new JsonPrimitive(obj.size());
            }
            if (obj.has(segment)) {
                return obj.get(segment);
            }
            return obj.get(segment); // May return null
        }
        
        // Fast path: array index access
        if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            // Check for 'length' first
            if (LENGTH.equals(segment)) return new JsonPrimitive(arr.size());
            
            // Only use fast path for pure integer strings (no range operators like '..')
            // Note: parseInt("2..4", 10) incorrectly returns 2, so we need to validate first
            if (segment.matches("^-?\\d+$")) {
                int idx = Integer.parseInt(segment);
                int actualIdx = idx < 0 ? arr.size() + idx : idx;
                return (actualIdx >= 0 && actualIdx < arr.size()) ? arr.get(actualIdx) : null;
            }
        }
        
        // Fast path: string access
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String str = element.getAsString();
            if (LENGTH.equals(segment)) return new JsonPrimitive(str.length());
            // Only use fast path for pure integer strings
            if (segment.matches("^-?\\d+$")) {
                int idx = Integer.parseInt(segment);
                int actualIdx = idx < 0 ? str.length() + idx : idx;
                return (actualIdx >= 0 && actualIdx < str.length()) 
                    ? new JsonPrimitive(str.charAt(actualIdx)) : null;
            }
        }
        
        // Fall back to full handling for edge cases (range operator, etc.)
        return resolveForEachPartOfTokenWithBrackets(token, parts, partNumber, segment, element);
    }
    
    // Parse bracket segments with caching
    private static String[] parseBracketSegment(String segment) {
        return bracketCache.computeIfAbsent(segment, s -> {
            String[] parts = s.split(REGEX_SQUARE_BRACKETS);
            List<String> result = new ArrayList<>();
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
            return result.toArray(new String[0]);
        });
    }

    protected JsonElement resolveForEachPartOfTokenWithBrackets(String token, String[] parts, int partNumber,
                                                                String cPart, JsonElement cElement) {

        if (cElement == null || cElement == JsonNull.INSTANCE)
            return null;

        // Check for 'length' keyword - both unquoted and quoted versions
        // e.g., .length and ["length"] should both return the length
        if (LENGTH.equals(cPart) || "\"length\"".equals(cPart) || "'length'".equals(cPart))
            return getLength(token, cElement);

        if ((cElement.isJsonPrimitive() && cElement.getAsJsonPrimitive().isString()) || cElement.isJsonArray())
            return handleArrayAccess(token, cPart, cElement);

        return handleObjectAccess(token, parts, partNumber, cPart, cElement);
    }

    protected abstract JsonElement getValueInternal(String token);

    public abstract String getPrefix();

    public abstract JsonElement getStore();

    private JsonElement getLength(String token, JsonElement cElement) {
        if (cElement.isJsonArray())
            return new JsonPrimitive(cElement.getAsJsonArray().size());
        if (cElement.isJsonObject()) {
            JsonObject jsonObject = cElement.getAsJsonObject();
            // For objects, check if there's a length property
            // If it's a primitive (number, string, boolean), use it
            // If it's an object/array, use Object.keys length to avoid bugs
            if (jsonObject.has(LENGTH)) {
                JsonElement lengthValue = jsonObject.get(LENGTH);
                // If length property is a primitive, use it; otherwise use Object.keys length
                if (lengthValue.isJsonObject() || lengthValue.isJsonArray()) {
                    return new JsonPrimitive(jsonObject.size());
                }
                return lengthValue;
            }
            return new JsonPrimitive(jsonObject.size());
        }
        if (cElement.isJsonPrimitive() && cElement.getAsJsonPrimitive().isString())
            return new JsonPrimitive(cElement.getAsString().length());

        throw new ExpressionEvaluationException(token,
                StringFormatter.format("Length can't be found in token $", token));
    }

    private JsonElement handleArrayAccess(String token, String cPart, JsonElement cElement) {

        if (!cElement.isJsonArray() && !cElement.isJsonPrimitive()) {
            throw new ExpressionEvaluationException(token, "Element is not an array or string");
        }

        int dotDotIndex = cPart.indexOf("..");
        if (dotDotIndex >= 0) {
            String startIndexStr = cPart.substring(0, dotDotIndex);
            String endIndexStr = cPart.substring(dotDotIndex + 2);

            int intStart = StringUtil.isNullOrBlank(startIndexStr) ? 0 : Integer.parseInt(startIndexStr);

            intStart = adjustIndex(intStart, cElement);

            int intEnd = endIndexStr.isEmpty() ?
                    (cElement.isJsonPrimitive() ? cElement.getAsString().length() : cElement.getAsJsonArray().size())
                    : Integer.parseInt(endIndexStr);

            intEnd = adjustIndex(intEnd, cElement);

            if (intStart >= intEnd) {
                return cElement.isJsonPrimitive() ? new JsonPrimitive("") : new JsonArray();
            }

            if (cElement.isJsonPrimitive()) {
                String cArray = cElement.getAsString();
                return new JsonPrimitive(cArray.substring(intStart, Math.min(intEnd, cArray.length())));
            } else {
                JsonArray jsonArray = cElement.getAsJsonArray();
                JsonArray resultArray = new JsonArray();

                for (int i = intStart; i < intEnd && i < jsonArray.size(); i++) {
                    resultArray.add(jsonArray.get(i));
                }
                return resultArray;
            }
        }

        try {
            int index = Integer.parseInt(cPart);
            index = adjustIndex(index, cElement);

            if (cElement.isJsonPrimitive()) {
                String cArray = cElement.getAsString();
                if (index < 0 || index >= cArray.length()) {
                    throw new ExpressionEvaluationException(token, "Index out of bounds");
                }
                return new JsonPrimitive(cArray.charAt(index));
            } else {
                JsonArray jsonArray = cElement.getAsJsonArray();
                if (index < 0 || index >= jsonArray.size()) {
                    throw new ExpressionEvaluationException(token, "Index out of bounds");
                }
                return jsonArray.get(index);
            }
        } catch (NumberFormatException ex) {
            throw new ExpressionEvaluationException(token, StringFormatter.format("$ couldn't be parsed into integer in $", cPart, token));
        }
    }

    private int adjustIndex(int index, JsonElement element) {
        int length = element.isJsonPrimitive() ? element.getAsString().length() : element.getAsJsonArray().size();
        if (index < 0) {
            index += length;
        }
        return Math.min(index, length);
    }

    private JsonElement handleObjectAccess(String token, String[] parts, int partNumber, String cPart,
                                           JsonElement cObject) {

        // Handle both single and double quoted keys
        if (cPart.startsWith("\"") || cPart.startsWith("'")) {
            char quoteChar = cPart.charAt(0);
            if (!cPart.endsWith(String.valueOf(quoteChar)) || cPart.length() == 1 || cPart.length() == 2)
                throw new ExpressionEvaluationException(token,
                        StringFormatter.format("$ is missing a closing quote or empty key found", token));

            cPart = cPart.substring(1, cPart.length() - 1);
        }

        checkIfObject(token, parts, partNumber, cObject);

        return cObject.getAsJsonObject().get(cPart);
    }

    private void checkIfObject(String token, String[] parts, int partNumber, JsonElement jsonElement) {
        if (!jsonElement.isJsonObject())
            throw new ExpressionEvaluationException(token, StringFormatter.format(
                    "Unable to retrive $ from $ in the path $", parts[partNumber], jsonElement.toString(), token));
    }
}

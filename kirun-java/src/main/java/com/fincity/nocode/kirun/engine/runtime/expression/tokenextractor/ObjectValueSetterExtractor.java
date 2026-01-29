package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.runtime.expression.Operation;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class ObjectValueSetterExtractor extends TokenValueExtractor {

    private JsonElement store;
    private String prefix;

    public ObjectValueSetterExtractor(JsonElement store, String prefix) {
        super();
        this.store = store;
        this.prefix = prefix;
    }

    public JsonElement getStore() {
        return this.store;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    public void setStore(JsonElement store) {
        this.store = store;
    }

    public void setValue(String token, JsonElement value, Boolean overwrite, Boolean deleteOnNull) {
        this.store = store.deepCopy();
        this.modifyStore(token, value, overwrite, deleteOnNull);
    }

    @Override
    protected JsonElement getValueInternal(String token) {

        String[] parts = TokenValueExtractor.splitPath(token);
        return this.retrieveElementFrom(token, parts, 1, getStore());

    }

    private void modifyStore(String stringToken, JsonElement value, Boolean overwrite, Boolean deleteOnNull) {
        overwrite = overwrite != null ? overwrite : true;
        deleteOnNull = deleteOnNull != null ? deleteOnNull : false;

        // Use TokenValueExtractor.splitPath to get path segments instead of Expression parsing
        // This is more reliable as it directly handles the path string
        String[] parts = TokenValueExtractor.splitPath(stringToken);
        
        if (parts.length < 2) {
            throw new KIRuntimeException(
                StringFormatter.format("Invalid path: $", stringToken));
        }
        
        // Start from index 1 (skip the prefix like 'Store')
        JsonElement el = this.store;
        
        // Navigate to the parent of the final element
        for (int i = 1; i < parts.length - 1; i++) {
            String part = parts[i];
            String nextPart = parts[i + 1];
            
            // Parse bracket segments within this part
            String[] segments = parseBracketSegments(part);
            
            for (int j = 0; j < segments.length; j++) {
                String segment = segments[j];
                boolean isLastSegmentOfPart = (j == segments.length - 1);

                Operation nextOp;
                if (isLastSegmentOfPart) {
                    // This is the last segment of this part, look at the next part
                    boolean isLastPart = (i == parts.length - 2);
                    if (isLastPart) {
                        nextOp = getOpForSegment(parts[parts.length - 1]);
                    } else {
                        nextOp = getOpForSegment(nextPart);
                    }
                } else {
                    // There are more segments in this part, look at the next segment
                    nextOp = isArrayIndex(segments[j + 1]) ? Operation.ARRAY_OPERATOR : Operation.OBJECT_OPERATOR;
                }

                if (isArrayIndex(segment)) {
                    el = getDataFromArray(el, segment, nextOp);
                } else {
                    el = getDataFromObject(el, stripQuotes(segment), nextOp);
                }
            }
        }
        
        // Handle the final part (set the value)
        String finalPart = parts[parts.length - 1];
        String[] finalSegments = parseBracketSegments(finalPart);
        
        // Navigate through all but the last segment of the final part
        for (int j = 0; j < finalSegments.length - 1; j++) {
            String segment = finalSegments[j];
            Operation nextOp = isArrayIndex(finalSegments[j + 1]) ? Operation.ARRAY_OPERATOR : Operation.OBJECT_OPERATOR;
            
            if (isArrayIndex(segment)) {
                el = getDataFromArray(el, segment, nextOp);
            } else {
                el = getDataFromObject(el, stripQuotes(segment), nextOp);
            }
        }
        
        // Set the final value
        String lastSegment = finalSegments[finalSegments.length - 1];
        if (isArrayIndex(lastSegment)) {
            putDataInArray(el, lastSegment, value, overwrite, deleteOnNull);
        } else {
            putDataInObject(el, stripQuotes(lastSegment), value, overwrite, deleteOnNull);
        }
    }
    
    /**
     * Parse a path segment that may contain bracket notation.
     * E.g., "addresses[0]" -> ["addresses", "0"]
     * E.g., "obj[\"key\"]" -> ["obj", "key"]
     */
    private String[] parseBracketSegments(String part) {
        List<String> segments = new ArrayList<>();
        int start = 0;
        int i = 0;
        
        while (i < part.length()) {
            if (part.charAt(i) == '[') {
                if (i > start) {
                    segments.add(part.substring(start, i));
                }
                // Find matching ]
                int end = i + 1;
                boolean inQuote = false;
                char quoteChar = 0;
                while (end < part.length()) {
                    if (inQuote) {
                        if (part.charAt(end) == quoteChar && (end == 0 || part.charAt(end - 1) != '\\')) {
                            inQuote = false;
                        }
                    } else {
                        if (part.charAt(end) == '"' || part.charAt(end) == '\'') {
                            inQuote = true;
                            quoteChar = part.charAt(end);
                        } else if (part.charAt(end) == ']') {
                            break;
                        }
                    }
                    end++;
                }
                // Extract bracket content (without the brackets)
                segments.add(part.substring(i + 1, end));
                start = end + 1;
                i = start;
            } else {
                i++;
            }
        }
        
        if (start < part.length()) {
            segments.add(part.substring(start));
        }
        
        return segments.isEmpty() ? new String[]{part} : segments.toArray(new String[0]);
    }
    
    /**
     * Check if a segment is an array index (numeric)
     */
    private boolean isArrayIndex(String segment) {
        // Check if it's a pure number (possibly negative)
        return segment.matches("^-?\\d+$");
    }
    
    /**
     * Strip quotes from a segment if present
     */
    private String stripQuotes(String segment) {
        if ((segment.startsWith("\"") && segment.endsWith("\"")) ||
            (segment.startsWith("'") && segment.endsWith("'"))) {
            return segment.substring(1, segment.length() - 1);
        }
        return segment;
    }
    
    /**
     * Determine the operation type for the next segment
     */
    private Operation getOpForSegment(String segment) {
        // Check if the segment starts with a bracket or is a pure number
        if (isArrayIndex(segment) || segment.startsWith("[")) {
            return Operation.ARRAY_OPERATOR;
        }
        return Operation.OBJECT_OPERATOR;
    }

    private JsonElement getDataFromArray(JsonElement el, String mem, Operation nextOp) {
        if (!el.isJsonArray())
            throw new KIRuntimeException(
                    StringFormatter.format("Expected an array but found $", el));

        int index;
        try {
            index = Integer.parseInt(mem);
        } catch (Exception e) {
            throw new KIRuntimeException(
                    StringFormatter.format("Expected an array index but found $", mem));
        }

        if (index < 0)
            throw new KIRuntimeException(
                    StringFormatter.format("Array index is out of bound - $", mem));

        JsonArray arr = el.getAsJsonArray();
        while (index >= arr.size())
            arr.add(JsonNull.INSTANCE);

        JsonElement je = arr.get(index);

        if (je == null || je.isJsonNull()) {
            je = nextOp == Operation.OBJECT_OPERATOR ? new JsonObject() : new JsonArray();
            arr.set(index, je);
        }

        return je;
    }

    private JsonElement getDataFromObject(JsonElement el, String mem, Operation nextOp) {
        if (el.isJsonArray() || !el.isJsonObject())
            throw new KIRuntimeException(
                    StringFormatter.format("Expected an object but found $", el));

        JsonObject jo = el.getAsJsonObject();
        JsonElement je = jo.get(mem);

        if (je == null || je.isJsonNull()) {
            je = nextOp == Operation.OBJECT_OPERATOR ? new JsonObject() : new JsonArray();
            jo.add(mem, je);
        }
        return je;
    }

    private void putDataInArray(JsonElement el, String mem, JsonElement value, Boolean overwrite,
            Boolean deleteOnNull) {
        if (!el.isJsonArray())
            throw new KIRuntimeException(
                    StringFormatter.format("Expected an array but found $", el));

        int index;
        try {
            index = Integer.parseInt(mem);
        } catch (Exception e) {
            throw new KIRuntimeException(
                    StringFormatter.format("Expected an array index but found $", mem));
        }

        if (index < 0)
            throw new KIRuntimeException(
                    StringFormatter.format("Array index is out of bound - $", mem));

        JsonArray arr = el.getAsJsonArray();
        while (index >= arr.size())
            arr.add(JsonNull.INSTANCE);

        if (Boolean.TRUE.equals(overwrite) || arr.get(index) == null || arr.get(index).isJsonNull()) {
            if (Boolean.TRUE.equals(deleteOnNull) && (value == null || value.isJsonNull())) {
                arr.remove(index);
            } else {
                arr.set(index, value);
            }
        }
    }

    private void putDataInObject(JsonElement el, String mem, JsonElement value, Boolean overwrite,
            Boolean deleteOnNull) {
        if (el.isJsonArray() || !el.isJsonObject())
            throw new KIRuntimeException(
                    StringFormatter.format("Expected an object but found $", el));

        JsonObject jo = el.getAsJsonObject();
        if (Boolean.TRUE.equals(overwrite) || jo.get(mem) == null || jo.get(mem).isJsonNull()) {
            if (Boolean.TRUE.equals(deleteOnNull) && (value == null || value.isJsonNull())) {
                jo.remove(mem);
            } else {
                jo.add(mem, value);
            }
        }
    }
}

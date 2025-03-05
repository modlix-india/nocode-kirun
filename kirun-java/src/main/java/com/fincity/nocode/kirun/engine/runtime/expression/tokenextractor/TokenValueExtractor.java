package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

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
    //public static final String REGEX_DOT = "\\.";
    public static final String REGEX_DOT = "(?<!\\.)\\.(?!\\.)";


    private static final String LENGTH = "length";

    public JsonElement getValue(String token) {

        String prefix = this.getPrefix();

        if (!token.startsWith(prefix))
            throw new KIRuntimeException(StringFormatter.format("Token $ doesn't start with $", token, prefix));

        return this.getValueInternal(token);
    }

    protected JsonElement retrieveElementFrom(String token, String[] parts, int partNumber, JsonElement jsonElement) {

        if (jsonElement == null || jsonElement == JsonNull.INSTANCE)
            return null;

        if (parts.length == partNumber)
            return jsonElement;

        JsonElement bElement = StreamUtil.sequentialReduce(
                Stream.of(parts[partNumber].split(REGEX_SQUARE_BRACKETS)).sequential().map(String::trim)
                        .filter(Predicate.not(String::isBlank)),
                jsonElement,
                (c, a, i) -> resolveForEachPartOfTokenWithBrackets(token, parts, partNumber, c, a));

        return retrieveElementFrom(token, parts, partNumber + 1, bElement);
    }

    protected JsonElement resolveForEachPartOfTokenWithBrackets(String token, String[] parts, int partNumber,
                                                                String cPart, JsonElement cElement) {

        if (cElement == null || cElement == JsonNull.INSTANCE)
            return null;

        if (LENGTH.equals(cPart))
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
            return jsonObject.has(LENGTH) ? jsonObject.get(LENGTH) : new JsonPrimitive(jsonObject.size());
        }
        if (cElement.isJsonPrimitive() && cElement.getAsJsonPrimitive().isString())
            return new JsonPrimitive(cElement.getAsString().length());

        throw new ExpressionEvaluationException(token,
                StringFormatter.format("Length can't be found in token $", token));
    }

    // private JsonElement handleArrayAccess(String token, String cPart, JsonArray cArray) {
    // 	try {
    // 		int index = Integer.parseInt(cPart);
    // 		return (index < cArray.size()) ? cArray.get(index) : null;
    // 	} catch (NumberFormatException ex) {
    // 		throw new ExpressionEvaluationException(token,
    // 				StringFormatter.format("$ couldn't be parsed into integer in $", cPart, token));
    // 	}
    // }
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

            int intEnd = (endIndexStr == null || endIndexStr.isEmpty()) ?
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

        if (cPart.startsWith("\"")) {
            if (!cPart.endsWith("\"") || cPart.length() == 1 || cPart.length() == 2)
                throw new ExpressionEvaluationException(token,
                        StringFormatter.format("$ is missing a double quote or empty key found", token));

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

package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import java.util.function.Predicate;
import java.util.stream.Stream;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.runtime.expression.exception.ExpressionEvaluationException;
import com.fincity.nocode.kirun.engine.util.stream.StreamUtil;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public abstract class TokenValueExtractor {

	public static final String REGEX_SQUARE_BRACKETS = "[\\[\\]]";
	public static final String REGEX_DOT = "\\.";

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
			return getLength(token, cPart, cElement);

		if (cElement.isJsonArray())
			return handleArrayAccess(token, cPart, cElement.getAsJsonArray());

		return handleObjectAccess(token, parts, partNumber, cPart, cElement);
	}

	protected abstract JsonElement getValueInternal(String token);

	public abstract String getPrefix();

	public abstract JsonElement getStore();

	private JsonElement getLength(String token, String cPart, JsonElement cElement) {

		if (cElement.isJsonArray())
			return new JsonPrimitive(cElement.getAsJsonArray().size());
		if (cElement.isJsonObject()) {
			JsonObject jsonObject = cElement.getAsJsonObject();
			return jsonObject.has(LENGTH) ? jsonObject.get(LENGTH) : new JsonPrimitive(jsonObject.size());
		}
		if (cElement.isJsonPrimitive() && cElement.getAsJsonPrimitive().isString())
			return new JsonPrimitive(cElement.getAsString().length());

		throw new ExpressionEvaluationException(token,
				StringFormatter.format("$ length can't be found in token $", cPart, token));
	}

	private JsonElement handleArrayAccess(String token, String cPart, JsonArray cArray) {
		try {
			int index = Integer.parseInt(cPart);
			return (index < cArray.size()) ? cArray.get(index) : null;
		} catch (NumberFormatException ex) {
			throw new ExpressionEvaluationException(token,
					StringFormatter.format("$ couldn't be parsed into integer in $", cPart, token));
		}
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

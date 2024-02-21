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

		JsonElement bElement = StreamUtil.sequentialReduce(Stream.of(parts[partNumber].split(REGEX_SQUARE_BRACKETS))
				.sequential()
				.map(String::trim)
				.filter(Predicate.not(String::isBlank)), jsonElement,
				(c, a, i) -> resolveForEachPartOfTokenWithBrackets(token, parts, partNumber, c, a, i));

		return retrieveElementFrom(token, parts, partNumber + 1, bElement);
	}

	protected JsonElement resolveForEachPartOfTokenWithBrackets(String token, String[] parts, int partNumber, String c,
			JsonElement a, Integer i) {

		if (a == null || a == JsonNull.INSTANCE)
			return null;

		if (i == 0) {

			if (LENGTH.equals(c)) {
				if (a.isJsonArray())
					return new JsonPrimitive(a.getAsJsonArray()
							.size());
				else if (a.isJsonObject())
					return new JsonPrimitive(a.getAsJsonObject().size());
				else if (a.isJsonPrimitive() && a.getAsJsonPrimitive().isString())
					return new JsonPrimitive(a.getAsString().length());
			}

			if (a.isJsonArray()) {

				try {
					int index = Integer.parseInt(c);
					JsonArray ja = a.getAsJsonArray();

					if (index >= ja.size())
						return null;

					return ja.get(index);
				} catch (Exception ex) {
					throw new ExpressionEvaluationException(token,
							StringFormatter.format("$ couldn't be parsed into integer in $", c, token));
				}
			}

			checkIfObject(token, parts, partNumber, a);
			return a.getAsJsonObject()
					.get(c);
		} else if (c.startsWith("\"")) {

			if (!c.endsWith("\"") || c.length() == 1 || c.length() == 2)
				throw new ExpressionEvaluationException(token,
						StringFormatter.format("$ is missing a double quote or empty key found", token));

			checkIfObject(token, parts, partNumber, a);
			return a.getAsJsonObject()
					.get(c.substring(1, c.length() - 1));
		}

		try {
			int index = Integer.parseInt(c);

			if (!a.isJsonArray())
				throw new ExpressionEvaluationException(token, StringFormatter
						.format("Expecting an array with index $ while processing the expression", index, token));
			JsonArray ja = a.getAsJsonArray();

			if (index >= ja.size())
				return null;

			return ja.get(index);
		} catch (Exception ex) {
			throw new ExpressionEvaluationException(token,
					StringFormatter.format("$ couldn't be parsed into integer in $", c, token));
		}
	}

	protected void checkIfObject(String token, String[] parts, int partNumber, JsonElement jsonElement) {

		if (!jsonElement.isJsonObject())
			throw new ExpressionEvaluationException(token, StringFormatter.format(
					"Unable to retrive $ from $ in the path $", parts[partNumber], jsonElement.toString(), token));
	}

	protected abstract JsonElement getValueInternal(String token);

	public abstract String getPrefix();

	public abstract JsonElement getStore();
}
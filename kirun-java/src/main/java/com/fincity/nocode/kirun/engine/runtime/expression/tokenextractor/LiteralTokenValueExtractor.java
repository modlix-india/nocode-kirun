package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fincity.nocode.kirun.engine.runtime.expression.exception.ExpressionEvaluationException;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public class LiteralTokenValueExtractor extends TokenValueExtractor {

	public static final LiteralTokenValueExtractor INSTANCE = new LiteralTokenValueExtractor();

	private static final Map<String, JsonElement> KEYWORDS;

	static {
		Map<String, JsonElement> map = new HashMap<>(
				Map.of("true", new JsonPrimitive(true), "false", new JsonPrimitive(false)));
		map.put("null", null);
		KEYWORDS = Collections.unmodifiableMap(map);
	}

	@Override
	protected JsonElement getValueInternal(String token) {

		if (token == null || token.isBlank())
			return null;

		token = token.trim();

		if (KEYWORDS.containsKey(token))
			return KEYWORDS.get(token);

		if (token.startsWith("\"")) {
			return processString(token);
		}

		return processNumbers(token);
	}

	private JsonElement processNumbers(String token) {
		try {

			int ind = token.indexOf('.');
			if (ind == -1) {

				Long num = Long.parseLong(token);
				int intNum = num.intValue();
				if (num == intNum)
					return new JsonPrimitive(intNum);
				return new JsonPrimitive(num);
			} else {
				// Check if the part after the dot is numeric (for decimal numbers like "2.5")
				// If not, treat the entire token as a string literal (e.g., "2.val")
				String afterDot = token.substring(ind + 1);
				boolean isNumeric = true;
				for (int i = 0; i < afterDot.length(); i++) {
					char c = afterDot.charAt(i);
					if (!Character.isDigit(c)) {
						isNumeric = false;
						break;
					}
				}
				
				if (!isNumeric) {
					// Not a number - return as string literal
					return new JsonPrimitive(token);
				}

				Double d = Double.parseDouble(token);
				Float f = d.floatValue();

				if (d == 0.0d)
					return new JsonPrimitive(f);

				return f != 0.0f && f != Float.POSITIVE_INFINITY && f != Float.NEGATIVE_INFINITY ? new JsonPrimitive(f)
						: new JsonPrimitive(d);
			}
		} catch (Exception ex) {
			// If parsing fails, treat as string literal
			return new JsonPrimitive(token);
		}
	}

	private JsonElement processString(String token) {
		if (!token.endsWith("\""))
			throw new ExpressionEvaluationException(token,
					StringFormatter.format("String literal $ is not closed properly", token));

		return new JsonPrimitive(token.substring(1, token.length() - 1));
	}

	@Override
	public String getPrefix() {
		return "";
	}

	@Override
	public JsonElement getStore() {
		return JsonNull.INSTANCE;
	}

	public JsonElement getValueFromExtractors(String token, Map<String, TokenValueExtractor> maps) {

		if (token == null || token.isBlank())
			return JsonNull.INSTANCE;

		String prefix = token + ".";

		if (maps.containsKey(prefix)) {
			return maps.get(prefix).getStore();
		}

		return this.getValue(token);
	}
}

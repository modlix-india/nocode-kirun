package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;

public abstract class TokenValueExtractor {

	public JsonElement getValue(String token) {

		String prefix = this.getPrefix();

		if (!token.startsWith(prefix))
			throw new KIRuntimeException(StringFormatter.format("Token $ doesn't start with $", token, prefix));

		return null;
	}

	public abstract String getPrefix();
}
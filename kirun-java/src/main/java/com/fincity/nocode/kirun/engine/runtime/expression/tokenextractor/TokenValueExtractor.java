package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.google.gson.JsonElement;

public abstract class TokenValueExtractor {

	public JsonElement getValue(String token) {
		
		String prefix = this.getPrefix();
		
		if (!token.startsWith(prefix))
			throw new KIRuntimeException(StringForma);
		
		return null;
	}
	
	public abstract String getPrefix();
}
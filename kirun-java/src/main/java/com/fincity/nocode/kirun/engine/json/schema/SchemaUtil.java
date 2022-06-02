package com.fincity.nocode.kirun.engine.json.schema;

import com.fincity.nocode.kirun.engine.Repository;
import com.google.gson.JsonElement;

public class SchemaUtil {

	public static JsonElement getDefaultValue(Schema s, Repository<Schema> sRepository) {
		
		if (s == null)
			return null;

		if (s.getConstant() != null)
			return s.getConstant();

		if (s.getDefaultValue() != null)
			return s.getDefaultValue();

		if (s.getRef() == null || s.getRef()
		        .isBlank())
			return null;

		return getDefaultValue(sRepository.find(s.getRef()), sRepository);
	}

	private SchemaUtil() {
	}
}

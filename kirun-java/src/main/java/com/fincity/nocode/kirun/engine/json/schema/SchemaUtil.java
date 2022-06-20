package com.fincity.nocode.kirun.engine.json.schema;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.util.string.StringUtil;
import com.google.gson.JsonElement;

public class SchemaUtil {

	public static JsonElement getDefaultValue(Schema s, Repository<Schema> sRepository) {

		if (s == null)
			return null;

		if (s.getConstant() != null)
			return s.getConstant();

		if (s.getDefaultValue() != null)
			return s.getDefaultValue();

		return getDefaultValue(getSchemaFromRef(s, sRepository, s.getRef()), sRepository);
	}

	public static Schema getSchemaFromRef(Schema schema, Repository<Schema> sRepository, String ref) {

		if (schema == null || ref == null || ref.isBlank())
			return null;

		if (!ref.startsWith("#")) {

			String[] nms = StringUtil.splitAtFirstOccurance(schema.getRef(), '/');
			String[] nmspnm = StringUtil.splitAtFirstOccurance(nms[0], '.');

			schema = sRepository.find(nmspnm[0], nmspnm[1]);
			if (nms[1] == null || nms[1].isBlank())
				return schema;

			ref = "#/" + nms[1];
		}

		String[] parts = ref.split("/");
		for (int i = 1; i < parts.length; i++) {

			
		}

		return schema;
	}

	private SchemaUtil() {
	}
}

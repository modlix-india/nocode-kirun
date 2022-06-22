package com.fincity.nocode.kirun.engine.json.schema;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaReferenceException;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.util.string.StringUtil;
import com.google.gson.JsonElement;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class SchemaUtil {

	private static final String UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH = "Unable to retrive schema from referenced path";

	private static final int CYCLIC_REFERENCE_LIMIT_COUNTER = 20;

	public static JsonElement getDefaultValue(Schema s, Repository<Schema> sRepository) {

		if (s == null)
			return null;

		if (s.getConstant() != null)
			return s.getConstant();

		if (s.getDefaultValue() != null)
			return s.getDefaultValue();

		return getDefaultValue(getSchemaFromRef(s, sRepository, s.getRef(), 0), sRepository);
	}

	public static Schema getSchemaFromRef(Schema schema, Repository<Schema> sRepository, String ref) {
		return getSchemaFromRef(schema, sRepository, ref, 0);
	}

	public static Schema getSchemaFromRef(Schema schema, Repository<Schema> sRepository, String ref, int iteration) {

		iteration++;

		if (iteration == CYCLIC_REFERENCE_LIMIT_COUNTER)
			throw new SchemaValidationException(ref, "Schema has a cyclic reference");

		if (schema == null || ref == null || ref.isBlank())
			return null;

		if (!ref.startsWith("#")) {

			var tuple = resolveExternalSchema(schema, sRepository, ref);
			schema = tuple.getT1();
			ref = tuple.getT2();
		}

		String[] parts = ref.split("/");
		int i = 1;

		schema = resolveInternalSchema(schema, sRepository, ref, iteration, parts, i);

		return schema;
	}

	private static Schema resolveInternalSchema(Schema schema, Repository<Schema> sRepository, String ref, // NOSONAR
	        int iteration, String[] parts, int i) {

		// Cannot divide the code further down in the interest of readability.

		while (i < parts.length) {

			if (parts[i].equals("$defs")) {
				i++;

				if (i >= parts.length || schema.get$defs() == null || schema.get$defs()
				        .isEmpty())
					throw new SchemaReferenceException(ref, UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH);

				schema = schema.get$defs()
				        .get(parts[i]);
			} else {

				if (!schema.getType()
				        .contains(SchemaType.OBJECT) || schema.getProperties() == null || schema.getProperties()
				                .isEmpty())
					throw new SchemaReferenceException(ref, "Cannot retrievie schema from non Object type schemas");

				schema = schema.getProperties()
				        .get(parts[i]);
			}

			i++;

			if (schema == null)
				throw new SchemaReferenceException(ref, UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH);

			if (!StringUtil.isNullOrBlank(schema.getRef())) {
				schema = getSchemaFromRef(schema, sRepository, schema.getRef(), iteration);
				if (schema == null)
					throw new SchemaReferenceException(ref, UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH);
			}
		}
		return schema;
	}

	private static Tuple2<Schema, String> resolveExternalSchema(Schema schema, Repository<Schema> sRepository,
	        String ref) {
		String[] nms = StringUtil.splitAtFirstOccurance(schema.getRef(), '/');
		String[] nmspnm = StringUtil.splitAtFirstOccurance(nms[0], '.');

		schema = sRepository.find(nmspnm[0], nmspnm[1]);
		if (nms[1] == null || nms[1].isBlank())
			return Tuples.of(schema, ref);

		ref = "#/" + nms[1];

		if (schema == null)
			throw new SchemaReferenceException(ref, UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH);

		return Tuples.of(schema, ref);
	}

	private SchemaUtil() {
	}
}

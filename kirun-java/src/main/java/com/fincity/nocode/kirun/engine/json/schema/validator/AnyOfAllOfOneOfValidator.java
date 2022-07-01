package com.fincity.nocode.kirun.engine.json.schema.validator;

import static com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator.path;

import java.util.ArrayList;
import java.util.List;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.google.gson.JsonElement;

public class AnyOfAllOfOneOfValidator {

	public static JsonElement validate(List<Schema> parents, Schema schema, Repository<Schema> repository,
	        JsonElement element) {

		List<SchemaValidationException> list = new ArrayList<>();
		if (schema.getOneOf() != null && !schema.getOneOf()
		        .isEmpty()) {
			oneOf(parents, schema, repository, element, list);
		} else if (schema.getAllOf() != null && !schema.getAllOf()
		        .isEmpty()) {
			allOf(parents, schema, repository, element, list);
		} else if (schema.getAnyOf() != null && !schema.getAnyOf()
		        .isEmpty()) {
			anyOf(parents, schema, repository, element, list);
		}

		return element;
	}

	private static void anyOf(List<Schema> parents, Schema schema, Repository<Schema> repository, JsonElement element,
	        List<SchemaValidationException> list) {
		boolean flag = false;
		for (Schema s : schema.getAnyOf()) {
			try {
				validate(parents, s, repository, element);
				flag = true;
				break;
			} catch (SchemaValidationException ex) {
				flag = false;
				list.add(ex);
			}
		}

		if (!flag) {
			throw new SchemaValidationException(path(parents), "The value don't satisfy any of the schemas.", list);
		}
	}

	private static void allOf(List<Schema> parents, Schema schema, Repository<Schema> repository, JsonElement element,
	        List<SchemaValidationException> list) {
		int flag = 0;
		for (Schema s : schema.getAllOf()) {
			try {
				validate(parents, s, repository, element);
				flag++;
			} catch (SchemaValidationException ex) {
				list.add(ex);
			}
		}

		if (flag != schema.getAllOf()
		        .size()) {
			throw new SchemaValidationException(path(parents), "The value doesn't satisfy some of the schemas.", list);
		}
	}

	private static void oneOf(List<Schema> parents, Schema schema, Repository<Schema> repository, JsonElement element,
	        List<SchemaValidationException> list) {
		int flag = 0;
		for (Schema s : schema.getOneOf()) {
			try {
				validate(parents, s, repository, element);
				flag++;
			} catch (SchemaValidationException ex) {
				list.add(ex);
			}
		}

		if (flag != 1) {
			throw new SchemaValidationException(path(parents),
			        (flag == 0 ? "The value does not satisfy any schema" : "The value satisfy more than one schema"),
			        list);
		}
	}

	private AnyOfAllOfOneOfValidator() {
	}
}

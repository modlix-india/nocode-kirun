package com.fincity.nocode.kirun.engine.json.schema.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.SchemaUtil;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.google.gson.JsonElement;

public class SchemaValidator {

	public static JsonElement validate(List<Schema> parents, Schema schema, Repository<Schema> repository,
	        JsonElement element) {

		if (schema == null) {
			throw new SchemaValidationException(SchemaValidator.path(parents), "No schema found to validate");
		}

		if (parents == null) {
			parents = new ArrayList<>();
		}
		parents.add(schema);

		if ((element == null || element.isJsonNull()) && schema.getDefaultValue() != null) {
			return schema.getDefaultValue().deepCopy();
		}

		if (schema.getConstant() != null) {
			return constantValidation(parents, schema, element);
		}

		if (schema.getEnums() != null && !schema.getEnums()
		        .isEmpty()) {
			return enumCheck(parents, schema, element);
		}

        if (schema.getFormat() != null && schema.getType() == null) {
            throw new SchemaValidationException(path(parents),
                    "Type is missing in schema for declared " + schema.getFormat().toString() + " format.");
        }
		
		if (schema.getType() != null) {
			typeValidation(parents, schema, repository, element);
		}

		// Need to write test cases to find out if element can be null at this point.
		if (schema.getRef() != null && !schema.getRef()
		        .isBlank()) {
			return validate(parents, SchemaUtil.getSchemaFromRef(parents.get(0), repository, schema.getRef()),
			        repository, element);
		}

		if (schema.getOneOf() != null || schema.getAllOf() != null || schema.getAnyOf() != null) {
			AnyOfAllOfOneOfValidator.validate(parents, schema, repository, element);
		}

		if (schema.getNot() != null) {
			boolean flag = false;
			try {
				validate(parents, schema.getNot(), repository, element);
				flag = true;
			} catch (SchemaValidationException sve) {
				flag = false;
			}
			if (flag)
				throw new SchemaValidationException(path(parents), "Schema validated value in not condition.");
		}

		return element;
	}

	public static JsonElement constantValidation(List<Schema> parents, Schema schema, JsonElement element) {
		if (!schema.getConstant()
		        .equals(element)) {
			throw new SchemaValidationException(path(parents), "Expecting a constant value : " + element);
		}
		return element;
	}

	public static JsonElement enumCheck(List<Schema> parents, Schema schema, JsonElement element) {

		boolean x = false;
		for (JsonElement e : schema.getEnums()) {

			if (e.equals(element)) {
				x = true;
				break;
			}
		}

		if (x)
			return element;
		else {
			throw new SchemaValidationException(path(parents), "Value is not one of " + schema.getEnums());
		}
	}

	public static void typeValidation(List<Schema> parents, Schema schema, Repository<Schema> repository,
	        JsonElement element) {

		boolean valid = false;
		List<SchemaValidationException> list = new ArrayList<>();
		for (SchemaType type : schema.getType()
		        .getAllowedSchemaTypes()) {

			try {
				TypeValidator.validate(parents, type, schema, repository, element);
				valid = true;
				break;
			} catch (SchemaValidationException sve) {
				valid = false;
				list.add(sve);
			}
		}

		if (!valid) {
			throw new SchemaValidationException(path(parents), "Value " + element + " is not of valid type(s)", list);
		}
	}	

	public static String path(List<Schema> parents) {

		return parents == null ? ""
		        : parents.stream()
		                .map(Schema::getTitle)
		                .collect(Collectors.joining("."));
	}

	private SchemaValidator() {
	}
}

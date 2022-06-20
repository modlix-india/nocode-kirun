package com.fincity.nocode.kirun.engine.json.schema.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.SchemaUtil;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.google.gson.JsonElement;

public class SchemaValidator {

	public static JsonElement validate(List<String> parents, Schema schema, Repository<Schema> repository,
	        JsonElement element) {

	
		if (schema == null) {
			return element;
		}
		
		if ((element == null || element.isJsonNull()) && schema.getDefaultValue() != null) {
			return schema.getDefaultValue();
		}

		if (schema.getConstant() != null) {
			return constantValidation(parents, schema, element);
		}

		if (schema.getEnums() != null && !schema.getEnums()
		        .isEmpty()) {
			return enumCheck(parents, schema, element);
		}

		if (schema.getType() != null) {
			typeValidation(parents, schema, repository, element);
			}

		// Need to write test cases to find out if element can be null at this point.
		if (schema.getRef() != null && !schema.getRef()
		        .isBlank() && element.isJsonObject()) {
			return validate(parents, SchemaUtil.getSchemaFromRef(schema, repository, schema.getRef()), repository,
			        element);
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
				throw new SchemaValidationException(path(parents, schema.getName()),
				        "Schema validated value in not condition.");
		}

		return element;
	}

	public static JsonElement constantValidation(List<String> parents, Schema schema, JsonElement element) {
		if (!schema.getConstant()
		        .equals(element)) {
			throw new SchemaValidationException(path(parents, schema.getName()),
			        "Expecting a constant value : " + element);
		}
		return element;
	}

	public static JsonElement enumCheck(List<String> parents, Schema schema, JsonElement element) {

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
			throw new SchemaValidationException(path(parents, schema.getName()),
			        "Value is not one of " + schema.getEnums());
		}
	}

	public static void typeValidation(List<String> parents, Schema schema, Repository<Schema> repository,
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
			throw new SchemaValidationException(path(parents, schema.getName()),
			        "Value " + element + " is not of valid type(s)", list);
		}
	}

	public static String path(List<String> parents, String id) {

		if (id == null)
			return "";

		if (parents == null || parents.isEmpty())
			return id;

		return parents.stream()
		        .collect(Collectors.joining("/")) + "/" + id + " ";
	}

	private SchemaValidator() {
	}
}

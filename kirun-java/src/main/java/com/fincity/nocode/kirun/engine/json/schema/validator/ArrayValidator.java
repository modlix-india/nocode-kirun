package com.fincity.nocode.kirun.engine.json.schema.validator;
 
import static com.fincity.nocode.kirun.engine.json.schema.validator.SchemaValidator.path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class ArrayValidator {

	public static JsonElement validate(List<String> parents, Schema schema, Repository<Schema> repository,
	        JsonElement element) {

		if (element == null || element.isJsonNull())
			throw new SchemaValidationException(path(parents, schema.getName()), "Expected an array but found null");

		if (!element.isJsonArray())
			throw new SchemaValidationException(path(parents, schema.getName()),
			        element.toString() + " is not an Array");

		JsonArray array = (JsonArray) element;

		checkMinMaxItems(parents, schema, array);

		checkItems(parents, schema, repository, array);
		
		checkUniqueItems(parents, schema, array);

		checkContains(parents, schema, repository, array);
		
		return element;
	}

	public static void checkContains(List<String> parents, Schema schema, Repository<Schema> repository,
	        JsonArray array) {

		if (schema.getContains() == null)
			return;

		boolean flag = false;
		for (int i = 0; i < array.size(); i++) {
			List<String> newParents = new ArrayList<>(parents == null ? List.of() : parents);
			newParents.add("" + i);
			try {
				SchemaValidator.validate(newParents, schema.getContains(), repository, array.get(i));
				flag = true;
				break;
			} catch (Exception ex) {
				flag = false;
			}
		}

		if (!flag) {
			throw new SchemaValidationException(path(parents, schema.getName()),
			        "None of the items are of type contains schema");
		}

	}

	public static void checkUniqueItems(List<String> parents, Schema schema, JsonArray array) {
		if (schema.getUniqueItems() != null && schema.getUniqueItems()
		        .booleanValue()) {

			Set<JsonElement> set = new HashSet<>();
			for (int i = 0; i < array.size(); i++) {
				set.add(array.get(i));
			}

			if (set.size() != array.size())
				throw new SchemaValidationException(path(parents, schema.getName()),
				        "Items on the array are not unique");
		}
	}

	public static void checkMinMaxItems(List<String> parents, Schema schema, JsonArray array) {
		if (schema.getMinItems() != null && schema.getMinItems()
		        .intValue() > array.size()) {
			throw new SchemaValidationException(path(parents, schema.getName()),
			        "Array should have minimum of " + schema.getMinItems() + " elements");
		}

		if (schema.getMaxItems() != null && schema.getMaxItems()
		        .intValue() < array.size()) {
			throw new SchemaValidationException(path(parents, schema.getName()),
			        "Array can have  maximum of " + schema.getMaxItems() + " elements");
		}
	}

	public static void checkItems(List<String> parents, Schema schema, Repository<Schema> repository,
	        JsonArray array) {
		ArraySchemaType type = schema.getItems();

		if (type == null)
			return;

		if (type.getSingleSchema() != null) {
			for (int i = 0; i < array.size(); i++) {
				List<String> newParents = new ArrayList<>(parents == null ? List.of() : parents);
				newParents.add("" + i);
				
				JsonElement element = SchemaValidator.validate(newParents, type.getSingleSchema(), repository,
				        array.get(i));
				array.set(i, element);
			}
		}

		if (type.getTupleSchema() != null) {
			if (type.getTupleSchema()
			        .size() != array.size()) {
				throw new SchemaValidationException(path(parents, schema.getName()),
				        "Expected an array with only " + type.getTupleSchema()
				                .size() + " but found " + array.size());
			}

			for (int i = 0; i < array.size(); i++) {
				List<String> newParents = new ArrayList<>(parents == null ? List.of() : parents);
				newParents.add("" + i);
				JsonElement element = SchemaValidator.validate(newParents, type.getTupleSchema()
				        .get(i), repository, array.get(i));
				array.set(i, element);
			}
		}
	}

	private ArrayValidator() {
	}
}

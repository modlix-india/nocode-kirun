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

	public static JsonElement validate(List<Schema> parents, Schema schema, Repository<Schema> repository,
	        JsonElement element) {

		if (element == null || element.isJsonNull())
			throw new SchemaValidationException(path(parents), "Expected an array but found null");

		if (!element.isJsonArray())
			throw new SchemaValidationException(path(parents),
			        element.toString() + " is not an Array");

		JsonArray array = (JsonArray) element;

		checkMinMaxItems(parents, schema, array);

		checkItems(parents, schema, repository, array);
		
		checkUniqueItems(parents, schema, array);

        if (schema.getContains() != null) {
            checkContains(parents, schema, repository, array);

            if (schema.getMinContains() != null)
                checkMinContains(parents, schema, repository, array);

            if (schema.getMaxContains() != null)
                checkMaxContains(parents, schema, repository, array);
        }
		
		return element;
	}

    public static void checkContains(List<Schema> parents, Schema schema, Repository<Schema> repository,
            JsonArray array) {

        boolean flag = false;
        for (int i = 0; i < array.size(); i++) {
            List<Schema> newParents = new ArrayList<>(parents == null ? List.of() : parents);
            try {
                SchemaValidator.validate(newParents, schema.getContains(), repository, array.get(i));
                flag = true;
                break;
            } catch (Exception ex) {

            }

        }

        if (!flag) {
            throw new SchemaValidationException(path(parents),
                    "None of the items are of type contains schema");
        }

    }

	public static void checkUniqueItems(List<Schema> parents, Schema schema, JsonArray array) {
		if (schema.getUniqueItems() != null && schema.getUniqueItems()
		        .booleanValue()) {

			Set<JsonElement> set = new HashSet<>();
			for (int i = 0; i < array.size(); i++) {
				set.add(array.get(i));
			}

			if (set.size() != array.size())
				throw new SchemaValidationException(path(parents),
				        "Items on the array are not unique");
		}
	}

	public static void checkMinMaxItems(List<Schema> parents, Schema schema, JsonArray array) {
		if (schema.getMinItems() != null && schema.getMinItems()
		        .intValue() > array.size()) {
			throw new SchemaValidationException(path(parents),
			        "Array should have minimum of " + schema.getMinItems() + " elements");
		}

		if (schema.getMaxItems() != null && schema.getMaxItems()
		        .intValue() < array.size()) {
			throw new SchemaValidationException(path(parents),
			        "Array can have  maximum of " + schema.getMaxItems() + " elements");
		}
	}

    public static void checkMinContains(List<Schema> parents, Schema schema, Repository<Schema> repository,
            JsonArray array) {

        int containsCount = 0;

        for (int i = 0; i < array.size(); i++) {
            List<Schema> newParents = new ArrayList<>(parents == null ? List.of() : parents);
            try {
                SchemaValidator.validate(newParents, schema.getContains(), repository, array.get(i));
                containsCount++;
            } catch (Exception ex) {

            }
        }
        if (schema.getMinContains() > containsCount)
            throw new SchemaValidationException(path(parents),
                    "The minimum number of the items defined are " + schema.getMinContains()
                            + " of type contains schema are not present");

    }

    public static void checkMaxContains(List<Schema> parents, Schema schema, Repository<Schema> repository,
            JsonArray array) {

        int containsCount = 0;

        for (int i = 0; i < array.size(); i++) {
            List<Schema> newParents = new ArrayList<>(parents == null ? List.of() : parents);
            try {
                SchemaValidator.validate(newParents, schema.getContains(), repository, array.get(i));
                containsCount++;
            } catch (Exception ex) {

            }
        }
        if (schema.getMaxContains() < containsCount)
            throw new SchemaValidationException(path(parents),
                    "The maximum number of the items defined are " + schema.getMaxContains()
                            + " of type contains schema are not present");

    }
    
	public static void checkItems(List<Schema> parents, Schema schema, Repository<Schema> repository,
	        JsonArray array) {
		ArraySchemaType type = schema.getItems();

		if (type == null)
			return;

		if (type.getSingleSchema() != null) {
			for (int i = 0; i < array.size(); i++) {
				List<Schema> newParents = new ArrayList<>(parents == null ? List.of() : parents);
				JsonElement element = SchemaValidator.validate(newParents, type.getSingleSchema(), repository,
				        array.get(i));
				array.set(i, element);
			}
		}

		if (type.getTupleSchema() != null) {
			if (type.getTupleSchema()
			        .size() != array.size()) {
				throw new SchemaValidationException(path(parents),
				        "Expected an array with only " + type.getTupleSchema()
				                .size() + " but found " + array.size());
			}

			for (int i = 0; i < array.size(); i++) {
				List<Schema> newParents = new ArrayList<>(parents == null ? List.of() : parents);
				JsonElement element = SchemaValidator.validate(newParents, type.getTupleSchema()
				        .get(i), repository, array.get(i));
				array.set(i, element);
			}
		}
	}

	private ArrayValidator() {
	}
}

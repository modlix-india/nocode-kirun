package com.fincity.nocode.kirun.engine.json.schema.validator.reactive;

import static com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator.path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public class ReactiveArrayValidator {

	private static final String BUT_FOUND = " but found ";

	public static Mono<JsonElement> validate(List<Schema> parents, Schema schema, ReactiveRepository<Schema> repository,
	        JsonElement element) {

		if (element == null || element.isJsonNull())
			return Mono.error(new SchemaValidationException(path(parents), "Expected an array but found null"));

		if (!element.isJsonArray())
			return Mono.error(new SchemaValidationException(path(parents), element.toString() + " is not an Array"));

		return Mono.just((JsonArray) element)

		        .flatMap(array ->
				{
			        try {
				        checkMinMaxItems(parents, schema, array);
			        } catch (SchemaValidationException sve) {
				        return Mono.error(sve);
			        }

			        return Mono.just(array);
		        })

		        .flatMap(array -> checkItems(parents, schema, repository, array))

		        .flatMap(array ->
				{
			        try {
				        checkUniqueItems(parents, schema, array);
			        } catch (SchemaValidationException sve) {
				        return Mono.error(sve);
			        }

			        return Mono.just(array);
		        })

		        .flatMap(array -> checkContains(parents, schema, repository, array));
	}

	public static Mono<JsonArray> checkContains(List<Schema> parents, Schema schema,
	        ReactiveRepository<Schema> repository, JsonArray array) {

		if (schema.getContains() == null)
			return Mono.just(array);

		return countContains(parents, schema, repository, array,
		        schema.getMinContains() == null && schema.getMaxContains() == null).flatMap(count ->
		{

			        if (count == 0)
				        return Mono.error(() -> new SchemaValidationException(path(parents),
				                "None of the items are of type contains schema"));

			        if (schema.getMinContains() != null && schema.getMinContains() > count)
				        return Mono.error(() -> new SchemaValidationException(path(parents),
				                "The minimum number of the items of type contains schema should be "
				                        + schema.getMinContains() + BUT_FOUND + count));

			        if (schema.getMaxContains() != null && schema.getMaxContains() < count)
				        return Mono.error(() -> new SchemaValidationException(path(parents),
				                "The maximum number of the items of type contains schema should be "
				                        + schema.getMaxContains() + BUT_FOUND + count));

			        return Mono.just(array);
		        });
	}

	private static Mono<Integer> countContains(List<Schema> parents, Schema schema,
	        ReactiveRepository<Schema> repository, JsonArray array, boolean stopPoint) {

		return Flux.fromIterable(array)
		        .flatMap(element ->
				{
			        List<Schema> newParents = new ArrayList<>(parents == null ? List.of() : parents);
			        return ReactiveSchemaValidator.validate(newParents, schema.getContains(), repository, element)
			                .map(e -> 1)
			                .onErrorReturn(0);
		        })
		        .takeUntil(e -> stopPoint && e == 1)
		        .reduce(Integer::sum);
	}

	public static Mono<JsonArray> checkItems(List<Schema> parents, Schema schema, ReactiveRepository<Schema> repository,
	        JsonArray array) {
		ArraySchemaType type = schema.getItems();

		if (type == null)
			return Mono.just(array);

		if (type.getSingleSchema() != null) {

			return Flux.fromIterable(array)
			        .flatMap(element ->
					{
				        List<Schema> newParents = new ArrayList<>(parents == null ? List.of() : parents);
				        return ReactiveSchemaValidator.validate(newParents, type.getSingleSchema(), repository,
				                element);
			        })
			        .collectList()
			        .map(e ->
					{

				        JsonArray ja = new JsonArray(e.size());
				        for (JsonElement el : e)
					        ja.add(el);
				        return ja;
			        });
		}

		if (type.getTupleSchema() != null) {
			if (type.getTupleSchema()
			        .size() != array.size()
			        && (array.size() < type.getTupleSchema()
			                .size() || !AdditionalType.canHaveAddtionalItems(schema.getAdditionalItems())))

				return Mono.error(() -> new SchemaValidationException(path(parents),
				        "Expected an array with only " + type.getTupleSchema()
				                .size() + BUT_FOUND + array.size()));

			return checkItemInTupleSchema(parents, repository, array, schema);
		}

		return Mono.error(() -> new SchemaValidationException(path(parents), "Invalid Array schema type"));
	}

	private static Mono<JsonArray> checkItemInTupleSchema(List<Schema> parents, ReactiveRepository<Schema> repository,
	        JsonArray array, Schema schema) {

		Flux<Tuple3<Integer, JsonElement, Optional<Schema>>> processing = Flux.create(sink -> {

			List<Schema> type = schema.getItems()
			        .getTupleSchema();

			int i = 0;
			for (i = 0; i < type.size(); i++) {
				sink.next(Tuples.of(i, array.get(i), Optional.of(type.get(i))));
			}

			Schema atSchema = schema.getAdditionalItems() == null ? null
			        : schema.getAdditionalItems()
			                .getSchemaValue();

			for (; i < array.size(); i++) {
				sink.next(Tuples.of(i, array.get(i), Optional.ofNullable(atSchema)));
			}

			sink.complete();
		});

		return processing.flatMap(tup -> {
			if (tup.getT3()
			        .isEmpty())
				return Mono.just(Tuples.of(tup.getT1(), tup.getT2()));

			List<Schema> newParents = new ArrayList<>(parents == null ? List.of() : parents);
			Mono<JsonElement> validated = ReactiveSchemaValidator.validate(newParents, tup.getT3()
			        .get(), repository, tup.getT2());

			return validated.map(e -> Tuples.of(tup.getT1(), e));
		})
		        .collectList()
		        .map(e ->
				{

			        JsonArray ja = new JsonArray(e.size());
			        for (var el : e)
				        ja.add(el.getT2());

			        return ja;
		        });
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

	private ReactiveArrayValidator() {
	}
}

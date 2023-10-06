package com.fincity.nocode.kirun.engine.json.schema.reactive;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaReferenceException;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.util.string.StringUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class ReactiveSchemaUtil {

	public static final String UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH = "Unable to retrive schema from referenced path";

	public static final int CYCLIC_REFERENCE_LIMIT_COUNTER = 20;

	public static Mono<JsonElement> getDefaultValue(Schema s, ReactiveRepository<Schema> sRepository) {

		if (s == null)
			return Mono.just(JsonNull.INSTANCE);

		if (s.getConstant() != null)
			return Mono.just(s.getConstant());

		if (s.getDefaultValue() != null)
			return Mono.just(s.getDefaultValue());

		return getSchemaFromRef(s, sRepository, s.getRef(), 0)

		        .flatMap(schema -> getDefaultValue(schema, sRepository))

		        .defaultIfEmpty(JsonNull.INSTANCE);
	}

	public static Mono<Boolean> hasDefaultValueOrNullSchemaType(Schema s, ReactiveRepository<Schema> repo) {
		if (s == null)
			return Mono.just(false);

		if (s.getConstant() != null)
			return Mono.just(true);

		if (s.getDefaultValue() != null)
			return Mono.just(true);

		if (s.getRef() == null) {
			return Mono.just(s.getType()
			        .getAllowedSchemaTypes()
			        .contains(SchemaType.NULL));
		}
		return getSchemaFromRef(s, repo, s.getRef())

		        .flatMap(schema -> hasDefaultValueOrNullSchemaType(schema, repo))

		        .defaultIfEmpty(Boolean.FALSE);
	}

	public static Mono<Schema> getSchemaFromRef(Schema schema, ReactiveRepository<Schema> sRepository, String ref) {
		return getSchemaFromRef(schema, sRepository, ref, 0);
	}

	public static Mono<Schema> getSchemaFromRef(Schema schema, ReactiveRepository<Schema> sRepository, final String ref,
	        int iteration) {

		if (iteration == CYCLIC_REFERENCE_LIMIT_COUNTER)
			return Mono.error(() -> new SchemaValidationException(ref, "Schema has a cyclic reference"));

		if (schema == null || ref == null || ref.isBlank())
			return Mono.empty();

		if (!ref.startsWith("#")) {

			return resolveExternalSchema(sRepository, ref).flatMap(tuple -> {

                if (tuple == null)
                    return Mono.empty();
	    
				Schema sch = tuple.getT1();
				String updateRef = tuple.getT2();

				String[] parts = ref.split("/");
				int i = 1;

				if (i == parts.length)
					return Mono.just(sch);

				return resolveInternalSchema(sch, sRepository, updateRef, iteration + 1, parts, i);
			});
		}

		String[] parts = ref.split("/");
		int i = 1;

		if (i == parts.length)
			return Mono.just(schema);

		return resolveInternalSchema(schema, sRepository, ref, iteration + 1, parts, i);
	}

	private static Mono<Schema> resolveInternalSchema(Schema sch, ReactiveRepository<Schema> sRepository, String ref, // NOSONAR
	        // Cannot split this logic as it doesn't make sense
	        int iteration, String[] parts, int index) {

		if (index == parts.length)
			return Mono.empty();

		return Mono.just(Tuples.of(sch, index))
		        .expand(tup ->
				{

			        int i = tup.getT2();
			        if (i >= parts.length)
				        return Mono.empty();

			        Schema schema = tup.getT1();
			        if (parts[i].equals("$defs")) {
				        i++;

				        if (i >= parts.length || schema.get$defs() == null || schema.get$defs()
				                .isEmpty())
					        return Mono.error(() -> new SchemaReferenceException(ref,
					                UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH));

				        schema = schema.get$defs()
				                .get(parts[i]);
			        } else {

				        if (!schema.getType()
				                .contains(SchemaType.OBJECT) || schema.getProperties() == null || schema.getProperties()
				                        .isEmpty())
					        return Mono.error(() -> new SchemaReferenceException(ref,
					                "Cannot retrievie schema from non Object type schemas"));

				        schema = schema.getProperties()
				                .get(parts[i]);
			        }

			        i++;

			        final int incrementedIndex = i;

			        if (schema == null)
				        return Mono.error(
				                () -> new SchemaReferenceException(ref, UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH));

			        if (!StringUtil.isNullOrBlank(schema.getRef())) {
				        return getSchemaFromRef(schema, sRepository, schema.getRef(), iteration)
				                .map(e -> Tuples.of(e, incrementedIndex))
				                .switchIfEmpty(Mono.defer(() -> Mono.error(new SchemaReferenceException(ref,
				                        UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH))));
			        }

			        return Mono.just(Tuples.of(schema, incrementedIndex));
		        })
		        .last()
		        .map(Tuple2::getT1);
	}

	private static Mono<Tuple2<Schema, String>> resolveExternalSchema(ReactiveRepository<Schema> sRepository,
	        String ref) {
		String[] nms = StringUtil.splitAtFirstOccurance(ref, '/');
		String[] nmspnm = StringUtil.splitAtFirstOccurance(nms[0], '.');

		return sRepository.find(nmspnm[0], nmspnm[1])
		        .flatMap(sch ->
				{
			        if (nms[1] == null || nms[1].isBlank())
				        return Mono.just(Tuples.of(sch, ref));

			        return Mono.just(Tuples.of(sch, "#/" + nms[1]));
		        })
		        .switchIfEmpty(Mono
		                .error(() -> new SchemaReferenceException(ref, UNABLE_TO_RETRIVE_SCHEMA_FROM_REFERENCED_PATH)));
	}

	private ReactiveSchemaUtil() {
	}
}

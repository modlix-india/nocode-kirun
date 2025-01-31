package com.fincity.nocode.kirun.engine.function.system;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.ParameterType;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class ValidateSchema extends AbstractReactiveFunction {

	private static final String SOURCE = "source";
	private static final String SCHEMA = "schema";
	private static final String IS_VALID = "isValid";

	private final Gson gson;

	public ValidateSchema() {

		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Type.class, new Type.SchemaTypeAdapter());
		AdditionalType.AdditionalTypeAdapter ata = new AdditionalType.AdditionalTypeAdapter();
		builder.registerTypeAdapter(AdditionalType.class, ata);
		ArraySchemaType.ArraySchemaTypeAdapter asta = new ArraySchemaType.ArraySchemaTypeAdapter();
		builder.registerTypeAdapter(ArraySchemaType.class, asta);
		gson = builder.create();
		ata.setGson(gson);
		asta.setGson(gson);
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
		JsonElement element = context.getArguments().get(SOURCE);

		JsonElement schemaJson = context.getArguments().get(SCHEMA);
		Schema schema = gson.fromJson(schemaJson, Schema.class);

		return this.validateSchema(schema, context.getSchemaRepository(), element);
	}

	private Mono<FunctionOutput> validateSchema(Schema targetSchema, ReactiveRepository<Schema> targetSchemaRepo,
			JsonElement element) {
		return ReactiveSchemaValidator.validate(null, targetSchema, targetSchemaRepo, element, true, null)
				.flatMap(isValid -> Mono.just(new FunctionOutput(
						List.of(EventResult.outputOf(Map.of(IS_VALID, new JsonPrimitive(Boolean.TRUE)))))))
				.onErrorReturn(new FunctionOutput(
						List.of(EventResult.outputOf(Map.of(IS_VALID, new JsonPrimitive(Boolean.FALSE))))));
	}

	@Override
	public FunctionSignature getSignature() {
		return new FunctionSignature()
				.setName("ValidateSchema")
				.setNamespace(Namespaces.SYSTEM)
				.setParameters(
						Map.ofEntries(
								Parameter.ofEntry(SOURCE, Schema.ofAny(SOURCE)),
								Parameter.ofEntry(SCHEMA, Schema.SCHEMA, ParameterType.CONSTANT)))
				.setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(IS_VALID, Schema.ofBoolean(IS_VALID)))));

	}
}

package com.fincity.nocode.kirun.engine.function.system.object;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.convertor.enums.ConversionMode;
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

import reactor.core.publisher.Mono;

public class ObjectConvert extends AbstractReactiveFunction {

	private static final String SOURCE = "source";
	static final String SCHEMA = "schema";
	private static final String VALUE = "value";
	private static final String CONVERSION_MODE = "conversionMode";
	private final Gson gson;

	public ObjectConvert() {

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

		ConversionMode mode = ConversionMode.genericValueOf(context.getArguments().get(CONVERSION_MODE).getAsString());

		return convertToSchema(schema, context.getSchemaRepository(), element, mode);
	}

	private Mono<FunctionOutput> convertToSchema(Schema targetSchema, ReactiveRepository<Schema> targetSchemaRepo,
			JsonElement element, ConversionMode mode) {

		return ReactiveSchemaValidator.validate(null, targetSchema, targetSchemaRepo, element, true, mode)
				.flatMap(convertedElement -> Mono
						.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, convertedElement))))))
				.onErrorMap(error -> new KIRuntimeException(error.getMessage(), error));
	}

	@Override
	public FunctionSignature getSignature() {
		return new FunctionSignature()
				.setName("ObjectConvert")
				.setNamespace(Namespaces.SYSTEM_OBJECT)
				.setParameters(
						Map.ofEntries(
								Parameter.ofEntry(SOURCE, Schema.ofAny(SCHEMA)),
								Parameter.ofEntry(SCHEMA, Schema.SCHEMA, ParameterType.CONSTANT),
								Parameter.ofEntry(CONVERSION_MODE, Schema.ofString(CONVERSION_MODE)
										.setEnums(ConversionMode.getConversionModes()))))
				.setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.ofAny(VALUE)))));
	}
}

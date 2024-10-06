package com.fincity.nocode.kirun.engine.function.system.object;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.convertor.enums.ConversionMode;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;

import reactor.core.publisher.Mono;

public class ObjectConvert extends AbstractReactiveFunction {

	private static final String SOURCE = "source";
	private static final String TARGET_NAMESPACE = "targetNamespace";
	private static final String TARGET = "target";
	private static final String VALUE = "value";
	private static final String CONVERSION_MODE = "conversionMode";

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonElement element = context.getArguments().get(SOURCE);

		String targetNamespace = context.getArguments().get(TARGET_NAMESPACE).getAsString();
		String target = context.getArguments().get(TARGET).getAsString();
		ConversionMode mode = ConversionMode.genericValueOf(context.getArguments().get(CONVERSION_MODE).getAsString());

		return convertToSchema(targetNamespace, target, context.getSchemaRepository(), element, mode);
	}

	private Mono<FunctionOutput> convertToSchema(String targetNamespace, String target,
			ReactiveRepository<Schema> targetSchemaRepo, JsonElement element, ConversionMode mode) {

		return targetSchemaRepo.find(targetNamespace, target)
				.switchIfEmpty(Mono.error(new KIRuntimeException(
						StringFormatter.format("No Schema found for Namespace $ and name $ in repo $",
								targetNamespace, target, targetSchemaRepo))))
				.flatMap(schema -> ReactiveSchemaValidator
						.validate(null, schema, targetSchemaRepo, element, true, mode))
				.onErrorMap(error -> new KIRuntimeException(error.getMessage(), error))
				.flatMap(convertedElement -> Mono
						.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, convertedElement))))));
	}

	@Override
	public FunctionSignature getSignature() {
		return new FunctionSignature()
				.setName("ObjectConvert")
				.setNamespace(Namespaces.SYSTEM_OBJECT)
				.setParameters(Map.of(
						SOURCE, new Parameter().setParameterName(SOURCE).setSchema(Schema.ofAny(SOURCE)),
						TARGET_NAMESPACE,
						new Parameter().setParameterName(TARGET_NAMESPACE).setSchema(Schema.ofAny(TARGET)),
						TARGET, new Parameter().setParameterName(TARGET).setSchema(Schema.ofString(TARGET)),
						CONVERSION_MODE, new Parameter().setParameterName(CONVERSION_MODE)
								.setSchema(Schema.ofString(CONVERSION_MODE)
										.setEnums(ConversionMode.getConversionModes()))))
				.setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.ofAny(VALUE)))));
	}
}

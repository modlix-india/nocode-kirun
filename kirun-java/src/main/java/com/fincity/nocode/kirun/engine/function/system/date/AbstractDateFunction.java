package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.date.ValidDateTimeUtil;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public abstract class AbstractDateFunction extends AbstractReactiveFunction {

	protected static final String ISO_DATE = "isoDate";

	private static final String OUTPUT = "result";

	private static final String ERROR_MSG = "Please provide the valid iso date.";

	private final FunctionSignature functionSignature;

	protected AbstractDateFunction(String namespace, String functionName, String output, SchemaType... schemaType) {

		if (schemaType == null || schemaType.length == 0) {
			schemaType = new SchemaType[] { SchemaType.DOUBLE };
		}

		functionSignature = new FunctionSignature().setName(functionName)
		        .setNamespace(namespace)
		        .setParameters(Map.of(ISO_DATE, new Parameter().setParameterName(ISO_DATE)
		                .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
		        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(output, new Schema().setName(output)
		                .setType(Type.of(schemaType[0]))))));
	}

	protected AbstractDateFunction(String secondName, String namespace, String functionName) {

		functionSignature = new FunctionSignature().setName(functionName)
		        .setNamespace(namespace)
		        .setParameters(Map.of(ISO_DATE, new Parameter().setParameterName(ISO_DATE)
		                .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp")), secondName,
		                new Parameter().setParameterName(secondName)
		                        .setSchema(Schema.ofBoolean(secondName))))
		        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofString(OUTPUT)))));
	}

	@Override
	public FunctionSignature getSignature() {
		return functionSignature;
	}

	public static Entry<String, ReactiveFunction> ofEntryDateAndStringWithOutputName(final String name, String output,
	        Function<String, Number> ufunction, SchemaType... schemaType) {

		return Map.entry(name, new AbstractDateFunction(Namespaces.DATE, name, output, schemaType) {

			@Override
			protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

				String date = context.getArguments()
				        .get(ISO_DATE)
				        .getAsString();

				if (!ValidDateTimeUtil.validate(date))
					throw new KIRuntimeException(ERROR_MSG);

				return Mono.just(new FunctionOutput(
				        List.of(EventResult.outputOf(Map.of(output, new JsonPrimitive(ufunction.apply(date)))))));
			}
		});
	}

	public static Entry<String, ReactiveFunction> ofEntryDateAndIntegerWithOutputName(final String name, String output,
	        Function<String, Number> ufunction, SchemaType... schemaType) {

		return Map.entry(name, new AbstractDateFunction(Namespaces.DATE, name, output, schemaType) {

			@Override
			protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

				String date = context.getArguments()
				        .get(ISO_DATE)
				        .getAsString();

				if (!ValidDateTimeUtil.validate(date))
					throw new KIRuntimeException(ERROR_MSG);

				return Mono.just(new FunctionOutput(
				        List.of(EventResult.outputOf(Map.of(output, new JsonPrimitive(ufunction.apply(date)))))));
			}
		});
	}
}

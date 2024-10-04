package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
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
import com.fincity.nocode.kirun.engine.util.stream.TriFunction;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public abstract class AbstractDateFunction extends AbstractReactiveFunction {

    public static final String EVENT_RESULT_NAME = "result";

    public static final String PARAMETER_DATE_NAME = "isoDate";

    public static final String PARAMETER_FIELD_NAME = "value";

	public static final String PARAMETER_INT_NAME = "intValue";

	public static final String PARAMETER_UNIT_NAME  = "unit";

	private static final String ERROR_MSG = "Please provide the valid iso date.";
	
	private final FunctionSignature functionSignature;

	@Override
	public FunctionSignature getSignature() {
		return functionSignature;
	}

	protected static final Parameter PARAMETER_DATE = new Parameter()
		.setParameterName(PARAMETER_DATE_NAME)
		.setSchema(Schema.ofString(PARAMETER_DATE_NAME).setRef(Namespaces.DATE + ".timeStamp"));


    protected static final Parameter PARAMETER_FIELD  = new Parameter()
		.setParameterName(PARAMETER_FIELD_NAME)
		.setSchema(Schema.ofInteger(PARAMETER_FIELD_NAME));

	protected static final Parameter PARAMETER_UNIT = new Parameter().setParameterName(PARAMETER_UNIT_NAME)
	.setSchema(Schema.ofString(PARAMETER_UNIT_NAME).setEnums(List.of(
		new JsonPrimitive("YEAR"),
		new JsonPrimitive("MONTH"),
		new JsonPrimitive("DAY"),
		new JsonPrimitive("HOUR"),
		new JsonPrimitive("MINUTE"),
		new JsonPrimitive("SECOND"),
		new JsonPrimitive("MILLISECOND"))));

	protected static final Parameter PARAMETER_INT = new Parameter().setParameterName(PARAMETER_INT_NAME)
		.setSchema(Schema.ofInteger(PARAMETER_INT_NAME));

	protected static final Event EVENT_INT = new Event().setName(Event.OUTPUT)
		.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofInteger(EVENT_RESULT_NAME)));

	protected static final Event EVENT_BOOLEAN = new Event().setName(Event.OUTPUT)
		.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofBoolean(EVENT_RESULT_NAME)));

	protected static final Event EVENT_DATE = new Event().setName(Event.OUTPUT)
		.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofString(EVENT_RESULT_NAME).setRef(Namespaces.DATE + ".timeStamp")));


	protected AbstractDateFunction(String namespace, String functionName, Event event, Parameter ... parameters){

		Map<String, Parameter> paramMap = new HashMap<>();

		for(Parameter param : parameters){
			paramMap.put(param.getParameterName(), param);
		}

		functionSignature = new FunctionSignature()
								.setName(functionName)
								.setNamespace(namespace)
								.setParameters(paramMap)
								.setEvents(Map.of(event.getName(), event));

	}

	protected AbstractDateFunction(String namespace, String functionName, String output, SchemaType... schemaType) {

		if (schemaType == null || schemaType.length == 0) {
			schemaType = new SchemaType[] { SchemaType.DOUBLE };
		}

		functionSignature = new FunctionSignature().setName(functionName)
		        .setNamespace(namespace)
		        .setParameters(Map.of(PARAMETER_DATE_NAME, new Parameter().setParameterName(PARAMETER_DATE_NAME)
		                .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
		        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(output, new Schema().setName(output)
		                .setType(Type.of(schemaType[0]))))));
	}

	protected AbstractDateFunction(String secondName, String namespace, String functionName) {

		functionSignature = new FunctionSignature().setName(functionName)
		        .setNamespace(namespace)
		        .setParameters(Map.of(PARAMETER_DATE_NAME, new Parameter().setParameterName(PARAMETER_DATE_NAME)
		                .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp")), secondName,
		                new Parameter().setParameterName(secondName)
		                        .setSchema(Schema.ofBoolean(secondName))))
		        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(EVENT_RESULT_NAME, Schema.ofString(EVENT_RESULT_NAME)))));
	}


 
	public static Entry<String, ReactiveFunction> ofEntryDateAndBooleanOutput(final String functionName, Function<String, Boolean> ufunction){

		return Map.entry(functionName, new AbstractDateFunction( Namespaces.DATE, functionName,  EVENT_BOOLEAN ,  PARAMETER_DATE ) {
		
			@Override
			protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

				String date = context.getArguments()
				        .get(PARAMETER_DATE_NAME)
				        .getAsString();

				if (!ValidDateTimeUtil.validate(date))
					throw new KIRuntimeException(ERROR_MSG);

				return Mono.just(new FunctionOutput(
				        List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(ufunction.apply(date)))))));
			}

		}) ;

	}

	public static Entry<String, ReactiveFunction> ofEntryDateAndIntegerWithOutputName(final String functionName, Function<String, Number> ufunction) {

		return Map.entry(functionName, new AbstractDateFunction(Namespaces.DATE, functionName, EVENT_INT, PARAMETER_DATE) {

			@Override
			protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

				String date = context.getArguments()
				        .get(PARAMETER_DATE_NAME)
				        .getAsString();

				if (!ValidDateTimeUtil.validate(date))
					throw new KIRuntimeException(ERROR_MSG);

				return Mono.just(new FunctionOutput(
				        List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(ufunction.apply(date)))))));
			}
			
		});
	}

	public static Entry<String,ReactiveFunction> ofEntryDateWithIntegerWithOutputDate(final String functionName, 
		BiFunction<String, Integer, String> bifunction){

		return Map.entry(functionName, new AbstractDateFunction(Namespaces.DATE, functionName, EVENT_DATE , PARAMETER_DATE, PARAMETER_INT) {

			@Override
			protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
				String date = context.getArguments()
				        .get(PARAMETER_DATE_NAME)
				        .getAsString();

				if (!ValidDateTimeUtil.validate(date))
					throw new KIRuntimeException(ERROR_MSG);

				int amount = context.getArguments()
				        .get(PARAMETER_INT_NAME)
				        .getAsInt();

				return Mono.just(new FunctionOutput(
				        List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(bifunction.apply(date, amount)))))));
				
			}

		});

	}

	public static Entry<String, ReactiveFunction> ofEntryDateWithIntegerUnitWithOutputName(final String functionName,
			TriFunction<String,Integer,String,String>  trifunction , SchemaType...  schemaType){

				return Map.entry(functionName,

				new AbstractDateFunction(Namespaces.DATE, functionName, EVENT_DATE, PARAMETER_DATE, PARAMETER_INT, PARAMETER_UNIT){

					@Override
					protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
						
						String date = context.getArguments()
							.get(PARAMETER_DATE_NAME)
							.getAsString();

						if (!ValidDateTimeUtil.validate(date))
							throw new KIRuntimeException(ERROR_MSG);

						int value = context.getArguments()
							.get(PARAMETER_INT_NAME)
							.getAsInt();

						String unit = context.getArguments()
							.get(PARAMETER_UNIT_NAME)
							.getAsString();

						return Mono.just(new FunctionOutput(
								List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(trifunction.apply(date, value, unit)))))));
					}

				});
	}

}

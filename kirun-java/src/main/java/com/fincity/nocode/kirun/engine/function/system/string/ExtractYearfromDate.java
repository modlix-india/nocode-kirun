package com.fincity.nocode.kirun.engine.function.system.string;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class ExtractYearfromDate extends AbstractReactiveFunction{

protected static final String PARAMETER_STRING_NAME = "dateinput";
	
	protected static final String EVENT_RESULT_NAME = "result";
	
	protected static final Parameter PARAMETER_STRING = new Parameter().setParameterName(PARAMETER_STRING_NAME)
	        .setSchema(Schema.ofString(PARAMETER_STRING_NAME));
	
	protected static final Event EVENT_STRING = new Event().setName(Event.OUTPUT)
	        .setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofString(EVENT_RESULT_NAME)));

	private final FunctionSignature signature = new FunctionSignature().setName("ExtractYearfromDate")
			 .setNamespace(Namespaces.STRING)
			 .setParameters(
					 Map.of(PARAMETER_STRING.getParameterName(), PARAMETER_STRING ))
			 .setEvents(Map.of(EVENT_STRING.getName(), EVENT_STRING));

	
	@Override
	public FunctionSignature getSignature() {
 		return signature;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
				String dateinput = context.getArguments()
		        .get(PARAMETER_STRING_NAME)
		        .getAsString();
		
		String date = extractYearFromDate(dateinput);
		
		 JsonElement datejson = new JsonPrimitive(date.toString());

	    return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, datejson)))));
	    }

	private String extractYearFromDate(String dateInput) {
	    if (dateInput != null) {
	        DateTimeFormatter[] formatters = {
	            DateTimeFormatter.ISO_INSTANT,
	            DateTimeFormatter.ISO_OFFSET_DATE_TIME
	           
	        };

	        for (DateTimeFormatter formatter : formatters) {
	            try {
	                LocalDateTime localDateTime = LocalDateTime.parse(dateInput, formatter);
	                int year = localDateTime.getYear();
	                return String.valueOf(year);
	            } catch (Exception e) {
	              
	            }
	        }
	    }
	    return dateInput;
	}
	    			   

			

}


	

	
	
	
	
	
	
	
	
	
	
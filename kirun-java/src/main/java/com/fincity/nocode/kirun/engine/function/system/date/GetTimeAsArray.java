package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.date.IsValidIsoDateTime;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import reactor.core.publisher.Mono;

public class GetTimeAsArray extends AbstractReactiveFunction{

	private static final String DATE = "date";

	 private static final String OUTPUT = "output";
	 
	 @Override
		public FunctionSignature getSignature() {
			 return new FunctionSignature().setName("GetTimeAsArray").setNamespace(Namespaces.DATE)
		                .setParameters(
		                        Map.of(DATE,
		                                new Parameter().setParameterName(DATE)
		                                        .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
		                .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofArray(OUTPUT)))));
		    }
	 
	 @Override
		protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
			
			String inputTimestamp = context.getArguments().get(DATE).getAsString();
			
			        
			    if (!IsValidIsoDateTime.checkValidity(inputTimestamp)) {
			        throw new KIRuntimeException("Given String is not convertible to ISO Dateformat");
			    } else {
			    	
			    	 ZonedDateTime zonedDateTime = ZonedDateTime.parse(inputTimestamp);
			    	 
			    	 int[] timeArray = {
			    	            
			    	            zonedDateTime.getHour(),
			    	            zonedDateTime.getMinute(),
			    	            zonedDateTime.getSecond(),
			    	            zonedDateTime.get(ChronoField.MILLI_OF_SECOND)
			    	        };
		    	

                     JsonArray jsonTimeArray = new JsonArray();
                     for (int component : timeArray) {
                     jsonTimeArray.add(new JsonPrimitive(component));
                     
                     
                     }

                       FunctionOutput functionOutput = new FunctionOutput(List.of(EventResult.outputOf(Map.of(OUTPUT, jsonTimeArray))));
                       
                       System.out.println(jsonTimeArray);
                       return Mono.just(functionOutput);
                      
                       
			    }
	 }
}
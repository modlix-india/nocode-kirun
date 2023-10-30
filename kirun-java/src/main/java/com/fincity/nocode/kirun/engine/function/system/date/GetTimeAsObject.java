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
import com.google.gson.JsonObject;

import reactor.core.publisher.Mono;

public class GetTimeAsObject extends AbstractReactiveFunction{

	private static final String DATE = "date";

	 private static final String OUTPUT = "output";
	 
	 @Override
		public FunctionSignature getSignature() {
			 return new FunctionSignature().setName("GetTimeAsObject").setNamespace(Namespaces.DATE)
		                .setParameters(
		                        Map.of(DATE,
		                                new Parameter().setParameterName(DATE)
		                                        .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
		                .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofObject(OUTPUT)))));
		    }
		    	
		  @Override 
		  protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
		    	    String inputTimestamp = context.getArguments().get(DATE).getAsString();

		    	    if (!IsValidIsoDateTime.checkValidity(inputTimestamp)) {
		    	        throw new KIRuntimeException("Given String is not convertible to ISO Dateformat");
		    	    } else {
		    	        ZonedDateTime zonedDateTime = ZonedDateTime.parse(inputTimestamp);

		    	        JsonObject jsonTimeObject = new JsonObject();
		    	        jsonTimeObject.addProperty("hour", zonedDateTime.getHour());
		    	        jsonTimeObject.addProperty("minute", zonedDateTime.getMinute());
		    	        jsonTimeObject.addProperty("second", zonedDateTime.getSecond());
		    	        jsonTimeObject.addProperty("milliseconds", zonedDateTime.get(ChronoField.MILLI_OF_SECOND));;

		    	        FunctionOutput functionOutput = new FunctionOutput(List.of(EventResult.outputOf(Map.of(OUTPUT, jsonTimeObject))));

		    	        System.out.println(jsonTimeObject);
		    	        return Mono.just(functionOutput);
		    	    }
		    	}

		

}

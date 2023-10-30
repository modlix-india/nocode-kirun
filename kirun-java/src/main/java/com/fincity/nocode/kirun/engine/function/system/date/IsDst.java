package com.fincity.nocode.kirun.engine.function.system.date;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.zone.ZoneRules;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
import com.fincity.nocode.kirun.engine.util.date.DateTimePatternUtil;
import com.fincity.nocode.kirun.engine.util.date.IsValidIsoDateTime;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class IsDst extends AbstractReactiveFunction{

	 private static final String DATE = "date";

	 private static final String OUTPUT = "output";

	@Override
	public FunctionSignature getSignature() {
		 return new FunctionSignature().setName("IsDst").setNamespace(Namespaces.DATE)
	                .setParameters(
	                        Map.of(DATE,
	                                new Parameter().setParameterName(DATE)
	                                        .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
	                .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofBoolean(OUTPUT)))));
	    }


	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
		
		String inputTimestamp = context.getArguments().get(DATE).getAsString();
		
		        
		    if (!IsValidIsoDateTime.checkValidity(inputTimestamp)) {
		        throw new KIRuntimeException("Given String is not convertible to ISO Dateformat");
		    } else {
		    	
     
		        Date updatedDate = new Date(DateFunctionRepository.getEpochFromDateTime(inputTimestamp));
		        Calendar cal = Calendar.getInstance();
		        cal.setTime(updatedDate);
		        
		        TimeZone timeZone = cal.getTimeZone();
		        boolean isDST = timeZone.inDaylightTime(cal.getTime());

//		    
//		        ZonedDateTime zonedDateTime = ZonedDateTime.parse(inputTimestamp);
//		    	ZoneId zoneId = ZoneId.ofOffset("UTC",zonedDateTime.getOffset());
//		        System.out.println(zoneId);
//		        boolean isDST = zoneId.getRules().isDaylightSavings(zonedDateTime.toInstant());
		        System.out.println("ISDST: " + isDST);
//		        
               
	    
		        return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(isDST))))));
		    }
		}}
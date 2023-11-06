package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
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
import com.fincity.nocode.kirun.engine.util.date.DateTimePatternUtil;
import com.fincity.nocode.kirun.engine.util.date.GetTimeZoneOffsetUtil;
import com.fincity.nocode.kirun.engine.util.date.IsValidISODateUtil;
import com.google.gson.JsonObject;

import reactor.core.publisher.Mono;

public class GetTimeAsObject extends AbstractReactiveFunction {

    private static final String ISO_DATE = "isoDate";

    private static final String OUTPUT = "result";

    @Override
    public FunctionSignature getSignature() {
        return new FunctionSignature().setName("GetTimeAsObject")
                .setNamespace(Namespaces.DATE)
                .setParameters(
                        Map.ofEntries(Parameter.ofEntry(ISO_DATE, Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofObject(OUTPUT)))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String inputDate = context.getArguments().get(ISO_DATE).getAsString();

        if (!IsValidISODateUtil.checkValidity(inputDate))
            throw new KIRuntimeException("Please provide valid ISO date");

        ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern());

       
        JsonObject dateTimeObject = new JsonObject();
        dateTimeObject.addProperty("year", zdt.getYear());
        dateTimeObject.addProperty("month", zdt.getMonthValue());
        dateTimeObject.addProperty("day", zdt.getDayOfMonth());
        dateTimeObject.addProperty("hours",zdt.getHour());
        dateTimeObject.addProperty("minutes",zdt.getMinute());
        dateTimeObject.addProperty("seconds", zdt.getSecond());
        dateTimeObject.addProperty("milli", zdt.getNano() / 1000000);
        dateTimeObject.addProperty("offset", GetTimeZoneOffsetUtil.getOffset(inputDate) );

    	System.out.println(dateTimeObject);
		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(OUTPUT, dateTimeObject)))));
		
    }

}

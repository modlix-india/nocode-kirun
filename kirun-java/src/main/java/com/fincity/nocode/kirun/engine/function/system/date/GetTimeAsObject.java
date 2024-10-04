package com.fincity.nocode.kirun.engine.function.system.date;

import java.sql.Date;
import java.util.Calendar;
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
import com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil;
import com.fincity.nocode.kirun.engine.util.date.ValidDateTimeUtil;
import com.google.gson.JsonObject;

import reactor.core.publisher.Mono;

public class GetTimeAsObject extends AbstractReactiveFunction {

    private static final String VALUE = "isoDate";

    private static final String OUTPUT = "result";

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName("GetTimeAsObject")
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.of(VALUE, new Parameter().setParameterName(VALUE)
                        .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(
                    Map.of(OUTPUT, Schema.ofObject(OUTPUT).setProperties(Map.of(
                        "year", Schema.ofInteger("year"),
                        "month", Schema.ofInteger("month"),
                        "day", Schema.ofInteger("day"),
                        "hour", Schema.ofInteger("hour"),
                        "minute", Schema.ofInteger("minute"),
                        "second", Schema.ofInteger("second"),
                        "millisecond", Schema.ofInteger("millisecond")
                    ))))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String inputDate = context.getArguments()
		        .get(VALUE)
		        .getAsString();

		if (!ValidDateTimeUtil.validate(inputDate))

			throw new KIRuntimeException("Please provide valid ISO date.");


		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.setTime(new Date(GetTimeInMillisUtil.getEpochTime(inputDate)));

		JsonObject dateObject = new JsonObject();

		dateObject.addProperty("year", calendar.get(Calendar.YEAR));
		dateObject.addProperty("month", calendar.get(Calendar.MONTH) + 1);
		dateObject.addProperty("day", calendar.get(Calendar.DAY_OF_MONTH));
		dateObject.addProperty("hour", calendar.get(Calendar.HOUR_OF_DAY));
		dateObject.addProperty("minute", calendar.get(Calendar.MINUTE));
		dateObject.addProperty("second", calendar.get(Calendar.SECOND));
		dateObject.addProperty("millisecond", calendar.get(Calendar.MILLISECOND));

		
		return Mono.just(new FunctionOutput(List.of(EventResult.of(OUTPUT,
		        Map.of(OUTPUT, dateObject)))));

    }
    
}

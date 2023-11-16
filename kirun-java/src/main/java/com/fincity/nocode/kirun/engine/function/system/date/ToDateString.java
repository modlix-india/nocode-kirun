package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Calendar;
import java.util.Date;
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
import com.fincity.nocode.kirun.engine.util.date.DateFormatterUtil;
import com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil;
import com.fincity.nocode.kirun.engine.util.date.ValidDateTimeUtil;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class ToDateString extends AbstractReactiveFunction {

    private static final String ISO_DATE = "isoDate";
    private static final String DATE_FORMAT = "dateFormat";
    private static final String OUTPUT = "result";

    @Override
    public FunctionSignature getSignature() {
        return new FunctionSignature()
                .setName("ToDateString")
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.ofEntries(
                        Parameter.ofEntry(ISO_DATE, Schema.ofRef(Namespaces.DATE + ".timeStamp")),
                        Parameter.ofEntry(DATE_FORMAT, Schema.ofString(DATE_FORMAT))

                ))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofString(OUTPUT)))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String inputDate = context.getArguments().get(ISO_DATE).getAsString();

        if (!ValidDateTimeUtil.validate(inputDate))
            throw new KIRuntimeException("Please provide valid iso datetime format");

        String dateFormat = context.getArguments().get(DATE_FORMAT).getAsString();

        Calendar cal = Calendar.getInstance();
        Date date = new Date(GetTimeInMillisUtil.getEpochTime(inputDate));
        cal.setTime(date);

        String output = DateFormatterUtil.formattedStringFromDate(cal, dateFormat);

        return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(OUTPUT, new JsonPrimitive(output))))));
    }

}

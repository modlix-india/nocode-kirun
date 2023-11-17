package com.fincity.nocode.kirun.engine.function.system.date;

import java.text.SimpleDateFormat;
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

import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class FromDateString extends AbstractReactiveFunction {

    private static final String DATE_IN_STRING = "date";
    private static final String DATE_FORMAT = "dateFormat";
    private static final String OUTPUT = "result";

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName("FromDateString")
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.ofEntries(
                        Parameter.ofEntry(DATE_IN_STRING, Schema.ofString(DATE_IN_STRING)),
                        Parameter.ofEntry(DATE_FORMAT, Schema.ofString(DATE_FORMAT))))
                .setEvents(Map.ofEntries(
                        Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofRef(Namespaces.DATE + ".timeStamp")))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String formattedDate = context.getArguments().get(DATE_IN_STRING).getAsString();

        String format = context.getArguments().get(DATE_FORMAT).getAsString();

        if (formattedDate.isEmpty() || format.isEmpty())
            throw new KIRuntimeException("Please provide values in the date and format");

        Date date = DateFormatterUtil.dateFromFormattedString(formattedDate, format);

        System.out.println(date);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        System.out.println(sdf.format(date));

        return Mono.just(new FunctionOutput(
                List.of(EventResult.of(OUTPUT, Map.of(OUTPUT, new JsonPrimitive(date.toString()))))));

    }

}

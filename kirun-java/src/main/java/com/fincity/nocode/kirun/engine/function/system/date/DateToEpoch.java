package com.fincity.nocode.kirun.engine.function.system.date;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;

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
import com.google.gson.JsonPrimitive;

import static com.fincity.nocode.kirun.engine.util.date.IsValidIsoDateTime.dateTimePattern;

import reactor.core.publisher.Mono;

public class DateToEpoch extends AbstractReactiveFunction {

    private static final String DATE = "date";

    private static final String OUTPUT = "epoch";

    private static final String ERROR_MSG = "Please provide the valid iso format";

    @Override
    public FunctionSignature getSignature() {

        return new FunctionSignature().setName("DateToEpoch")
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.of(DATE,
                        new Parameter().setParameterName(DATE)
                                .setSchema(Schema.ofRef(Namespaces.DATE + ".timeStamp"))))
                .setEvents(Map.ofEntries(
                        Event.outputEventMapEntry(Map.of(
                                OUTPUT, Schema.ofLong(OUTPUT)))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String inputDate = context.getArguments().get(DATE).getAsString();

        Matcher matcher = dateTimePattern.matcher(inputDate);

        if (!matcher.find())
            throw new KIRuntimeException(ERROR_MSG);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {

            return Mono.just(new FunctionOutput(List.of(EventResult
                    .outputOf(Map.of(OUTPUT, new JsonPrimitive(Long.valueOf(sdf.parse(inputDate).getTime())))))));

        } catch (Exception e) {
            throw new KIRuntimeException(ERROR_MSG);
        }

    }

}
